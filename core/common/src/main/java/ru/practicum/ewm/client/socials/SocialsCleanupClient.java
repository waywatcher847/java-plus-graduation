package ru.practicum.ewm.client.socials;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "socials-service", contextId = "SocialsCleanupClient")
public interface SocialsCleanupClient {
    @DeleteMapping("/internal/socials/users/{id}")
    void deleteUser(@PathVariable("id") Long id);
}
