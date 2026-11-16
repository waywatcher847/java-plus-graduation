package ru.practicum.ewm.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.event.*;
import ru.practicum.ewm.dto.ratings.RatingStatsDto;
import ru.practicum.ewm.dto.user.UserDto;
import ru.practicum.ewm.dto.user.UserShortDto;
import ru.practicum.ewm.exception.CategoryNotFoundException;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.repository.CategoryRepository;
import ru.practicum.ewm.client.socials.SocialsReader;
import ru.practicum.ewm.common.util.EventUtil;
import ru.practicum.ewm.exception.EventNotFoundException;
import ru.practicum.ewm.event.mapper.EventMapper;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.model.EventState;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.event.repository.EventSpecification;

import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.ValidationException;


import ru.practicum.ewm.exception.UserNotFoundException;
import ru.practicum.ewm.client.user.UserReader;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

    private static final String PUBLISH_EVENT = "PUBLISH_EVENT";
    private static final String REJECT_EVENT = "REJECT_EVENT";

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final long HOURS_BEFORE_EVENT = 2;

    private final EventRepository eventRepository;
    private final UserReader userRepository;
    private final CategoryRepository categoryRepository;

    private final SocialsReader socialsReader;
    private final EventUtil eventUtil;
    private final EventMapper mapper;


    @Override
    @Transactional
    public EventFullDto create(Long userId, NewEventDto dto) {
        UserDto user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        Category category = categoryRepository.findById(dto.getCategory())
                .orElseThrow(() -> new CategoryNotFoundException(dto.getCategory()));

        LocalDateTime eventDate = LocalDateTime.parse(dto.getEventDate(), FORMATTER);
        if (eventDate.isBefore(LocalDateTime.now().plusHours(HOURS_BEFORE_EVENT))) {
            throw new ValidationException("Field: eventDate. Error: должно содержать дату, которая еще не наступила. Value: " + dto.getEventDate());
        }

        Event event = mapper.toEvent(dto, category, user.getId());
        event = eventRepository.save(event);
        log.debug("Event created: {} by user {}", event.getId(), userId);

        UserShortDto initiator = new UserShortDto(user.getId(), user.getName());
        return mapper.toEventFullDto(event, 0L, 0L, RatingStatsDto.EMPTY, 0L, initiator);
    }

    @Override
    public List<EventShortDto> getAllByUser(Long userId, Integer from, Integer size) {
        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        PageRequest page = PageRequest.of(from / size, size);
        List<Event> events = eventRepository.findByInitiatorId(userId, page).getContent();

        Map<Long, Long> views = eventUtil.views(events.stream().map(Event::getId).toList());
        Map<Long, Long> confirmedCounts = eventUtil.confirmed(events.stream().map(Event::getId).toList());
        var aggregatedSocials = socialsReader.aggregate(events.stream().map(Event::getId).toList(), null);
        Map<Long, RatingStatsDto> ratings = aggregatedSocials.getRatings();
        Map<Long, Long> commentCounts = aggregatedSocials.getComments();

        Map<Long, UserShortDto> initiators = loadInitiators(events);

        return events.stream()
                .map(e -> mapper.toEventShortDto(
                        e,
                        views.getOrDefault(e.getId(), 0L),
                        confirmedCounts.getOrDefault(e.getId(), 0L),
                        ratingFor(e.getId(), ratings),
                        countFor(e.getId(), commentCounts),
                        initiators.getOrDefault(e.getInitiatorId(), new UserShortDto(e.getInitiatorId(), null))
                ))
                .collect(Collectors.toList());
    }

    @Override
    public EventFullDto getByUserAndEvent(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));

        if (!event.getInitiatorId().equals(userId)) {
            throw new EventNotFoundException(eventId);
        }
        return mapper.toEventFullDto(event, eventUtil.getViews(eventId),
                eventUtil.getConfirmedRequests(eventId),
                getRating(eventId), getCommentCount(eventId), loadInitiator(event.getInitiatorId()
                ));
    }

    @Override
    @Transactional
    public EventFullDto updateByUser(Long userId, Long eventId, UpdateEventUserRequest dto) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));

        if (!event.getInitiatorId().equals(userId)) {
            throw new EventNotFoundException(eventId);
        }

        if (event.getState() == EventState.PUBLISHED) {
            throw new ConflictException("Only pending or canceled events can be changed");
        }

        if (dto.getEventDate() != null) {
            LocalDateTime newDate = LocalDateTime.parse(dto.getEventDate(), FORMATTER);
            if (newDate.isBefore(LocalDateTime.now().plusHours(HOURS_BEFORE_EVENT))) {
                throw new ValidationException("Field: eventDate. Error: должно содержать дату, которая еще не наступила. Value: " + dto.getEventDate());
            }
        }

        Category category = null;
        if (dto.getCategory() != null) {
            category = categoryRepository.findById(dto.getCategory())
                    .orElseThrow(() -> new CategoryNotFoundException(dto.getCategory()));
        }

        EventMapper.updateEventFromUserRequest(event, dto, category);

        if (dto.getStateAction() != null) {
            switch (dto.getStateAction()) {
                case "SEND_TO_REVIEW":
                    event.setState(EventState.PENDING);
                    break;
                case "CANCEL_REVIEW":
                    event.setState(EventState.CANCELED);
                    break;
                default:
                    throw new ValidationException("Unknown state action: " + dto.getStateAction());
            }
        }

        event = eventRepository.save(event);

        log.debug("Event updated by user: {}", eventId);
        return mapper.toEventFullDto(event, eventUtil.getViews(eventId),
                eventUtil.getConfirmedRequests(eventId), getRating(eventId), getCommentCount(eventId),
                loadInitiator(event.getInitiatorId()));
    }

    @Override
    public List<EventFullDto> getByAdmin(AdminEventSearchRequest request) {
        List<Long> users = request.getUsers();
        List<String> states = request.getStates();
        List<Long> categories = request.getCategories();
        String rangeStart = request.getRangeStart();
        String rangeEnd = request.getRangeEnd();
        int from = request.getFrom() != null ? request.getFrom() : 0;
        int size = request.getSize() != null ? request.getSize() : 10;

        List<EventState> stateEnums = null;
        if (states != null && !states.isEmpty()) {
            stateEnums = states.stream().map(EventState::valueOf).collect(Collectors.toList());
        }

        LocalDateTime start = rangeStart != null ? LocalDateTime.parse(rangeStart, FORMATTER) : null;
        LocalDateTime end = rangeEnd != null ? LocalDateTime.parse(rangeEnd, FORMATTER) : null;

        if (start != null && end != null && start.isAfter(end)) {
            throw new ValidationException("Field: rangeStart. Error: start must be before end. Value: rangeStart=" + rangeStart + ", rangeEnd=" + rangeEnd);
        }

        PageRequest page = PageRequest.of(from / size, size);
        List<Event> events = eventRepository.findAll(
                EventSpecification.eventsByAdmin(users, stateEnums, categories, start, end),
                page
        ).getContent();

        var aggregatedSocials = socialsReader.aggregate(events.stream().map(Event::getId).toList(), null);
        Map<Long, Long> views = eventUtil.views(events.stream().map(Event::getId).toList());
        Map<Long, Long> confirmedCounts = eventUtil.confirmed(events.stream().map(Event::getId).toList());
        Map<Long, RatingStatsDto> ratings = aggregatedSocials.getRatings();
        Map<Long, Long> commentCounts = aggregatedSocials.getComments();

        Map<Long, UserShortDto> initiators = loadInitiators(events);

        return events.stream()
                .map(e -> mapper.toEventFullDto(e, views.getOrDefault(e.getId(), 0L),
                        confirmedCounts.getOrDefault(e.getId(), 0L), ratingFor(e.getId(), ratings),
                        countFor(e.getId(), commentCounts),
                        initiators.getOrDefault(e.getInitiatorId(), new UserShortDto(e.getInitiatorId(), null))
                ))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventFullDto updateByAdmin(Long eventId, UpdateEventAdminRequest dto) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));

        Category category = null;
        if (dto.getCategory() != null) {
            category = categoryRepository.findById(dto.getCategory())
                    .orElseThrow(() -> new CategoryNotFoundException(dto.getCategory()));
        }

        if (dto.getEventDate() != null) {
            LocalDateTime newDate = LocalDateTime.parse(dto.getEventDate(), FORMATTER);
            if (newDate.isBefore(LocalDateTime.now().plusHours(1))) {
                throw new ValidationException("Field: eventDate. Error: должно содержать дату, которая еще не наступила. Value: " + dto.getEventDate());
            }
        }

        EventMapper.updateEventFromAdminRequest(event, dto, category);

        if (dto.getStateAction() != null) {
            switch (dto.getStateAction()) {
                case PUBLISH_EVENT:
                    if (event.getState() != EventState.PENDING) {
                        throw new ConflictException("Cannot publish the event because it's not in the right state: " + event.getState());
                    }
                    if (event.getEventDate().isBefore(LocalDateTime.now().plusHours(1))) {
                        throw new ConflictException("Event date must be at least 1 hour from now");
                    }
                    event.setState(EventState.PUBLISHED);
                    event.setPublishedOn(LocalDateTime.now());
                    break;
                case REJECT_EVENT:
                    if (event.getState() == EventState.PUBLISHED) {
                        throw new ConflictException("Cannot reject the event because it's already published");
                    }
                    event.setState(EventState.CANCELED);
                    break;
                default:
                    throw new ValidationException("Unknown state action: " + dto.getStateAction());
            }
        }

        event = eventRepository.save(event);
        log.debug("Event updated by admin: {}", eventId);
        return mapper.toEventFullDto(event, eventUtil.getViews(eventId),
                eventUtil.getConfirmedRequests(eventId), getRating(eventId), getCommentCount(eventId),
                loadInitiator(event.getInitiatorId()));
    }

    @Override
    public List<EventShortDto> getPublished(EventSearchRequest request) {
        String text = request.getText();
        List<Long> categories = request.getCategories();
        Boolean paid = request.getPaid();
        String rangeStart = request.getRangeStart();
        String rangeEnd = request.getRangeEnd();
        Boolean onlyAvailable = request.getOnlyAvailable();
        String sort = request.getSort();
        int from = request.getFrom() != null ? request.getFrom() : 0;
        int size = request.getSize() != null ? request.getSize() : 10;
        Long userId = request.getUserId();

        LocalDateTime start = rangeStart != null ? LocalDateTime.parse(rangeStart, FORMATTER) : null;
        LocalDateTime end = rangeEnd != null ? LocalDateTime.parse(rangeEnd, FORMATTER) : null;

        if (start != null && end != null && start.isAfter(end)) {
            throw new ValidationException("Field: rangeStart. Error: start must be before end. Value: rangeStart=" + rangeStart + ", rangeEnd=" + rangeEnd);
        }

        if (start == null && end == null) {
            start = LocalDateTime.now();
        }

        boolean commentsSort = "COMMENTS".equals(sort);
        boolean ratingSort = "RATING".equals(sort);
        boolean noPaginationSort = commentsSort || ratingSort;
        List<Event> events;
        if (noPaginationSort) {
            events = eventRepository.findAll(
                    EventSpecification.publishedEvents(text, categories, paid, start, end)
            );
        } else {
            PageRequest page = PageRequest.of(from / size, size);
            events = eventRepository.findAll(
                    EventSpecification.publishedEvents(text, categories, paid, start, end),
                    page
            ).getContent();
        }

        Map<Long, Long> views = eventUtil.views(events.stream().map(Event::getId).toList());
        Map<Long, Long> confirmedCounts = eventUtil.confirmed(events.stream().map(Event::getId).toList());
        var aggregatedSocials = socialsReader.aggregate(events.stream().map(Event::getId).toList(), userId);
        Map<Long, RatingStatsDto> ratings = aggregatedSocials.getRatings();
        Map<Long, Long> commentCounts = aggregatedSocials.getComments();
        List<EventShortDto> result = new ArrayList<>();
        List<EventShortDto> subscribedEvents = new ArrayList<>();
        List<EventShortDto> otherEvents = new ArrayList<>();

        Map<Long, UserShortDto> initiators = loadInitiators(events);


        Set<Long> followed = aggregatedSocials.getFollowed();
        for (Event e : events) {
            long confirmed = confirmedCounts.getOrDefault(e.getId(), 0L);
            if (Boolean.TRUE.equals(onlyAvailable) && e.getParticipantLimit() != 0
                    && confirmed >= e.getParticipantLimit()) {
                continue;
            }
            EventShortDto dto = mapper.toEventShortDto(e, views.getOrDefault(e.getId(), 0L), confirmed,
                    ratingFor(e.getId(), ratings), countFor(e.getId(), commentCounts
                    ), initiators.getOrDefault(e.getInitiatorId(), new UserShortDto(e.getInitiatorId(), null)));
            if (followed.contains(e.getInitiatorId())) {
                subscribedEvents.add(dto);
            } else {
                otherEvents.add(dto);
            }
        }

        if (commentsSort) {
            subscribedEvents.sort(Comparator.comparingLong(EventShortDto::getCommentCount).reversed());
            otherEvents.sort(Comparator.comparingLong(EventShortDto::getCommentCount).reversed());
        } else if (ratingSort) {
            Comparator<EventShortDto> byRating =
                    Comparator.comparingLong(EventShortDto::getRating).reversed();
            subscribedEvents.sort(byRating);
            otherEvents.sort(byRating);
        } else if ("VIEWS".equals(sort)) {
            subscribedEvents.sort(Comparator.comparingLong(EventShortDto::getViews));
            otherEvents.sort(Comparator.comparingLong(EventShortDto::getViews));
        }

        result.addAll(subscribedEvents);
        result.addAll(otherEvents);

        if (noPaginationSort) {
            int startIndex = Math.min(from, result.size());
            int endIndex = Math.min(startIndex + size, result.size());
            return new ArrayList<>(result.subList(startIndex, endIndex));
        }

        return result;
    }

    @Override
    public EventFullDto getPublishedById(Long id) {
        Event event = eventRepository.findByIdAndState(id, EventState.PUBLISHED)
                .orElseThrow(() -> new EventNotFoundException(id));

        return mapper.toEventFullDto(event, eventUtil.getViews(id),
                eventUtil.getConfirmedRequests(id), getRating(id), getCommentCount(id),
                loadInitiator(event.getInitiatorId()));
    }

    private Map<Long, RatingStatsDto> getRatings(Collection<Event> events) {
        return socialsReader.aggregate(events.stream().map(Event::getId).toList(), null).getRatings();
    }

    private RatingStatsDto getRating(Long eventId) {
        return ratingFor(eventId, socialsReader.aggregate(List.of(eventId), null).getRatings());
    }

    private RatingStatsDto ratingFor(Long eventId, Map<Long, RatingStatsDto> ratings) {
        return ratings.getOrDefault(eventId, RatingStatsDto.EMPTY);
    }

    private Map<Long, Long> getCommentCounts(Collection<Event> events) {
        return socialsReader.aggregate(events.stream().map(Event::getId).toList(), null).getComments();
    }

    private Long getCommentCount(Long eventId) {
        return socialsReader.aggregate(List.of(eventId), null).getComments().getOrDefault(eventId, 0L);
    }

    private Long countFor(Long eventId, Map<Long, Long> commentCounts) {
        return commentCounts.getOrDefault(eventId, 0L);
    }

    private Map<Long, UserShortDto> loadInitiators(Collection<Event> events) {
        Set<Long> ids = events.stream().map(Event::getInitiatorId).collect(Collectors.toSet());
        return userRepository.findShortByIds(ids);
    }

    private UserShortDto loadInitiator(Long initiatorId) {
        return userRepository.findById(initiatorId)
                .map(u -> new UserShortDto(u.getId(), u.getName()))
                .orElse(new UserShortDto(initiatorId, null));
    }
}
