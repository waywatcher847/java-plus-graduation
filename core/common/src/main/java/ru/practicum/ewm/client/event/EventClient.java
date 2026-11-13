package ru.practicum.ewm.client.event;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "event-service")
public interface EventClient {
    @GetMapping("/internal/events/{id}")
    EventData get(@PathVariable("id") Long id);
}
