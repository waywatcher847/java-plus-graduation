package ru.practicum.ewm.comment.service;

import ru.practicum.ewm.dto.comment.CommentDto;
import ru.practicum.ewm.dto.comment.NewCommentDto;
import ru.practicum.ewm.dto.comment.UpdateCommentDto;

import java.util.List;

public interface CommentService {

    CommentDto create(Long userId, Long eventId, NewCommentDto dto);

    CommentDto update(Long userId, Long commentId, UpdateCommentDto dto);

    void deleteUserComment(Long userId, Long commentId);

    void deleteByAdmin(Long commentId);

    List<CommentDto> getByUser(Long userId, Integer from, Integer size);

    List<CommentDto> getByEvent(Long eventId, Integer from, Integer size);

    CommentDto getOne(Long eventId, Long commentId);
}
