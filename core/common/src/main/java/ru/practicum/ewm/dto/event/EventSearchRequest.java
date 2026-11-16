package ru.practicum.ewm.dto.event;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventSearchRequest {
    private String text;
    private List<Long> categories;
    private Boolean paid;
    private String rangeStart;
    private String rangeEnd;
    private Boolean onlyAvailable;
    private String sort;

    @PositiveOrZero
    @Builder.Default
    private Integer from = 0;

    @Builder.Default
    private Integer size = 10;

    private Long userId;
}
