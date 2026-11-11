package ru.practicum.ewm.subscription.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.subscription.model.Subscription;
import ru.practicum.ewm.model.SubscriptionStatus;

import java.util.List;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    List<Subscription> findByFollowerId(Long followerId);

    List<Subscription> findByFollowerIdAndStatus(Long followerId, SubscriptionStatus status);

    List<Subscription> findByFollowedIdAndStatus(Long followedId, SubscriptionStatus status);

    boolean existsByFollowerIdAndFollowedId(Long followerId, Long followedId);

    @Query("""
            SELECT sub
            FROM Subscription sub
            WHERE sub.followedId = :userId
              AND sub.status = :status
            """)
    List<Subscription> getFollowers(
            @Param("userId") Long userId,
            @Param("status") SubscriptionStatus status
    );

    @Modifying
    @Query("DELETE FROM Subscription s WHERE s.follower.id = :userId OR s.followed.id = :userId")
    void deleteByFollowerIdOrFollowedId(@Param("userId") Long userId);

}
