package ru.practicum.ewm.comment.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.ewm.dto.comment.CommentDto;
import ru.practicum.ewm.dto.comment.NewCommentDto;
import ru.practicum.ewm.comment.model.Comment;
import ru.practicum.ewm.client.event.EventData;

import java.time.LocalDateTime;

@Component
public class CommentMapper {

    public Comment toComment(NewCommentDto dto, EventData event, Long authorId) {
        return Comment.builder()
                .text(dto.getText())
                .eventId(event.getId())
                .authorId(authorId)
                .created(LocalDateTime.now())
                .build();
    }

    public CommentDto toCommentDto(Comment comment, String authorName) {
        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .authorId(comment.getAuthorId())
                .authorName(authorName)
                .eventId(comment.getEventId())
                .created(comment.getCreated())
                .updated(comment.getUpdated())
                .build();
    }
}
