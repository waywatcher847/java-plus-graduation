package ru.practicum.ewm.rating.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.model.RatingType;
import ru.practicum.ewm.rating.model.EventRating;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface EventRatingRepository extends JpaRepository<EventRating, Long> {

    Optional<EventRating> findByEventIdAndUserId(Long eventId, Long userId);

    @Query("""
            SELECT r.eventId AS eventId,
                   SUM(CASE WHEN r.type = :likeType THEN 1 ELSE 0 END) AS likes,
                   SUM(CASE WHEN r.type = :dislikeType THEN 1 ELSE 0 END) AS dislikes
            FROM EventRating r
            WHERE r.eventId IN :eventIds
            GROUP BY r.eventId
            """)
    List<RatingStatsView> findStatsByEventIds(
            @Param("eventIds") Collection<Long> eventIds,
            @Param("likeType") RatingType likeType,
            @Param("dislikeType") RatingType dislikeType
    );

    @Modifying
    @Query("DELETE FROM EventRating r WHERE r.userId = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM EventRating r WHERE r.eventId IN :eventIds")
    void deleteByEventIdIn(@Param("eventIds") Collection<Long> eventIds);
}
