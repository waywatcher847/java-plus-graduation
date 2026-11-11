package ru.practicum.ewm.request.service;

import ru.practicum.ewm.dto.event.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.dto.event.EventRequestStatusUpdateResult;
import ru.practicum.ewm.dto.request.ParticipationRequestDto;

import java.util.List;

public interface RequestService {

    ParticipationRequestDto create(Long userId, Long eventId);

    List<ParticipationRequestDto> getAllByUser(Long userId);

    ParticipationRequestDto cancel(Long userId, Long requestId);

    List<ParticipationRequestDto> getByEvent(Long userId, Long eventId);

    EventRequestStatusUpdateResult changeStatus(Long userId, Long eventId,
                                                  EventRequestStatusUpdateRequest dto);
}
