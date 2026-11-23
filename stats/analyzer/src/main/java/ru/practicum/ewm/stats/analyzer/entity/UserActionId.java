package ru.practicum.ewm.stats.analyzer.entity;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class UserActionId {
    private Long userId;
    private Long eventId;
}