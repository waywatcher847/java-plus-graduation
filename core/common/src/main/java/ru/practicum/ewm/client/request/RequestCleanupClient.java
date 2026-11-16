package ru.practicum.ewm.client.request;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "request-service", contextId = "RequestCleanupClient")
public interface RequestCleanupClient {
    @DeleteMapping("/internal/requests/users/{id}")
    void deleteUser(@PathVariable("id") Long id);
}
