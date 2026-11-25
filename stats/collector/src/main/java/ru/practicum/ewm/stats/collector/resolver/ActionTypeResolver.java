package ru.practicum.ewm.stats.collector.resolver;

import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.grpc.stats.service.collector.ActionTypeProto;

import java.util.Map;

public final class ActionTypeResolver {

    private static final Map<ActionTypeProto, ActionTypeAvro> MAPPING = Map.of(
            ActionTypeProto.ACTION_VIEW, ActionTypeAvro.VIEW,
            ActionTypeProto.ACTION_REGISTER, ActionTypeAvro.REGISTER,
            ActionTypeProto.ACTION_LIKE, ActionTypeAvro.LIKE
    );

    private ActionTypeResolver() {
    }

    public static ActionTypeAvro resolve(ActionTypeProto proto) {
        ActionTypeAvro type = MAPPING.get(proto);
        if (type == null) {
            throw new IllegalArgumentException("Неизвестный тип действия: " + proto);
        }
        return type;
    }
}