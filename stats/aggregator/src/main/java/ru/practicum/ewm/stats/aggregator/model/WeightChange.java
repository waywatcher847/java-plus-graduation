package ru.practicum.ewm.stats.aggregator.model;

public record WeightChange(double previous, double current) {

    public double delta() {
        return current - previous;
    }
}