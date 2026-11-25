package ru.practicum.ewm.event.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.client.CollectorGrpcClient;
import ru.practicum.ewm.dto.event.EventFullDto;
import ru.practicum.ewm.dto.event.EventSearchRequest;
import ru.practicum.ewm.dto.event.EventShortDto;
import ru.practicum.ewm.event.service.EventService;

import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class EventPublicController {
    private final EventService eventService;
    private final CollectorGrpcClient collectorClient;

    @GetMapping("/events")
    public List<EventShortDto> getPublished(EventSearchRequest request,
                                            @RequestHeader(value = "X-EWM-USER-ID", required = false) Long userId) {
        request.setUserId(userId);
        return eventService.getPublished(request);
    }

    @GetMapping("/events/{id}")
    public EventFullDto getPublishedById(@PathVariable Long id,
                                         @RequestHeader(value = "X-EWM-USER-ID", required = false) Long userId) {
        if (userId != null) {
            collectorClient.sendView(userId, id);
        }
        return eventService.getPublishedById(id);
    }

    @GetMapping("/events/recommendations")
    public List<EventShortDto> getRecommendations(@RequestHeader("X-EWM-USER-ID") long userId,
                                                  @RequestParam(defaultValue = "10") int limit) {
        return eventService.getRecommendations(userId, limit);
    }

    @PutMapping("/events/{eventId}/like")
    public void likeEvent(@PathVariable Long eventId,
                          @RequestHeader("X-EWM-USER-ID") long userId) {
        eventService.likeEvent(userId, eventId);
    }
}
