package ru.practicum.ewm.exception;

public class CommentNotFoundException extends NotFoundException {
    public CommentNotFoundException(Long id) {
        super("Comment with ID = %d not found.".formatted(id));
    }
}
