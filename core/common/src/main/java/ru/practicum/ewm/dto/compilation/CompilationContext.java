package ru.practicum.ewm.dto.compilation;

import lombok.Builder;
import lombok.Getter;
import ru.practicum.ewm.dto.ratings.RatingStatsDto;
import ru.practicum.ewm.dto.user.UserShortDto;

import java.util.Map;

@Getter
@Builder
public class CompilationContext {
    private final Map<Long, Long> views;
    private final Map<Long, Long> confirmed;
    private final Map<Long, RatingStatsDto> ratings;
    private final Map<Long, Long> comments;
    private final Map<Long, UserShortDto> initiators;
}