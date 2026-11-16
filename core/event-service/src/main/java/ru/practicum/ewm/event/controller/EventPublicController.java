package ru.practicum.ewm.event.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.client.FallBackTemplate;
import ru.practicum.ewm.client.StatsFeignClient;
import ru.practicum.ewm.dto.event.EventFullDto;
import ru.practicum.ewm.dto.event.EventSearchRequest;
import ru.practicum.ewm.dto.event.EventShortDto;
import ru.practicum.ewm.event.service.EventService;
import ru.practicum.ewm.stats.dto.StatHitRequest;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class EventPublicController {

    private final EventService eventService;
    private final StatsFeignClient hitClient;
    private final FallBackTemplate fallBack;

    @GetMapping("/events")
    public List<EventShortDto> getPublished(EventSearchRequest request,
                                            @RequestHeader(value = "X-EWM-UserId", required = false) Long userId,
                                            HttpServletRequest httpRequest) {
        request.setUserId(userId);
        saveHit(httpRequest);
        return eventService.getPublished(request);
    }

    @GetMapping("/events/{id}")
    public EventFullDto getPublishedById(@PathVariable Long id,
                                         HttpServletRequest request) {
        saveHit(request);
        return eventService.getPublishedById(id);
    }

    private void saveHit(HttpServletRequest request) {
        fallBack.bestEffort("statisticsHits", () -> {
            hitClient.hit(new StatHitRequest(
                    "ewm-main-service",
                    request.getRequestURI(),
                    request.getRemoteAddr(),
                    LocalDateTime.now()
            ));
        });
    }
}
