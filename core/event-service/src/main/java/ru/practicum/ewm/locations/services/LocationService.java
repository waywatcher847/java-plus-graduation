package ru.practicum.ewm.locations.services;

import org.springframework.data.domain.Pageable;
import ru.practicum.ewm.dto.event.EventShortDto;
import ru.practicum.ewm.dto.location.LocationDtoResponse;
import ru.practicum.ewm.dto.location.LocationSearchRequest;
import ru.practicum.ewm.dto.location.NewLocationDto;
import ru.practicum.ewm.dto.location.UpdateLocationRequest;

import java.util.List;

public interface LocationService {
    LocationDtoResponse create(NewLocationDto dto);

    LocationDtoResponse patch(Long locId, UpdateLocationRequest dto);

    void delete(Long locId);

    List<LocationDtoResponse> getAll(Pageable pageable);

    LocationDtoResponse getById(Long locId);

    List<EventShortDto> getEventsInLocation(LocationSearchRequest request);
}