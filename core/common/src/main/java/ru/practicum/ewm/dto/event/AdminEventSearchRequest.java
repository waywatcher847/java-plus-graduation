package ru.practicum.ewm.dto.event;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminEventSearchRequest {
    private List<Long> users;
    private List<String> states;
    private List<Long> categories;
    private String rangeStart;
    private String rangeEnd;

    @PositiveOrZero
    @Builder.Default
    private Integer from = 0;

    @Builder.Default
    private Integer size = 10;
}