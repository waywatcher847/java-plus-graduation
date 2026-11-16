package ru.practicum.ewm.dto.request;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParticipationRequestDto {

    private Long id;

    private String created;

    private Long event;

    private Long requester;

    private String status;
}
