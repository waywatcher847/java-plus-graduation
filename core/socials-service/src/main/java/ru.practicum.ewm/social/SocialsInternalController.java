package ru.practicum.ewm.social;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.client.socials.SocialsAggregated;
import ru.practicum.ewm.comment.repository.CommentRepository;
import ru.practicum.ewm.rating.repository.EventRatingRepository;
import ru.practicum.ewm.rating.service.RatingService;
import ru.practicum.ewm.model.SubscriptionStatus;
import ru.practicum.ewm.subscription.model.Subscription;
import ru.practicum.ewm.subscription.repository.SubscriptionRepository;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/internal/socials")
@RequiredArgsConstructor
public class SocialsInternalController {
    private final CommentRepository comments;
    private final EventRatingRepository eventRating;
    private final RatingService ratings;
    private final SubscriptionRepository subscriptions;

    @DeleteMapping("/users/{id}")
    @Transactional
    public void deleteUser(@PathVariable Long id) {
        comments.deleteByAuthorId(id);
        eventRating.deleteByUserId(id);
        subscriptions.deleteByFollowerIdOrFollowedId(id);
    }

    @PostMapping("/aggregate")
    @Transactional(readOnly = true)
    public SocialsAggregated aggregate(@RequestBody List<Long> ids,
                                       @RequestParam(required = false) Long userId) {
        var counts = ids.isEmpty() ? Collections.<Long, Long>emptyMap()
                : comments.countByEventIdIn(ids).stream()
                .collect(Collectors.toMap(CommentRepository.CommentCountView::getEventId, CommentRepository.CommentCountView::getCnt));

        var followed = userId == null ? Collections.<Long>emptySet()
                : subscriptions.findByFollowerIdAndStatus(userId, SubscriptionStatus.CONFIRMED).stream()
                .map(Subscription::getFollowedId).collect(Collectors.toSet());
        return new SocialsAggregated(ratings.getStats(ids), counts, followed);
    }

}
