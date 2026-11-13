package ru.practicum.ewm.dto.compilation;


import lombok.*;
import ru.practicum.ewm.dto.event.EventShortDto;
import ru.practicum.ewm.dto.user.UserShortDto;

import java.util.List;

@Builder
@Getter
@ToString
public class CompilationDtoResponse {
    private Long id;
    private Boolean pinned;
    private String title;
    private List<EventShortDto> events;
}