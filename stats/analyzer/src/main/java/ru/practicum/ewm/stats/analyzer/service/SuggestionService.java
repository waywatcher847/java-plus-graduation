package ru.practicum.ewm.stats.analyzer.service;

import ru.practicum.grpc.stats.service.dashboard.*;

import java.util.List;

public interface SuggestionService {
    List<RecommendedEventProto> fetchUserPredictions(UserPredictionsRequestProto request);
    List<RecommendedEventProto> fetchSimilarEvents(SimilarEventsRequestProto request);
    List<RecommendedEventProto> fetchInteractionsCount(InteractionsCountRequestProto request);
    boolean hasUserViewedEvent(long userId, long eventId);
}