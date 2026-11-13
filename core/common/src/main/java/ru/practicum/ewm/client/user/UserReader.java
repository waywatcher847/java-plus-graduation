package ru.practicum.ewm.client.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.dto.user.UserDto;
import ru.practicum.ewm.exception.ApiException;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserReader {
    private final UserClient client;

    public Optional<UserDto> findById(Long id) {
        try {
            return Optional.of(client.get(id));
        } catch (ApiException e) {
            return Optional.empty();
        }
    }
}
