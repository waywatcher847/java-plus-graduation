package ru.practicum.ewm.compilation.services;

import ru.practicum.ewm.dto.compilation.CompilationDtoResponse;
import ru.practicum.ewm.dto.compilation.NewCompilationDto;
import ru.practicum.ewm.dto.compilation.UpdateCompilationRequest;

import org.springframework.data.domain.Pageable;
import java.util.List;

public interface CompilationService {
    CompilationDtoResponse create(NewCompilationDto dto);

    void delete(Long compId);

    CompilationDtoResponse patch(Long compId, UpdateCompilationRequest dto);

    List<CompilationDtoResponse> getCompilations(Boolean pinned, Pageable pageable);

    CompilationDtoResponse getById(Long compId);
}