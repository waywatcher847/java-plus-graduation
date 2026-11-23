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
import ru.practicum.ewm.stats.analyzer.entity.EventSimilarity;
import ru.practicum.ewm.stats.analyzer.repository.EventSimilarityRepository;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class SimilarityHandler implements Runnable {
    private static final Duration POLL_TIMEOUT = Duration.ofMillis(5000);

    private final Consumer<Long, SpecificRecordBase> subscriber;
    private final EventSimilarityRepository repository;

    @Value("${kafka.topics.event-similarity}")
    private String similarityTopic;

    public SimilarityHandler(@Qualifier("similaritySubscriber") Consumer<Long, SpecificRecordBase> subscriber,
                             EventSimilarityRepository repository) {
        this.subscriber = subscriber;
        this.repository = repository;
    }

    @Override
    public void run() {
        Runtime.getRuntime().addShutdownHook(new Thread(subscriber::wakeup));
        try {
            subscriber.subscribe(List.of(similarityTopic));
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
        if (!(payload instanceof EventSimilarityAvro similarity)) {
            log.warn("Неподдерживаемый тип: {}", payload.getClass());
            registerOffset(record, offsets);
            return;
        }
        if (similarity.getEventA() <= 0 || similarity.getEventB() <= 0 || similarity.getScore() <= 0) {
            log.warn("Невалидное сходство: {}", similarity);
            registerOffset(record, offsets);
            return;
        }
        EventSimilarity entity = EventSimilarity.builder()
                .eventA(similarity.getEventA())
                .eventB(similarity.getEventB())
                .score(similarity.getScore())
                .timestamp(similarity.getTimestamp())
                .build();
        repository.upsertSimilarity(
                entity.getEventA(),
                entity.getEventB(),
                entity.getScore(),
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