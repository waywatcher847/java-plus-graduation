package ru.practicum.ewm.stats.analyzer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kafka.consumer.event-similarity")
public record SimilaritySubscriberSettings(
        String bootstrapServers,
        String keyDeserializer,
        String valueDeserializer,
        String enableAutoCommit,
        String clientId,
        String groupId,
        String autoOffsetReset,
        String maxPollRecords,
        String maxPollInterval
) implements SubscriberSettings {
}