package ru.practicum.ewm.stats.collector.mapper;

import java.util.Arrays;
import java.util.Optional;

public class EnumMapper {
    public static <T extends Enum<T>> Optional<T> toAppEnum(T[] enumValues, String protoName) {
        if (protoName == null) {
            return Optional.empty();
        }

        String formattedName = protoName;
        int underscoreIndex = protoName.indexOf('_');
        if (underscoreIndex != -1 && underscoreIndex < protoName.length() - 1) {
            formattedName = protoName.substring(underscoreIndex + 1);
        }

        final String targetName = formattedName.toUpperCase();

        return Arrays.stream(enumValues)
                .filter(type -> type.name().equalsIgnoreCase(targetName))
                .findFirst();
    }
}
