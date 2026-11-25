package ru.practicum.ewm.stats.aggregator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class AggregatorApp {
    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = SpringApplication.run(AggregatorApp.class, args);
        AggregatorEngine engine = ctx.getBean(AggregatorEngine.class);
        engine.run();
    }
}