package ru.practicum.ewm.event.mapper;


import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.dto.event.EventUpdateRequest;
import ru.practicum.ewm.dto.location.LocationDto;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.Location;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

class EventUpdater {
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private EventUpdater() {
    }

    static void apply(Event event, EventUpdateRequest dto, Category category) {
        if (dto.getAnnotation() != null) {
            event.setAnnotation(dto.getAnnotation());
        }
        if (category != null) {
            event.setCategory(category);
        }
        if (dto.getDescription() != null) {
            event.setDescription(dto.getDescription());
        }
        if (dto.getEventDate() != null) {
            event.setEventDate(LocalDateTime.parse(dto.getEventDate(), FORMATTER));
        }
        LocationDto location = dto.getLocation();
        if (location != null && location.getLat() != null && location.getLon() != null) {
            event.setLocation(new Location(location.getLat(), location.getLon()));
        }
        if (dto.getPaid() != null) {
            event.setPaid(dto.getPaid());
        }
        if (dto.getParticipantLimit() != null) {
            event.setParticipantLimit(dto.getParticipantLimit());
        }
        if (dto.getRequestModeration() != null) {
            event.setRequestModeration(dto.getRequestModeration());
        }
        if (dto.getTitle() != null) {
            event.setTitle(dto.getTitle());
        }
    }
}