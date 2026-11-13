package ru.practicum.ewm.exception;

public class UserNotFoundException extends NotFoundException {
    public UserNotFoundException(Long id) {
        super("User with ID = %d not found.".formatted(id));
    }
}
