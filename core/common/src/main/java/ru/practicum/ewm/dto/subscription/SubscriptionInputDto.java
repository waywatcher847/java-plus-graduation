package ru.practicum.ewm.dto.subscription;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionInputDto {

    private Long targetUserId;
}
