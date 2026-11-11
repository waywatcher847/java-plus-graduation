package ru.practicum.ewm.client.socials;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@FeignClient(name = "socials-service")
public interface SocialsClient {
    @PostMapping("/internal/socials/aggregate")
    SocialsAggregated aggregate(@RequestBody List<Long> ids,
                                @RequestParam(value = "userId", required = false) Long userId);
}
