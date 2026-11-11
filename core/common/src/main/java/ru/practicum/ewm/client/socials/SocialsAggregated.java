package ru.practicum.ewm.client.socials;

import lombok.*;
import ru.practicum.ewm.dto.ratings.RatingStatsDto;
import java.util.Map;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SocialsAggregated {
    private Map<Long, RatingStatsDto> ratings;
    private Map<Long, Long> comments;
    private Set<Long> followed;
}
