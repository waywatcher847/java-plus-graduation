package ru.practicum.ewm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import ch.qos.logback.classic.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication
public class EWMApplication {
    public static final String APP_NAME = "ewm-main-service";

    public static void main(String[] args) {
        SpringApplication.run(EWMApplication.class, args);
        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME))
                .setLevel(Level.INFO);
    }
}
