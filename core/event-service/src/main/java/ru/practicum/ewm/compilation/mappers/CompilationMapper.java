package ru.practicum.ewm.compilation.mappers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.client.socials.SocialsReader;
import ru.practicum.ewm.common.util.EventUtil;
import ru.practicum.ewm.dto.compilation.CompilationDtoResponse;
import ru.practicum.ewm.dto.compilation.NewCompilationDto;
import ru.practicum.ewm.compilation.models.Compilation;
import ru.practicum.ewm.event.mapper.EventMapper;
import ru.practicum.ewm.dto.ratings.RatingStatsDto;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CompilationMapper {
    private final EventUtil eventUtil;
    private final SocialsReader socialsReader;

    public Compilation toModel(NewCompilationDto dto) {
        return Compilation.builder().pinned(dto.getPinned()).title(dto.getTitle()).build();
    }

    public CompilationDtoResponse toDto(Compilation model) {
        return toDtos(List.of(model)).getFirst();
    }

    public List<CompilationDtoResponse> toDtos(List<Compilation> models) {
        var ids = models.stream().flatMap(model -> model.getEvents().stream())
                .map(link -> link.getEvent().getId()).distinct().toList();
        var aggregatedSocials = socialsReader.aggregate(ids, null);
        var views = eventUtil.views(ids);
        var confirmed = eventUtil.confirmed(ids);
        return models.stream().map(model -> CompilationDtoResponse.builder()
                .id(model.getId()).pinned(model.getPinned()).title(model.getTitle())
                .events(model.getEvents().stream().map(link -> {
                    var event = link.getEvent();
                    return EventMapper.toEventShortDto(event, views.getOrDefault(event.getId(), 0L),
                            confirmed.getOrDefault(event.getId(), 0L),
                            aggregatedSocials.getRatings().getOrDefault(event.getId(), RatingStatsDto.EMPTY),
                            aggregatedSocials.getComments().getOrDefault(event.getId(), 0L));
                }).toList()).build()).toList();
    }
}
