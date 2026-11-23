package ru.practicum.ewm.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.practicum.grpc.stats.service.dashboard.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnalyzerGrpcClient {

    @GrpcClient("analyzer")
    private RecommendationsControllerGrpc.RecommendationsControllerBlockingStub analyzerStub;

    public List<RecommendedEventProto> getRecommendationsForUser(long userId, int maxResults) {
        log.debug("Получение рекомендаций для пользователя {} с лимитом {}", userId, maxResults);
        UserPredictionsRequestProto request = UserPredictionsRequestProto.newBuilder()
                .setUserId(userId)
                .setMaxResults(maxResults)
                .build();
        Iterator<RecommendedEventProto> iterator = analyzerStub.getRecommendationsForUser(request);
        List<RecommendedEventProto> result = new ArrayList<>();
        iterator.forEachRemaining(result::add);
        return result;
    }

    public double getEventRating(long eventId) {
        log.debug("Получение рейтинга для мероприятия {}", eventId);
        InteractionsCountRequestProto request = InteractionsCountRequestProto.newBuilder()
                .addEventId(eventId)
                .build();
        Iterator<RecommendedEventProto> iterator = analyzerStub.getInteractionsCount(request);
        if (iterator.hasNext()) {
            return iterator.next().getScore();
        }
        return 0.0;
    }

    public boolean hasUserViewedEvent(long userId, long eventId) {
        log.debug("Проверка посещения мероприятия {} пользователем {}", eventId, userId);
        HasUserViewedEventRequest request = HasUserViewedEventRequest.newBuilder()
                .setUserId(userId)
                .setEventId(eventId)
                .build();
        HasUserViewedEventResponse response = analyzerStub.hasUserViewedEvent(request);
        return response.getViewed();
    }
}