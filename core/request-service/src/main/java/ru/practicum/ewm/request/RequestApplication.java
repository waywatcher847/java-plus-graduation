package ru.practicum.ewm.request;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

//@SpringBootApplication(scanBasePackages = {"ru.practicum.ewm.request", "ru.practicum.ewm.client", "ru.practicum.ewm.handler"})
@SpringBootApplication
@EnableFeignClients(basePackages = "ru.practicum.ewm.client")
public class RequestApplication {
    public static void main(String[] args) {
        SpringApplication.run(RequestApplication.class, args);
    }
}
