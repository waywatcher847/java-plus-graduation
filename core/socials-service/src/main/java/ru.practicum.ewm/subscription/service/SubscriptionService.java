package ru.practicum.ewm.subscription.service;

import ru.practicum.ewm.dto.compilation.CompilationDtoResponse;
import ru.practicum.ewm.dto.ratings.RatingStatsDto;
import ru.practicum.ewm.dto.subscription.SubscriptionDto;
import ru.practicum.ewm.dto.subscription.SubscriptionInputDto;
import ru.practicum.ewm.dto.subscription.SubscriptionStatusUpdateResult;
import ru.practicum.ewm.dto.user.UserShortDto;

import java.util.List;

public interface SubscriptionService {

    SubscriptionDto create(Long userId, SubscriptionInputDto dto);

    SubscriptionStatusUpdateResult confirm(Long userId, Long subscriptionId);

    SubscriptionStatusUpdateResult reject(Long userId, Long subscriptionId);

    List<SubscriptionDto> getAllByUser(Long userId);

    List<SubscriptionDto> getIncoming(Long userId);

    List<SubscriptionDto> getOutgoing(Long userId);

    void cancel(Long userId, Long subscriptionId);

    List<UserShortDto> getFollowers(Long userId);
}
