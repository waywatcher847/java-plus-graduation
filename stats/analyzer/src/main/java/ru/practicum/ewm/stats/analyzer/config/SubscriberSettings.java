package ru.practicum.ewm.stats.analyzer.config;

public interface SubscriberSettings {
    String bootstrapServers();
    String keyDeserializer();
    String valueDeserializer();
    String enableAutoCommit();
    String clientId();
    String groupId();
    String autoOffsetReset();
    String maxPollRecords();
    String maxPollInterval();
}