package ru.practicum.ewm.stats.aggregator.config;

import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
@EnableConfigurationProperties({PublisherProperties.class, SubscriberProperties.class})
public class KafkaInfrastructureConfig {

    @Bean
    public Producer<Long, SpecificRecordBase> similarityPublisher(PublisherProperties props) {
        Properties config = new Properties();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, props.bootstrapServers());
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, props.keySerializer());
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, props.valueSerializer());
        config.put(ProducerConfig.CLIENT_ID_CONFIG, props.clientId());
        return new KafkaProducer<>(config);
    }

    @Bean
    public Consumer<Long, SpecificRecordBase> actionSubscriber(SubscriberProperties props) {
        Properties config = new Properties();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, props.bootstrapServers());
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, props.keyDeserializer());
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, props.valueDeserializer());
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, props.enableAutoCommit());
        config.put(ConsumerConfig.CLIENT_ID_CONFIG, props.clientId());
        config.put(ConsumerConfig.GROUP_ID_CONFIG, props.groupId());
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, props.autoOffsetReset());
        config.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, props.maxPollRecords());
        config.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, props.maxPollInterval());
        return new KafkaConsumer<>(config);
    }
}