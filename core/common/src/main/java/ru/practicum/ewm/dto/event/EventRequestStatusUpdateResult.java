package ru.practicum.ewm.dto.event;

import java.util.List;
import lombok.*;
import ru.practicum.ewm.dto.request.ParticipationRequestDto;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventRequestStatusUpdateResult {

    private List<ParticipationRequestDto> confirmedRequests;

    private List<ParticipationRequestDto> rejectedRequests;
}
