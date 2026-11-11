package ru.practicum.ewm.dto.subscription;

import lombok.*;
import ru.practicum.ewm.dto.user.UserShortDto;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionDto {

    private Long id;

    private UserShortDto follower;

    private UserShortDto followed;

    private String status;

    private String created;
}
