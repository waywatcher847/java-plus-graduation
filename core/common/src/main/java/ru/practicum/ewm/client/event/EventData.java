package ru.practicum.ewm.client.event;

import lombok.*;
import ru.practicum.ewm.dto.user.UserShortDto;
import ru.practicum.ewm.model.EventState;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventData {
    private Long id;
    private UserShortDto initiator;
    private EventState state;
    private Integer participantLimit;
    private Boolean requestModeration;
}
