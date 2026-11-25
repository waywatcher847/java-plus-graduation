package ru.practicum.ewm.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.practicum.grpc.stats.service.dashboard.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnalyzerGrpcClient {

    @GrpcClient("analyzer")
    private RecommendationsControllerGrpc.RecommendationsControllerBlockingStub analyzerStub;

    private final FallBackTemplate resilience;

    @Value("${grpc.client.analyzer.timeout-ms:2000}")
    private long timeoutMs;

    private RecommendationsControllerGrpc.RecommendationsControllerBlockingStub withDeadline() {
        return analyzerStub.withDeadlineAfter(timeoutMs, TimeUnit.MILLISECONDS);
    }

    public List<RecommendedEventProto> getRecommendationsForUser(long userId, int maxResults) {
        log.debug("Получение рекомендаций для пользователя {} с лимитом {}", userId, maxResults);
        UserPredictionsRequestProto request = UserPredictionsRequestProto.newBuilder()
                .setUserId(userId)
                .setMaxResults(maxResults)
                .build();

        return resilience.read("analyzer", () -> {
            Iterator<RecommendedEventProto> iterator = withDeadline().getRecommendationsForUser(request);
            List<RecommendedEventProto> result = new ArrayList<>();
            iterator.forEachRemaining(result::add);
            return result;
        }, List::of);
    }

    public double getEventRating(long eventId) {
        log.debug("Получение рейтинга для мероприятия {}", eventId);
        InteractionsCountRequestProto request = InteractionsCountRequestProto.newBuilder()
                .addEventId(eventId)
                .build();
        return resilience.read("analyzer", () -> {
            Iterator<RecommendedEventProto> iterator = withDeadline().getInteractionsCount(request);
            if (iterator.hasNext()) {
                return iterator.next().getScore();
            }
            return 0.0;
        }, () -> 0.0);
    }

    public boolean hasUserViewedEvent(long userId, long eventId) {
        log.debug("Проверка посещения мероприятия {} пользователем {}", eventId, userId);
        HasUserViewedEventRequest request = HasUserViewedEventRequest.newBuilder()
                .setUserId(userId)
                .setEventId(eventId)
                .build();

        return resilience.read("analyzer", () -> {
            HasUserViewedEventResponse response = withDeadline().hasUserViewedEvent(request);
            return response.getViewed();
        }, () -> false);
    }
}