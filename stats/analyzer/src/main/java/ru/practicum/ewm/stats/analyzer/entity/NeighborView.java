package ru.practicum.ewm.stats.analyzer.entity;

public interface NeighborView {
    long getEventId();
    double getRating();
    double getSimilarity();
}