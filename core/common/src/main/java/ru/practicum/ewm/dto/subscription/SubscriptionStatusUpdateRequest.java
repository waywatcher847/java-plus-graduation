package ru.practicum.ewm.dto.subscription;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionStatusUpdateRequest {

    private String status;
}
