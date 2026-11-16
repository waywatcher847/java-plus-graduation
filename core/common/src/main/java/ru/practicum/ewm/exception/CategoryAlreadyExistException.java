package ru.practicum.ewm.exception;

public class CategoryAlreadyExistException extends ConflictException {
    public CategoryAlreadyExistException(String message) {
        super(message);
    }
}
