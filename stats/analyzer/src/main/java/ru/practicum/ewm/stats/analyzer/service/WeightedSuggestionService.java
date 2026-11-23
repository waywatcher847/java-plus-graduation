package ru.practicum.ewm.stats.analyzer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.analyzer.entity.*;
import ru.practicum.ewm.stats.analyzer.repository.EventSimilarityRepository;
import ru.practicum.ewm.stats.analyzer.repository.UserActionRepository;
import ru.practicum.grpc.stats.service.dashboard.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WeightedSuggestionService implements SuggestionService {

    private static final int NEIGHBOR_COUNT = 5;

    private final EventSimilarityRepository similarityRepository;
    private final UserActionRepository actionRepository;

    @Override
    public boolean hasUserViewedEvent(long userId, long eventId) {
        log.trace("Проверка просмотра eventId={} пользователем {}", eventId, userId);
        boolean viewed = actionRepository.findInteractedEvents(userId).contains(eventId);
        log.debug("Пользователь {} {} событие {}", userId, viewed ? "просматривал" : "не просматривал", eventId);
        return viewed;
    }

    @Override
    public List<RecommendedEventProto> fetchUserPredictions(UserPredictionsRequestProto request) {
        log.trace("Получение рекомендаций для пользователя {}", request.getUserId());
        List<Long> recentEvents = actionRepository.findRecentEvents(request.getUserId(), request.getMaxResults());
        if (recentEvents.isEmpty()) {
            return List.of();
        }
        List<EventScoreView> candidates = similarityRepository.findUnviewedSimilar(
                recentEvents, request.getUserId(), request.getMaxResults());
        if (candidates.isEmpty()) {
            return List.of();
        }
        Map<Long, List<NeighborView>> neighbors = candidates.stream()
                .map(EventScoreView::getEventId)
                .collect(Collectors.toMap(
                        Function.identity(),
                        id -> similarityRepository.findNearestNeighbors(id, request.getUserId(), NEIGHBOR_COUNT)
                ));
        return candidates.stream()
                .map(candidate -> buildSuggestion(candidate, neighbors))
                .toList();
    }

    @Override
    public List<RecommendedEventProto> fetchSimilarEvents(SimilarEventsRequestProto request) {
        long eventId = request.getEventId();
        if (request.getMaxResults() <= 0 || eventId <= 0) {
            return List.of();
        }
        List<EventSimilarity> similarities = similarityRepository.findByEvent(eventId);
        if (similarities.isEmpty()) {
            return List.of();
        }
        Set<Long> viewed = actionRepository.findInteractedEvents(request.getUserId());
        return similarities.stream()
                .filter(s -> !(viewed.contains(s.getEventA()) && viewed.contains(s.getEventB())))
                .sorted(Comparator.comparingDouble(EventSimilarity::getScore).reversed())
                .limit(request.getMaxResults())
                .map(s -> {
                    long counterpart = s.getEventA() == eventId ? s.getEventB() : s.getEventA();
                    return RecommendedEventProto.newBuilder()
                            .setEventId(counterpart)
                            .setScore(s.getScore())
                            .build();
                })
                .toList();
    }

    @Override
    public List<RecommendedEventProto> fetchInteractionsCount(InteractionsCountRequestProto request) {
        if (request.getEventIdList().isEmpty()) {
            return Collections.emptyList();
        }
        return actionRepository.sumRatingsByEvents(request.getEventIdList()).stream()
                .map(view -> RecommendedEventProto.newBuilder()
                        .setEventId(view.getEventId())
                        .setScore(view.getScore())
                        .build())
                .toList();
    }

    private RecommendedEventProto buildSuggestion(EventScoreView candidate,
                                                  Map<Long, List<NeighborView>> neighbors) {
        double score = computePrediction(candidate, neighbors);
        return RecommendedEventProto.newBuilder()
                .setEventId(candidate.getEventId())
                .setScore(score)
                .build();
    }

    private double computePrediction(EventScoreView candidate, Map<Long, List<NeighborView>> neighbors) {
        List<NeighborView> list = neighbors.getOrDefault(candidate.getEventId(), List.of());
        double similaritySum = list.stream().mapToDouble(NeighborView::getSimilarity).sum();
        if (similaritySum == 0.0) {
            return 0.0;
        }
        double weightedSum = list.stream()
                .mapToDouble(n -> n.getRating() * n.getSimilarity())
                .sum();
        return weightedSum / similaritySum;
    }
}