package ru.practicum.ewm.stats.analyzer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import ru.practicum.ewm.stats.analyzer.handler.SimilarityHandler;
import ru.practicum.ewm.stats.analyzer.handler.ActionHandler;

@SpringBootApplication
public class InsightServiceApp {
    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = SpringApplication.run(InsightServiceApp.class, args);
        Thread similarityThread = new Thread(ctx.getBean(SimilarityHandler.class));
        similarityThread.setName("similarity-handler");
        similarityThread.start();

        Thread actionThread = new Thread(ctx.getBean(ActionHandler.class));
        actionThread.setName("action-handler");
        actionThread.start();
    }
}