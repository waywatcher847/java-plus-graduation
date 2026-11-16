package ru.practicum.ewm.event.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.event.AdminEventSearchRequest;
import ru.practicum.ewm.dto.event.EventFullDto;
import ru.practicum.ewm.dto.event.UpdateEventAdminRequest;
import ru.practicum.ewm.event.service.EventService;

import java.util.List;

@RestController
@RequestMapping("/admin/events")
@RequiredArgsConstructor
public class EventAdminController {

    private final EventService eventService;

    @GetMapping
    public List<EventFullDto> getByAdmin(AdminEventSearchRequest request) {
        return eventService.getByAdmin(request);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateByAdmin(@PathVariable Long eventId,
                                      @Valid @RequestBody UpdateEventAdminRequest dto) {
        return eventService.updateByAdmin(eventId, dto);
    }
}
