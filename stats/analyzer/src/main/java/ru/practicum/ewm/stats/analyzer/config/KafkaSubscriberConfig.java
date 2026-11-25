package ru.practicum.ewm.stats.analyzer.config;

import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
@EnableConfigurationProperties({ActionSubscriberSettings.class, SimilaritySubscriberSettings.class})
public class KafkaSubscriberConfig {

    @Bean("actionSubscriber")
    public Consumer<Long, SpecificRecordBase> actionSubscriber(ActionSubscriberSettings settings) {
        return new KafkaConsumer<>(buildProperties(settings));
    }

    @Bean("similaritySubscriber")
    public Consumer<Long, SpecificRecordBase> similaritySubscriber(SimilaritySubscriberSettings settings) {
        return new KafkaConsumer<>(buildProperties(settings));
    }

    private Properties buildProperties(SubscriberSettings settings) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, settings.bootstrapServers());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, settings.keyDeserializer());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, settings.valueDeserializer());
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, settings.enableAutoCommit());
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, settings.clientId());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, settings.groupId());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, settings.autoOffsetReset());
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, settings.maxPollRecords());
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, settings.maxPollInterval());
        return props;
    }
}