package ru.practicum.ewm.dto.comment;

import java.time.LocalDateTime;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentDto {
    private Long id;
    private String text;
    private Long authorId;
    private String authorName;
    private Long eventId;
    private LocalDateTime created;
    private LocalDateTime updated;
}
