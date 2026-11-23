package ru.practicum.ewm.stats.analyzer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kafka.consumer.user-action")
public record ActionSubscriberSettings(
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