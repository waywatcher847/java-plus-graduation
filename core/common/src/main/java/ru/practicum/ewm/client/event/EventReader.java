package ru.practicum.ewm.client.event;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.exception.ApiException;
import ru.practicum.ewm.model.EventState;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class EventReader {
    private final EventClient client;

    public Optional<EventData> findById(Long id) {
        try {
            return Optional.of(client.get(id));
        } catch (ApiException e) {
            return Optional.empty();
        }
    }

    public Optional<EventData> findByIdAndState(Long id, EventState state) {
        return findById(id).filter(event -> event.getState() == state);
    }
}
