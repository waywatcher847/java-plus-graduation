package ru.practicum.ewm.event.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.client.event.EventData;
import ru.practicum.ewm.dto.user.UserShortDto;
import ru.practicum.ewm.exception.EventNotFoundException;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.repository.EventRepository;

@RestController
@RequestMapping("/internal/events")
@RequiredArgsConstructor
public class EventInternalController {
    private final EventRepository repository;

    @GetMapping("/{id}")
    public EventData get(@PathVariable Long id) {
        Event event = repository.findById(id)
                .orElseThrow(() -> new EventNotFoundException(id));
        return new EventData(
                event.getId(),
                new UserShortDto(event.getInitiatorId(), event.getInitiatorName()),
                event.getState(),
                event.getParticipantLimit(),
                event.getRequestModeration()
        );
    }
}