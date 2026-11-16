package ru.practicum.ewm.rating.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.user.UserDto;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.EventNotFoundException;
import ru.practicum.ewm.client.event.EventData;
import ru.practicum.ewm.model.EventState;
import ru.practicum.ewm.client.event.EventReader;
import ru.practicum.ewm.dto.ratings.RatingStatsDto;
import ru.practicum.ewm.rating.model.EventRating;
import ru.practicum.ewm.model.RatingType;
import ru.practicum.ewm.rating.repository.EventRatingRepository;
import ru.practicum.ewm.exception.UserNotFoundException;
import ru.practicum.ewm.client.user.UserReader;
import ru.practicum.ewm.rating.repository.RatingStatsView;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RatingServiceImpl implements RatingService {

    private final EventRatingRepository ratingRepository;
    private final EventReader eventRepository;
    private final UserReader userRepository;

    @Override
    @Transactional
    public void setRating(Long userId, Long eventId, RatingType type) {
        UserDto user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        EventData event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));

        if (event.getState() != EventState.PUBLISHED) {
            throw new ConflictException("Only published events can be rated");
        }
        if (event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("An initiator cannot rate their own event");
        }

        EventRating rating = ratingRepository.findByEventIdAndUserId(eventId, userId)
                .orElseGet(() -> EventRating.builder().eventId(event.getId()).userId(user.getId()).build());
        rating.setType(type);
        ratingRepository.save(rating);
    }

    @Override
    @Transactional
    public void deleteRating(Long userId, Long eventId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));
        ratingRepository.findByEventIdAndUserId(eventId, userId)
                .ifPresent(ratingRepository::delete);
    }

    @Override
    public Map<Long, RatingStatsDto> getStats(Collection<Long> eventIds) {
        if (eventIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return ratingRepository.findStatsByEventIds(eventIds, RatingType.LIKE, RatingType.DISLIKE).stream()
                .collect(Collectors.toMap(
                        RatingStatsView::getEventId,
                        view -> new RatingStatsDto(view.getLikes(), view.getDislikes())
                ));
    }
}
