package ru.practicum.ewm.subscription.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.ewm.dto.subscription.SubscriptionDto;
import ru.practicum.ewm.dto.user.UserShortDto;
import ru.practicum.ewm.subscription.model.Subscription;

@Component
public class SubscriptionMapper {

    public SubscriptionDto toSubscriptionDto(Subscription subscription,
                                             UserShortDto follower,
                                             UserShortDto followed) {
        return SubscriptionDto.builder()
                .id(subscription.getId())
                .follower(follower)
                .followed(followed)
                .status(subscription.getStatus().toString())
                .created(subscription.getCreated().toString())
                .build();
    }
}
