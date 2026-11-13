package ru.practicum.ewm.dto.location;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LocationDto {

    private Float lat;

    private Float lon;
}
