package ru.practicum.ewm.client.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.dto.user.UserDto;
import ru.practicum.ewm.dto.user.UserShortDto;
import ru.practicum.ewm.exception.ApiException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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

    public Map<Long, UserShortDto> findShortByIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) return Map.of();
        try {
            return client.getBatch(new ArrayList<>(ids)).stream().collect(Collectors.toMap(UserDto::getId,
                    u -> new UserShortDto(u.getId(), u.getName()), (a, b) -> a));
        } catch (ApiException e) {
            return Map.of();
        }
    }

    public Map<Long, UserDto> findByIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) return Map.of();
        try {
            return client.getBatch(new ArrayList<>(ids)).stream().collect(Collectors.toMap(UserDto::getId, u -> u, (a, b) -> a));
        } catch (ApiException e) {
            return Map.of();
        }
    }
}
