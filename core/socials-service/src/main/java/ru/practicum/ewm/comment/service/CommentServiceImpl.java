package ru.practicum.ewm.comment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.comment.CommentDto;
import ru.practicum.ewm.dto.comment.NewCommentDto;
import ru.practicum.ewm.dto.comment.UpdateCommentDto;
import ru.practicum.ewm.dto.user.UserDto;
import ru.practicum.ewm.exception.CommentNotFoundException;
import ru.practicum.ewm.comment.mapper.CommentMapper;
import ru.practicum.ewm.comment.model.Comment;
import ru.practicum.ewm.comment.repository.CommentRepository;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.client.event.EventData;
import ru.practicum.ewm.model.EventState;
import ru.practicum.ewm.client.event.EventReader;
import ru.practicum.ewm.client.user.UserReader;
import ru.practicum.ewm.exception.UserNotFoundException;
import ru.practicum.ewm.exception.EventNotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final UserReader userRepository;
    private final EventReader eventRepository;
    private final CommentMapper mapper;

    @Override
    @Transactional
    public CommentDto create(Long userId, Long eventId, NewCommentDto dto) {
        UserDto author = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        EventData event = eventRepository.findByIdAndState(eventId, EventState.PUBLISHED)
                .orElseThrow(() -> new EventNotFoundException(eventId));

        Comment comment = mapper.toComment(dto, event, author.getId());
        comment = commentRepository.save(comment);

        log.debug("Comment created: {} by user {} on event {}", comment.getId(), userId, eventId);
        return mapper.toCommentDto(comment, author.getName());
    }

    @Override
    @Transactional
    public CommentDto update(Long userId, Long commentId, UpdateCommentDto dto) {
        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException(commentId));
        if (!comment.getAuthorId().equals(userId)) {
            throw new ConflictException("Only the author can edit the comment");
        }
        comment.setText(dto.getText());
        comment.setUpdated(LocalDateTime.now());
        comment = commentRepository.save(comment);

        String authorName = userRepository.findById(comment.getAuthorId())
                .map(UserDto::getName)
                .orElse(null);

        return mapper.toCommentDto(comment, authorName);
    }

    @Override
    @Transactional
    public void deleteUserComment(Long userId, Long commentId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException(commentId));
        if (!comment.getAuthorId().equals(userId)) {
            throw new ConflictException("Only the author can delete the comment");
        }
        commentRepository.delete(comment);
        log.debug("Comment deleted by author: {}", commentId);
    }

    @Override
    @Transactional
    public void deleteByAdmin(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException(commentId));
        commentRepository.delete(comment);
        log.debug("Comment deleted by admin: {}", commentId);
    }

    @Override
    public List<CommentDto> getByUser(Long userId, Integer from, Integer size) {
        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        PageRequest page = PageRequest.of(from / size, size);
        List<Comment> comments = commentRepository.findByAuthorIdOrderByCreatedDesc(userId, page).getContent();

        Set<Long> authorIds = comments.stream().map(Comment::getAuthorId).collect(Collectors.toSet());
        Map<Long, UserDto> authors = userRepository.findByIds(authorIds);

        return comments.stream()
                .map(c -> mapper.toCommentDto(c,
                        authors.getOrDefault(c.getAuthorId(), new UserDto(c.getAuthorId(), null, null)).getName()))
                .collect(Collectors.toList());
    }

    @Override
    public List<CommentDto> getByEvent(Long eventId, Integer from, Integer size) {
        eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));
        PageRequest page = PageRequest.of(from / size, size);
        List<Comment> comments = commentRepository.findByEventIdOrderByCreatedDesc(eventId, page).getContent();

        Set<Long> authorIds = comments.stream().map(Comment::getAuthorId).collect(Collectors.toSet());
        Map<Long, UserDto> authors = userRepository.findByIds(authorIds);

        return comments.stream()
                .map(c -> mapper.toCommentDto(c,
                        authors.getOrDefault(c.getAuthorId(), new UserDto(c.getAuthorId(), null, null)).getName()))
                .collect(Collectors.toList());
    }

    @Override
    public CommentDto getOne(Long eventId, Long commentId) {
        eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException(commentId));
        if (!comment.getEventId().equals(eventId)) {
            throw new CommentNotFoundException(commentId);
        }

        String authorName = userRepository.findById(comment.getAuthorId())
                .map(UserDto::getName)
                .orElse(null);

        return mapper.toCommentDto(comment, authorName);
    }
}
