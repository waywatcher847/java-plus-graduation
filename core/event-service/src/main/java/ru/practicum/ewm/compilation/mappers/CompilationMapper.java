package ru.practicum.ewm.compilation.mappers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.compilation.models.CompilationEvent;
import ru.practicum.ewm.dto.compilation.CompilationDtoResponse;
import ru.practicum.ewm.dto.compilation.NewCompilationDto;
import ru.practicum.ewm.compilation.models.Compilation;
import ru.practicum.ewm.dto.user.UserShortDto;
import ru.practicum.ewm.event.mapper.EventMapper;
import ru.practicum.ewm.dto.ratings.RatingStatsDto;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.dto.compilation.CompilationContext;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CompilationMapper {

    public Compilation toModel(NewCompilationDto dto) {
        return Compilation.builder()
                .pinned(dto.getPinned() != null ? dto.getPinned() : false)
                .title(dto.getTitle())
                .build();
    }

    public CompilationDtoResponse toDto(Compilation model, CompilationContext context) {
        return mapCompilation(model, context);
    }

    public List<CompilationDtoResponse> toDtoList(List<Compilation> models, CompilationContext context) {
        return models.stream()
                .map(model -> mapCompilation(model, context))
                .toList();
    }

    private CompilationDtoResponse mapCompilation(Compilation model, CompilationContext context) {
        List<Event> events = model.getEvents() != null ? model.getEvents().stream()
                .map(CompilationEvent::getEvent)
                .toList() : List.of();

        return CompilationDtoResponse.builder()
                .id(model.getId())
                .pinned(model.getPinned())
                .title(model.getTitle())
                .events(events.stream().map(event -> {
                    UserShortDto initiator = context.getInitiators().getOrDefault(
                            event.getInitiatorId(),
                            new UserShortDto(event.getInitiatorId(), null)
                    );
                    return EventMapper.toEventShortDto(
                            event,
                            context.getViews().getOrDefault(event.getId(), 0L),
                            context.getConfirmed().getOrDefault(event.getId(), 0L),
                            context.getRatings().getOrDefault(event.getId(), RatingStatsDto.EMPTY),
                            context.getComments().getOrDefault(event.getId(), 0L),
                            initiator
                    );
                }).toList())
                .build();
    }
}
