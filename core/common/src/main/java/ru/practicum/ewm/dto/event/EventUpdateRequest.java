package ru.practicum.ewm.dto.event;

import ru.practicum.ewm.dto.location.LocationDto;

public interface EventUpdateRequest {
    String getAnnotation();

    Long getCategory();

    String getDescription();

    String getEventDate();

    LocationDto getLocation();

    Boolean getPaid();

    Integer getParticipantLimit();

    Boolean getRequestModeration();

    String getTitle();
}