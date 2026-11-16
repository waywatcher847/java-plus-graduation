package ru.practicum.ewm.subscription.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.subscription.SubscriptionDto;
import ru.practicum.ewm.dto.subscription.SubscriptionInputDto;
import ru.practicum.ewm.dto.subscription.SubscriptionStatusUpdateResult;
import ru.practicum.ewm.dto.user.UserDto;
import ru.practicum.ewm.dto.user.UserShortDto;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.subscription.mapper.SubscriptionMapper;
import ru.practicum.ewm.subscription.model.Subscription;
import ru.practicum.ewm.model.SubscriptionStatus;
import ru.practicum.ewm.subscription.repository.SubscriptionRepository;
import ru.practicum.ewm.exception.SubscriptionNotFoundException;
import ru.practicum.ewm.exception.UserNotFoundException;


import ru.practicum.ewm.client.user.UserReader;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserReader userRepository;
    private final SubscriptionMapper mapper;


    @Override
    @Transactional
    public SubscriptionDto create(Long userId, SubscriptionInputDto dto) {
        UserDto follower = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        UserDto followed = userRepository.findById(dto.getTargetUserId())
                .orElseThrow(() -> new UserNotFoundException(dto.getTargetUserId()));
        if (follower.getId().equals(followed.getId())) {
            throw new ConflictException("Cannot subscribe to yourself");
        }
        if (subscriptionRepository.existsByFollowerIdAndFollowedId(follower.getId(), followed.getId())) {
            throw new ConflictException("Subscription already exists");
        }
        Subscription subscription = Subscription.builder()
                .followerId(follower.getId())
                .followedId(followed.getId())
                .status(SubscriptionStatus.PENDING)
                .created(LocalDateTime.now())
                .build();
        subscription = subscriptionRepository.save(subscription);

        log.debug("Subscription created: followerId={}, followedId={}",
                follower.getId(), followed.getId());
        return mapper.toSubscriptionDto(
                subscription,
                new UserShortDto(follower.getId(), follower.getName()),
                new UserShortDto(followed.getId(), followed.getName()));
    }

    @Override
    @Transactional
    public SubscriptionStatusUpdateResult confirm(Long userId, Long subscriptionId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new SubscriptionNotFoundException("Subscription not found"));

        if (!subscription.getFollowedId().equals(userId)) {
            throw new SubscriptionNotFoundException("Subscription not found");
        }

        if (subscription.getStatus() != SubscriptionStatus.PENDING) {
            throw new ConflictException("Subscription must be in PENDING status");
        }

        subscription.setStatus(SubscriptionStatus.CONFIRMED);
        subscriptionRepository.save(subscription);
        log.debug("Subscription confirmed: {}", subscriptionId);
        return new SubscriptionStatusUpdateResult(subscription.getId(), subscription.getStatus().toString());
    }

    @Override
    @Transactional
    public SubscriptionStatusUpdateResult reject(Long userId, Long subscriptionId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new SubscriptionNotFoundException("Subscription not found"));

        if (!subscription.getFollowedId().equals(userId)) {
            throw new SubscriptionNotFoundException("Subscription not found");
        }

        if (subscription.getStatus() != SubscriptionStatus.PENDING) {
            throw new ConflictException("Subscription must be in PENDING status");
        }

        subscription.setStatus(SubscriptionStatus.REJECTED);
        subscriptionRepository.save(subscription);
        log.debug("Subscription rejected: {}", subscriptionId);
        return new SubscriptionStatusUpdateResult(subscription.getId(), subscription.getStatus().toString());
    }

    @Override
    public List<SubscriptionDto> getAllByUser(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        return mapWithUsers(subscriptionRepository.findByFollowerId(userId));
    }

    @Override
    public List<SubscriptionDto> getIncoming(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        return mapWithUsers(
                subscriptionRepository.findByFollowedIdAndStatus(userId, SubscriptionStatus.PENDING));
    }

    @Override
    public List<SubscriptionDto> getOutgoing(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        return mapWithUsers(
                subscriptionRepository.findByFollowerIdAndStatus(userId, SubscriptionStatus.PENDING));
    }

    @Override
    @Transactional
    public void cancel(Long userId, Long subscriptionId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new SubscriptionNotFoundException("Subscription not found"));

        if (!subscription.getFollowerId().equals(userId)) {
            throw new SubscriptionNotFoundException("Subscription not found");
        }

        subscription.setStatus(SubscriptionStatus.CANCELED);
        subscriptionRepository.save(subscription);
        log.debug("Subscription cancelled: {}", subscriptionId);
    }

    @Override
    public List<UserShortDto> getFollowers(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        List<Subscription> subs = subscriptionRepository
                .getFollowers(userId, SubscriptionStatus.CONFIRMED);

        Set<Long> followerIds = subs.stream()
                .map(Subscription::getFollowerId)
                .collect(Collectors.toSet());
        Map<Long, UserDto> users = userRepository.findByIds(followerIds);

        return subs.stream()
                .map(s -> toShort(users.get(s.getFollowerId()), s.getFollowerId()))
                .toList();
    }

    private List<SubscriptionDto> mapWithUsers(List<Subscription> subs) {
        if (subs.isEmpty()) return List.of();

        Set<Long> userIds = new HashSet<>();
        subs.forEach(s -> {
            userIds.add(s.getFollowerId());
            userIds.add(s.getFollowedId());
        });

        Map<Long, UserDto> users = userRepository.findByIds(userIds);

        return subs.stream().map(s -> mapper.toSubscriptionDto(
                s,
                toShort(users.get(s.getFollowerId()), s.getFollowerId()),
                toShort(users.get(s.getFollowedId()), s.getFollowedId())
        )).collect(Collectors.toList());
    }

    private UserShortDto toShort(UserDto u, Long fallbackId) {
        return u != null
                ? new UserShortDto(u.getId(), u.getName())
                : new UserShortDto(fallbackId, null);
    }
}
