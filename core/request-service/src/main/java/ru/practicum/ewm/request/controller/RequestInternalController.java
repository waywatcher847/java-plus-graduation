package ru.practicum.ewm.request.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.request.repository.ParticipationRequestRepository;
import ru.practicum.ewm.model.RequestStatus;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/internal/requests")
@RequiredArgsConstructor
public class RequestInternalController {
    private final ParticipationRequestRepository repository;

    @PostMapping("/counts")
    @Transactional(readOnly = true)
    public Map<Long, Long> counts(@RequestBody List<Long> eventIds) {
        if (eventIds.isEmpty()) return Map.of();
        return repository.countWithStatus(eventIds, RequestStatus.CONFIRMED).stream()
                .collect(Collectors.toMap(ParticipationRequestRepository.ConfirmedCount::getEventId,
                        ParticipationRequestRepository.ConfirmedCount::getCount));

    }

    @DeleteMapping("/users/{id}")
    @Transactional
    public void deleteUser(@PathVariable Long id) {
        repository.deleteByRequesterId(id);
    }

}
