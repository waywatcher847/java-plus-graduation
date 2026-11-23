package ru.practicum.ewm.stats.aggregator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.aggregator.service.EventComparator;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class AggregatorEngine implements Runnable {

    private final EventComparator calculator;
    private final Consumer<Long, SpecificRecordBase> subscriber;
    private final Producer<Long, SpecificRecordBase> publisher;

    private static final Duration POLL_TIMEOUT = Duration.ofMillis(5000);

    @Value("${kafka.topics.user-actions}")
    private String actionsTopic;

    @Value("${kafka.topics.event-similarity}")
    private String similarityTopic;

    @Override
    public void run() {
        Runtime.getRuntime().addShutdownHook(new Thread(subscriber::wakeup));
        try {
            subscriber.subscribe(List.of(actionsTopic));
            while (true) {
                ConsumerRecords<Long, SpecificRecordBase> batch = subscriber.poll(POLL_TIMEOUT);
                Map<TopicPartition, OffsetAndMetadata> pendingOffsets = new HashMap<>();

                for (ConsumerRecord<Long, SpecificRecordBase> record : batch) {
                    try {
                        processRecord(record, pendingOffsets);
                    } catch (Exception ex) {
                        log.error("Ошибка обработки записи offset={}, key={}", record.offset(), record.key(), ex);
                        break;
                    }
                }
                if (!pendingOffsets.isEmpty()) {
                    subscriber.commitSync(pendingOffsets);
                }
            }
        } catch (WakeupException ex) {
            log.debug("Поток разбужен сигналом остановки", ex);
        } finally {
            subscriber.close();
            publisher.close();
        }
    }

    private void processRecord(ConsumerRecord<Long, SpecificRecordBase> record,
                               Map<TopicPartition, OffsetAndMetadata> offsets) {
        SpecificRecordBase payload = record.value();
        if (payload == null) {
            log.warn("Пустое сообщение в offset={}", record.offset());
            registerOffset(record, offsets);
            return;
        }
        if (!(payload instanceof UserActionAvro action)) {
            log.warn("Неподдерживаемый тип записи: {}", payload.getClass());
            registerOffset(record, offsets);
            return;
        }
        if (action.getUserId() <= 0 || action.getEventId() <= 0 || action.getActionType() == null) {
            log.warn("Невалидные данные действия: {}", action);
            registerOffset(record, offsets);
            return;
        }

        List<EventSimilarityAvro> similarities = calculator.calculate(action);
        for (EventSimilarityAvro similarity : similarities) {
            ProducerRecord<Long, SpecificRecordBase> outgoing =
                    new ProducerRecord<>(similarityTopic, similarity.getEventA(), similarity);
            publisher.send(outgoing);
        }
        registerOffset(record, offsets);
    }

    private void registerOffset(ConsumerRecord<Long, SpecificRecordBase> record,
                                Map<TopicPartition, OffsetAndMetadata> offsets) {
        offsets.put(
                new TopicPartition(record.topic(), record.partition()),
                new OffsetAndMetadata(record.offset() + 1)
        );
    }
}