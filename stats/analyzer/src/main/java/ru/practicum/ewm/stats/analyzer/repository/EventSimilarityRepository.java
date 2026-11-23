package ru.practicum.ewm.stats.analyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.stats.analyzer.entity.EventScoreView;
import ru.practicum.ewm.stats.analyzer.entity.EventSimilarity;
import ru.practicum.ewm.stats.analyzer.entity.EventSimilarityId;
import ru.practicum.ewm.stats.analyzer.entity.NeighborView;

import java.time.Instant;
import java.util.List;

public interface EventSimilarityRepository extends JpaRepository<EventSimilarity, EventSimilarityId> {

    @Modifying
    @Transactional
    @Query(value = """
            INSERT INTO event_similarity (event_a, event_b, score, timestamp)
            VALUES (:eventA, :eventB, :score, :timestamp)
            ON CONFLICT (event_a, event_b)
            DO UPDATE SET score = EXCLUDED.score, timestamp = EXCLUDED.timestamp
            """, nativeQuery = true)
    void upsertSimilarity(@Param("eventA") long eventA,
                          @Param("eventB") long eventB,
                          @Param("score") double score,
                          @Param("timestamp") Instant timestamp);

    @Query(value = """
            SELECT *
            FROM event_similarity es
            WHERE :eventId IN (es.event_a, es.event_b)
            """, nativeQuery = true)
    List<EventSimilarity> findByEvent(@Param("eventId") long eventId);

    @Query(value = """
            SELECT CASE WHEN es.event_a IN (:eventIds) THEN es.event_b ELSE es.event_a END AS event_id,
                   es.score AS score
            FROM event_similarity es
            WHERE (es.event_a IN (:eventIds) OR es.event_b IN (:eventIds))
              AND NOT EXISTS (
                  SELECT 1 FROM user_actions ua
                  WHERE ua.user_id = :userId
                    AND ua.event_id = CASE WHEN es.event_a IN (:eventIds) THEN es.event_b ELSE es.event_a END
              )
            ORDER BY score DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<EventScoreView> findUnviewedSimilar(@Param("eventIds") List<Long> eventIds,
                                             @Param("userId") long userId,
                                             @Param("limit") int limit);

    @Query(value = """
            SELECT ua.event_id AS event_id,
                   ua.rating    AS rating,
                   sim.score    AS similarity
            FROM (
                SELECT CASE WHEN es.event_a = :eventId THEN es.event_b ELSE es.event_a END AS neighbor_id,
                       es.score AS score
                FROM event_similarity es
                WHERE (es.event_a = :eventId OR es.event_b = :eventId)
                  AND EXISTS (
                      SELECT 1 FROM user_actions ua
                      WHERE ua.user_id = :userId
                        AND ua.event_id = CASE WHEN es.event_a = :eventId THEN es.event_b ELSE es.event_a END
                  )
                ORDER BY es.score DESC
                LIMIT :kNearestNeighbors
            ) sim
            JOIN user_actions ua
              ON ua.event_id = sim.neighbor_id
             AND ua.user_id = :userId
            """, nativeQuery = true)
    List<NeighborView> findNearestNeighbors(@Param("eventId") long eventId,
                                            @Param("userId") long userId,
                                            @Param("kNearestNeighbors") int kNearestNeighbors);
}