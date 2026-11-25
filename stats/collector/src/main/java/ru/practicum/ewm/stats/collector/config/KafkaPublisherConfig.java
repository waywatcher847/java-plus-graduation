package ru.practicum.ewm.stats.collector.config;

import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
@EnableConfigurationProperties(KafkaPublisherProperties.class)
public class KafkaPublisherConfig {

    @Bean
    public Producer<Long, SpecificRecordBase> actionPublisher(KafkaPublisherProperties props) {
        Properties config = new Properties();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, props.bootstrapServers());
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, props.keySerializer());
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, props.valueSerializer());
        config.put(ProducerConfig.CLIENT_ID_CONFIG, props.clientId());
        return new KafkaProducer<>(config);
    }
}