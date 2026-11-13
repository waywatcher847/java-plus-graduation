package ru.practicum.ewm.client.user;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.compilation.NewCompilationDto;
import ru.practicum.ewm.dto.user.UserDto;

import java.util.List;

@FeignClient(name = "user-service")
public interface UserClient {
    @GetMapping("/internal/users/{id}")
    UserDto get(@PathVariable("id") Long id);

    @PostMapping("/internal/users/batch")
    List<UserDto> getBatch(@RequestBody List<Long> ids);
}
