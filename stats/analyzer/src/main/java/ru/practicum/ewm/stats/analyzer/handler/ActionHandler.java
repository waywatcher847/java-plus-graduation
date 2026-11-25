package ru.practicum.ewm.stats.analyzer.handler;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.analyzer.entity.UserAction;
import ru.practicum.ewm.stats.analyzer.repository.UserActionRepository;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class ActionHandler implements Runnable {
    private static final Duration POLL_TIMEOUT = Duration.ofMillis(5000);
    private static final Map<ActionTypeAvro, Double> ACTION_WEIGHTS = Map.of(
            ActionTypeAvro.VIEW, 0.4,
            ActionTypeAvro.REGISTER, 0.8,
            ActionTypeAvro.LIKE, 1.0
    );

    private final Consumer<Long, SpecificRecordBase> subscriber;
    private final UserActionRepository repository;

    @Value("${kafka.topics.user-actions}")
    private String actionsTopic;

    public ActionHandler(@Qualifier("actionSubscriber") Consumer<Long, SpecificRecordBase> subscriber,
                         UserActionRepository repository) {
        this.subscriber = subscriber;
        this.repository = repository;
    }

    @Override
    public void run() {
        Runtime.getRuntime().addShutdownHook(new Thread(subscriber::wakeup));
        try {
            subscriber.subscribe(List.of(actionsTopic));
            while (true) {
                ConsumerRecords<Long, SpecificRecordBase> batch = subscriber.poll(POLL_TIMEOUT);
                Map<TopicPartition, OffsetAndMetadata> offsets = new HashMap<>();
                for (ConsumerRecord<Long, SpecificRecordBase> record : batch) {
                    try {
                        handle(record, offsets);
                    } catch (Exception ex) {
                        log.error("Ошибка обработки offset={}, key={}", record.offset(), record.key(), ex);
                        break;
                    }
                }
                if (!offsets.isEmpty()) {
                    subscriber.commitSync(offsets);
                }
            }
        } catch (WakeupException ex) {
            log.debug("Поток разбужен", ex);
        } finally {
            subscriber.close();
        }
    }

    private void handle(ConsumerRecord<Long, SpecificRecordBase> record, Map<TopicPartition, OffsetAndMetadata> offsets) {
        SpecificRecordBase payload = record.value();
        if (payload == null) {
            log.warn("Пустое сообщение offset={}", record.offset());
            registerOffset(record, offsets);
            return;
        }
        if (!(payload instanceof UserActionAvro action)) {
            log.warn("Неподдерживаемый тип: {}", payload.getClass());
            registerOffset(record, offsets);
            return;
        }
        if (action.getUserId() <= 0 || action.getEventId() <= 0 || action.getActionType() == null) {
            log.warn("Невалидное действие: {}", action);
            registerOffset(record, offsets);
            return;
        }
        Double weight = ACTION_WEIGHTS.get(action.getActionType());
        if (weight == null) {
            registerOffset(record, offsets);
            return;
        }
        UserAction entity = UserAction.builder()
                .userId(action.getUserId())
                .eventId(action.getEventId())
                .rating(weight)
                .timestamp(action.getTimestamp())
                .build();
        repository.upsertAction(
                entity.getUserId(),
                entity.getEventId(),
                entity.getRating(),
                entity.getTimestamp()
        );
        registerOffset(record, offsets);
    }

    private void registerOffset(ConsumerRecord<Long, SpecificRecordBase> record, Map<TopicPartition, OffsetAndMetadata> offsets) {
        offsets.put(
                new TopicPartition(record.topic(), record.partition()),
                new OffsetAndMetadata(record.offset() + 1)
        );
    }
}