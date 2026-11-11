package ru.practicum.ewm.exception;

public class EventNotFoundException extends NotFoundException {
    public EventNotFoundException(Long id) {
        super("Event with ID = %d not found".formatted(id));
    }
}
