package ru.practicum.ewm.stats.collector.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kafka.producer")
public record KafkaPublisherProperties(
        String bootstrapServers,
        String keySerializer,
        String valueSerializer,
        String clientId
) {
}