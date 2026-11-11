package ru.practicum.ewm.request.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.request.model.ParticipationRequest;
import ru.practicum.ewm.model.RequestStatus;

import java.util.Collection;
import java.util.List;

public interface ParticipationRequestRepository extends JpaRepository<ParticipationRequest, Long> {

    List<ParticipationRequest> findByEventId(Long eventId);

    List<ParticipationRequest> findByRequesterId(Long requesterId);

    List<ParticipationRequest> findByIdIn(List<Long> requestIds);

    long countByEventIdAndStatus(Long eventId, RequestStatus status);

    boolean existsByRequesterIdAndEventId(Long requesterId, Long eventId);

    @Query("""
            SELECT r.eventId AS eventId, COUNT(r) AS count
            FROM ParticipationRequest r
            WHERE r.eventId IN :eventIds\s
              AND r.status = :status
            GROUP BY r.eventId
           \s""")
    List<ConfirmedCount> countWithStatus(
            @Param("eventIds") List<Long> eventIds,
            @Param("status") RequestStatus status
    );

    interface ConfirmedCount {
        Long getEventId();

        Long getCount();
    }

    @Modifying
    @Query("DELETE FROM ParticipationRequest r WHERE r.requesterId = :requesterId")
    void deleteByRequesterId(@Param("requesterId") Long requesterId);

    @Modifying
    @Query("DELETE FROM ParticipationRequest r WHERE r.eventId IN :eventIds")
    void deleteByEventIdIn(@Param("eventIds") Collection<Long> eventIds);
}
