package ru.practicum.ewm.client.socials;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.client.FallBackTemplate;

import java.util.*;

@Component
@RequiredArgsConstructor
public class SocialsReader {
    private final SocialsClient client;
    private final FallBackTemplate resilience;

    public SocialsAggregated aggregate(Collection<Long> ids, Long userId) {
        if (ids.isEmpty()) return new SocialsAggregated(Map.of(), Map.of(), Set.of());
        return resilience.read("socials", () -> client.aggregate(new ArrayList<>(ids), userId),
                () -> new SocialsAggregated(Map.of(), Map.of(), Set.of()));
    }
}
