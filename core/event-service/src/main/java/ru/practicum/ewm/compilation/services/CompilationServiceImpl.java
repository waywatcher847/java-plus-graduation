package ru.practicum.ewm.compilation.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.client.AnalyzerGrpcClient;
import ru.practicum.ewm.client.socials.SocialsAggregated;
import ru.practicum.ewm.client.socials.SocialsReader;
import ru.practicum.ewm.client.user.UserReader;
import ru.practicum.ewm.common.util.EventUtil;
import ru.practicum.ewm.dto.compilation.CompilationContext;
import ru.practicum.ewm.dto.compilation.CompilationDtoResponse;
import ru.practicum.ewm.dto.compilation.NewCompilationDto;
import ru.practicum.ewm.dto.compilation.UpdateCompilationRequest;
import ru.practicum.ewm.compilation.mappers.CompilationMapper;
import ru.practicum.ewm.compilation.models.Compilation;
import ru.practicum.ewm.compilation.models.CompilationEvent;
import ru.practicum.ewm.compilation.repository.CompilationRepository;
import ru.practicum.ewm.dto.user.UserShortDto;
import ru.practicum.ewm.exception.CompilationNotFoundException;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.repository.EventRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompilationServiceImpl implements CompilationService {
    private final EventRepository eventRepository;
    private final CompilationRepository compilationRepository;
    private final CompilationMapper mapper;
    private final EventUtil eventUtil;
    private final SocialsReader socialsReader;
    private final UserReader userReader;
    private final AnalyzerGrpcClient analyzerClient;

    @Override
    @Transactional
    public CompilationDtoResponse create(NewCompilationDto dto) {
        log.debug("CompilationService->create: {}", dto);
        List<Event> events = dto.getEvents() != null ? eventRepository.findByIdIn(dto.getEvents()) : List.of();

        Compilation compilation = mapper.toModel(dto);
        if (compilation.getEvents() == null) {
            compilation.setEvents(new ArrayList<>());
        }
        createCompilationEvents(compilation, events);

        Compilation result = compilationRepository.save(compilation);
        CompilationContext context = fetchContext(List.of(result));

        log.debug("CompilationService->create result: {}", result);
        return mapper.toDto(result, context);
    }

    @Override
    @Transactional
    public void delete(Long compId) {
        log.debug("CompilationService->delete id={}", compId);
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new CompilationNotFoundException(compId));
        compilationRepository.delete(compilation);
    }

    @Override
    @Transactional
    public CompilationDtoResponse patch(Long compId, UpdateCompilationRequest dto) {
        log.debug("CompilationService->patch id={}. {}", compId, dto);
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(()
                        -> new CompilationNotFoundException(compId));

        if (dto.getEvents() != null) {
            compilation.getEvents().clear();
            compilationRepository.flush();
            List<Event> events = eventRepository
                    .findByIdIn(dto.getEvents());
            createCompilationEvents(compilation, events);
        }

        if (dto.getPinned() != null) {
            compilation.setPinned(dto.getPinned());
        }

        if (dto.getTitle() != null && !dto.getTitle().trim().isEmpty()) {
            compilation.setTitle(dto.getTitle());
        }

        Compilation result = compilationRepository.save(compilation);
        CompilationContext context = fetchContext(List.of(result));
        return mapper.toDto(result, context);
    }

    @Override
    public List<CompilationDtoResponse> getCompilations(Boolean pinned, Pageable pageable) {
        log.debug("CompilationService->get pinned={}, pageable={}", pinned, pageable);
        List<Compilation> models = compilationRepository.findWithOffset(pinned, pageable);

        if (models.isEmpty()) {
            return List.of();
        }
        CompilationContext context = fetchContext(models);
        return mapper.toDtoList(models, context);
    }


        @Override
    public CompilationDtoResponse getById(Long compId) {
            log.debug("CompilationService->getById id={}", compId);
            Compilation result = compilationRepository.findById(compId)
                    .orElseThrow(() -> new CompilationNotFoundException(compId));
            CompilationContext context = fetchContext(List.of(result));
            return mapper.toDto(result, context);
        }

    private void createCompilationEvents(Compilation compilation, List<Event> events) {
        events.forEach(event -> compilation.getEvents().add(
                CompilationEvent.builder()
                        .compilation(compilation)
                        .event(event)
                        .build()
        ));
    }

    private CompilationContext fetchContext(List<Compilation> compilations) {
        List<Long> eventIds = compilations.stream()
                .flatMap(compilation -> compilation.getEvents() != null ? compilation.getEvents().stream() : Stream.empty())
                .map(link -> link.getEvent().getId())
                .distinct()
                .toList();
        Set<Long> initiatorIds = compilations.stream()
                .flatMap(compilation -> compilation.getEvents() != null ? compilation.getEvents().stream() : Stream.empty())
                .map(link -> link.getEvent().getInitiatorId())
                .collect(Collectors.toSet());

        if (eventIds.isEmpty()) {
            return CompilationContext.builder()
                    .ratings(Map.of())
                    .confirmed(Map.of())
                    .ratingStats(Map.of())
                    .comments(Map.of())
                    .initiators(Map.of())
                    .build();
        }

        SocialsAggregated aggregatedSocials = socialsReader.aggregate(eventIds, null);
        Map<Long, Long> confirmed = eventUtil.confirmed(eventIds);

        Map<Long, Double> ratings = eventIds.stream()
                .collect(Collectors.toMap(
                        id -> id,
                        id -> analyzerClient.getEventRating(id),
                        (a, b) -> a
                ));

        Map<Long, UserShortDto> initiators = userReader.findShortByIds(initiatorIds);

        return CompilationContext.builder()
                .ratings(ratings)
                .confirmed(confirmed)
                .ratingStats(aggregatedSocials.getRatings())
                .comments(aggregatedSocials.getComments())
                .initiators(initiators)
                .build();
    }
}