package ru.practicum.ewm.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

//@SpringBootApplication(scanBasePackages = {"ru.practicum.ewm.user", "ru.practicum.ewm.client", "ru.practicum.ewm.handler"})
@SpringBootApplication
@EnableFeignClients(basePackages = "ru.practicum.ewm.client")
public class UserApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserApplication.class, args);
    }
}
