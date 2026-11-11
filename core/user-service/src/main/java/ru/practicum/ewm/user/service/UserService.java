package ru.practicum.ewm.user.service;

import ru.practicum.ewm.dto.compilation.NewCompilationDto;
import ru.practicum.ewm.dto.user.NewUserRequest;
import ru.practicum.ewm.dto.user.UserDto;

import java.util.List;

public interface UserService {

    UserDto create(NewUserRequest request);

    List<UserDto> getAll(List<Long> ids, Integer from, Integer size);

    void delete(Long userId);
}
