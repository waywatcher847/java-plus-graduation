package ru.practicum.ewm.stats.aggregator.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.aggregator.model.EventPair;
import ru.practicum.ewm.stats.aggregator.model.WeightChange;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class EventComparatorImpl implements EventComparator {

    private static final int MIN_EVENTS_FOR_SIMILARITY = 2;

    private static final Map<ActionTypeAvro, Double> ACTION_WEIGHTS = Map.of(
            ActionTypeAvro.VIEW, 0.4,
            ActionTypeAvro.REGISTER, 0.8,
            ActionTypeAvro.LIKE, 1.0
    );

    // userId -> набор eventId, с которыми он взаимодействовал
    private final Map<Long, Set<Long>> userEvents = new ConcurrentHashMap<>();
    // eventId -> (userId -> максимальный вес взаимодействия)
    private final Map<Long, ConcurrentHashMap<Long, Double>> eventUserWeights = new ConcurrentHashMap<>();
    // eventId -> суммарный вес всех взаимодействий
    private final Map<Long, Double> eventTotalWeights = new ConcurrentHashMap<>();
    // (eventA, eventB) -> сумма минимумов весов общих пользователей
    private final Map<EventPair, Double> pairMinWeightSums = new ConcurrentHashMap<>();

    @Override
    public List<EventSimilarityAvro> calculate(UserActionAvro action) {
        log.trace("Расчёт сходства для действия {}", action);

        Double actionWeight = ACTION_WEIGHTS.get(action.getActionType());
        if (actionWeight == null) {
            return List.of();
        }

        Set<Long> userSet = userEvents
                .computeIfAbsent(action.getUserId(), id -> ConcurrentHashMap.newKeySet());
        userSet.add(action.getEventId());

        WeightChange change = updateEventWeight(action, actionWeight);
        if (change.delta() == 0.0) {
            return List.of();
        }
        eventTotalWeights.merge(action.getEventId(), change.delta(), Double::sum);

        if (userSet.size() < MIN_EVENTS_FOR_SIMILARITY) {
            return List.of();
        }

        return buildSimilarities(action, userSet, change);
    }

    private WeightChange updateEventWeight(UserActionAvro action, double actionWeight) {
        double[] previous = {0.0};
        double updated = eventUserWeights
                .computeIfAbsent(action.getEventId(), id -> new ConcurrentHashMap<>())
                .merge(action.getUserId(), actionWeight, (oldVal, newVal) -> {
                    previous[0] = oldVal;
                    return Math.max(oldVal, newVal);
                });
        return new WeightChange(previous[0], updated);
    }

    private List<EventSimilarityAvro> buildSimilarities(UserActionAvro action,
                                                        Set<Long> userSet,
                                                        WeightChange change) {
        List<EventSimilarityAvro> result = new ArrayList<>();
        long targetEvent = action.getEventId();

        for (Long otherEvent : userSet) {
            if (otherEvent.equals(targetEvent)) {
                continue;
            }
            EventPair pair = EventPair.ordered(targetEvent, otherEvent);
            updatePairMinWeightSum(pair, action, change);
            double score = computeScore(pair);
            log.debug("Сходство пары {} = {}", pair, score);
            result.add(EventSimilarityAvro.newBuilder()
                    .setEventA(pair.eventA())
                    .setEventB(pair.eventB())
                    .setScore(score)
                    .setTimestamp(Instant.now())
                    .build());
        }
        return result;
    }

    private void updatePairMinWeightSum(EventPair pair, UserActionAvro action, WeightChange change) {
        long counterpart = pair.counterpartOf(action.getEventId());
        double counterpartWeight = eventUserWeights
                .getOrDefault(counterpart, new ConcurrentHashMap<>())
                .getOrDefault(action.getUserId(), 0.0);

        double increment = Math.min(change.current(), counterpartWeight)
                - Math.min(change.previous(), counterpartWeight);
        if (increment != 0.0) {
            pairMinWeightSums.merge(pair, increment, Double::sum);
        }
    }

    private double computeScore(EventPair pair) {
        double minSum = pairMinWeightSums.getOrDefault(pair, 0.0);
        double totalA = eventTotalWeights.getOrDefault(pair.eventA(), 0.0);
        double totalB = eventTotalWeights.getOrDefault(pair.eventB(), 0.0);
        if (totalA == 0.0 || totalB == 0.0) {
            return 0.0;
        }
        return minSum / Math.sqrt(totalA * totalB);
    }
}