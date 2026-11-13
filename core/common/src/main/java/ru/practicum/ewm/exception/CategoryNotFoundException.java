package ru.practicum.ewm.exception;

public class CategoryNotFoundException extends NotFoundException {
    public CategoryNotFoundException(Long id) {
        super("Катгория с ID = %d не найдена".formatted(id));
    }
}
