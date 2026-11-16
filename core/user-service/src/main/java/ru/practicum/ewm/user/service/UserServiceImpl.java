package ru.practicum.ewm.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.client.FallBackTemplate;
import ru.practicum.ewm.client.event.EventCleanupClient;

import ru.practicum.ewm.client.request.RequestCleanupClient;
import ru.practicum.ewm.client.socials.SocialsCleanupClient;
import ru.practicum.ewm.dto.user.NewUserRequest;
import ru.practicum.ewm.dto.user.UserDto;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.UserNotFoundException;
import ru.practicum.ewm.user.mapper.UserMapper;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper mapper;
    private final FallBackTemplate fallBack;
    private final EventCleanupClient eventCleanupClient;
    private final SocialsCleanupClient socialsCleanupClient;
    private final RequestCleanupClient requestCleanupClient;

    @Override
    @Transactional
    public UserDto create(NewUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email " + request.getEmail() + " already exists");
        }
        User user = mapper.toUser(request);
        user = userRepository.save(user);
        log.debug("User created: {}", user.getId());
        return mapper.toUserDto(user);
    }

    @Override
    public List<UserDto> getAll(List<Long> ids, Integer from, Integer size) {
        PageRequest page = PageRequest.of(from / size, size);
        List<User> users;
        if (ids != null && !ids.isEmpty()) {
            users = userRepository.findByIdIn(ids, page).getContent();
        } else {
            users = userRepository.findAll(page).getContent();
        }
        log.debug("Users found: {}", users.size());
        return users.stream()
                .map(mapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        fallBack.bestEffort("cleanup-event",
                () -> eventCleanupClient.deleteUser(userId));
        fallBack.bestEffort("cleanup-socials",
                () -> socialsCleanupClient.deleteUser(userId));
        fallBack.bestEffort("cleanup-request",
                () -> requestCleanupClient.deleteUser(userId));

        userRepository.deleteById(userId);

        log.debug("User deleted: {}", userId);
    }
}
