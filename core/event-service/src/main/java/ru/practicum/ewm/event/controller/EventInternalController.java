package ru.practicum.ewm.event.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.client.event.EventData;
import ru.practicum.ewm.client.user.UserReader;
import ru.practicum.ewm.dto.user.UserShortDto;
import ru.practicum.ewm.exception.EventNotFoundException;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.repository.EventRepository;

@RestController
@RequestMapping("/internal/events")
@RequiredArgsConstructor
public class EventInternalController {
    private final EventRepository repository;
    private final UserReader userReader;

    @GetMapping("/{id}")
    public EventData get(@PathVariable Long id) {
        Event event = repository.findById(id)
                .orElseThrow(() -> new EventNotFoundException(id));

        UserShortDto initiator = userReader.findById(event.getInitiatorId())
                .map(u -> new UserShortDto(u.getId(), u.getName()))
                .orElse(new UserShortDto(event.getInitiatorId(), null));

        return new EventData(
                event.getId(),
                initiator,
                event.getState(),
                event.getParticipantLimit(),
                event.getRequestModeration()
        );
    }

    @DeleteMapping("/users/{id}")
    @Transactional
    public void deleteByUser(@PathVariable Long id) {
        repository.deleteByInitiatorId(id);
    }
}