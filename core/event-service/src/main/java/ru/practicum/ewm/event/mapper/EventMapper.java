package ru.practicum.ewm.event.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.dto.category.CategoryDto;
import ru.practicum.ewm.dto.event.*;
import ru.practicum.ewm.dto.location.LocationDto;
import ru.practicum.ewm.dto.ratings.RatingStatsDto;
import ru.practicum.ewm.dto.user.UserShortDto;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.model.EventState;
import ru.practicum.ewm.event.model.Location;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class EventMapper {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public Event toEvent(NewEventDto dto, Category category, Long initiatorId) {
        return Event.builder()
                .annotation(dto.getAnnotation())
                .category(category)
                .createdOn(LocalDateTime.now())
                .description(dto.getDescription())
                .eventDate(LocalDateTime.parse(dto.getEventDate(), FORMATTER))
                .initiatorId(initiatorId)
                .location(new Location(dto.getLocation().getLat(), dto.getLocation().getLon()))
                .paid(dto.getPaid() != null ? dto.getPaid() : false)
                .participantLimit(dto.getParticipantLimit() != null ? dto.getParticipantLimit() : 0)
                .requestModeration(dto.getRequestModeration() != null ? dto.getRequestModeration() : true)
                .state(EventState.PENDING)
                .title(dto.getTitle())
                .build();
    }

    public EventFullDto toEventFullDto(Event event, Double rating, Long confirmedRequests,
                                       RatingStatsDto ratingStats, Long commentCount,
                                       UserShortDto initiator) {
        return EventFullDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(new CategoryDto(event.getCategory().getId(), event.getCategory().getName()))
                .confirmedRequests(confirmedRequests)
                .createdOn(event.getCreatedOn() != null ? event.getCreatedOn().format(FORMATTER) : null)
                .description(event.getDescription())
                .eventDate(event.getEventDate().format(FORMATTER))
                .initiator(initiator)
                .location(new LocationDto(event.getLocation().getLat(), event.getLocation().getLon()))
                .paid(event.getPaid())
                .participantLimit(event.getParticipantLimit())
                .publishedOn(event.getPublishedOn() != null ? event.getPublishedOn().format(FORMATTER) : null)
                .requestModeration(event.getRequestModeration())
                .state(event.getState().name())
                .title(event.getTitle())
                .rating(rating)
                .likes(ratingStats.getLikes())
                .dislikes(ratingStats.getDislikes())
                .commentCount(commentCount)
                .build();
    }

    public static EventShortDto toEventShortDto(Event event, Double rating, Long confirmedRequests,
                                                RatingStatsDto ratingStats, Long commentCount,
                                                UserShortDto initiator) {
        return EventShortDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(new CategoryDto(event.getCategory().getId(), event.getCategory().getName()))
                .confirmedRequests(confirmedRequests)
                .eventDate(event.getEventDate().format(FORMATTER))
                .initiator(initiator)
                .paid(event.getPaid())
                .title(event.getTitle())
                .rating(rating)
                .likes(ratingStats.getLikes())
                .dislikes(ratingStats.getDislikes())
                .commentCount(commentCount)
                .build();
    }

    public static void updateEventFromUserRequest(Event event, UpdateEventUserRequest dto, Category category) {
        EventUpdater.apply(event, dto, category);
    }

    public static void updateEventFromAdminRequest(Event event, UpdateEventAdminRequest dto, Category category) {
        EventUpdater.apply(event, dto, category);
    }
}