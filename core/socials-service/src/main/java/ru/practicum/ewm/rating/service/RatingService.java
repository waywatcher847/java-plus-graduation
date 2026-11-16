package ru.practicum.ewm.rating.service;

import ru.practicum.ewm.dto.ratings.RatingStatsDto;
import ru.practicum.ewm.model.RatingType;

import java.util.Collection;
import java.util.Map;

public interface RatingService {

    void setRating(Long userId, Long eventId, RatingType type);

    void deleteRating(Long userId, Long eventId);

    Map<Long, RatingStatsDto> getStats(Collection<Long> eventIds);
}
