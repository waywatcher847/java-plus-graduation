package ru.practicum.ewm.stats.base;

public class StatsServerNotFound extends RuntimeException {
    public StatsServerNotFound (String message, Throwable cause) {
        super(message, cause);
    }
}