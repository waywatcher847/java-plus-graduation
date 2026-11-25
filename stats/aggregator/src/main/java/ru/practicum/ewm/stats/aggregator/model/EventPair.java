package ru.practicum.ewm.stats.aggregator.model;

public record EventPair(long eventA, long eventB) {

    public static EventPair ordered(long a, long b) {
        return a <= b ? new EventPair(a, b) : new EventPair(b, a);
    }

    public long counterpartOf(long eventId) {
        return eventA == eventId ? eventB : eventA;
    }
}