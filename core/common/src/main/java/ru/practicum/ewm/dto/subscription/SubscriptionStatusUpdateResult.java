package ru.practicum.ewm.dto.subscription;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionStatusUpdateResult {

    private Long id;

    private String status;
}
