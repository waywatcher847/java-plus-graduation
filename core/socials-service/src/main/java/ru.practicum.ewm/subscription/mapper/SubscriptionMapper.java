package ru.practicum.ewm.subscription.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.ewm.dto.compilation.CompilationDtoResponse;
import ru.practicum.ewm.dto.ratings.RatingStatsDto;
import ru.practicum.ewm.dto.subscription.SubscriptionDto;
import ru.practicum.ewm.dto.user.UserShortDto;
import ru.practicum.ewm.subscription.model.Subscription;

@Component
public class SubscriptionMapper {

    public SubscriptionDto toSubscriptionDto(Subscription subscription) {
        return SubscriptionDto.builder()
                .id(subscription.getId())
                .follower(new UserShortDto(subscription.getFollowerId(), subscription.getFollowerName()))
                .followed(new UserShortDto(subscription.getFollowedId(), subscription.getFollowedName()))
                .status(subscription.getStatus().toString())
                .created(subscription.getCreated().toString())
                .build();
    }
}
