package ru.practicum.ewm.stats.analyzer.controller;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.ewm.stats.analyzer.service.SuggestionService;
import ru.practicum.grpc.stats.service.dashboard.*;

import java.util.List;

@GrpcService
@RequiredArgsConstructor
public class SuggestionEndpoint extends RecommendationsControllerGrpc.RecommendationsControllerImplBase {

    private final SuggestionService suggestionService;

    @Override
    public void getRecommendationsForUser(UserPredictionsRequestProto request,
                                          StreamObserver<RecommendedEventProto> responseObserver) {
        invokeSafely(responseObserver, () -> {
            List<RecommendedEventProto> suggestions = suggestionService.fetchUserPredictions(request);
            suggestions.forEach(responseObserver::onNext);
            responseObserver.onCompleted();
        });
    }

    @Override
    public void getSimilarEvents(SimilarEventsRequestProto request,
                                 StreamObserver<RecommendedEventProto> responseObserver) {
        invokeSafely(responseObserver, () -> {
            List<RecommendedEventProto> suggestions = suggestionService.fetchSimilarEvents(request);
            suggestions.forEach(responseObserver::onNext);
            responseObserver.onCompleted();
        });
    }

    @Override
    public void getInteractionsCount(InteractionsCountRequestProto request,
                                     StreamObserver<RecommendedEventProto> responseObserver) {
        invokeSafely(responseObserver, () -> {
            List<RecommendedEventProto> suggestions = suggestionService.fetchInteractionsCount(request);
            suggestions.forEach(responseObserver::onNext);
            responseObserver.onCompleted();
        });
    }

    @Override
    public void hasUserViewedEvent(HasUserViewedEventRequest request,
                                   StreamObserver<HasUserViewedEventResponse> responseObserver) {
        invokeSafely(responseObserver, () -> {
            boolean viewed = suggestionService.hasUserViewedEvent(request.getUserId(), request.getEventId());
            responseObserver.onNext(HasUserViewedEventResponse.newBuilder().setViewed(viewed).build());
            responseObserver.onCompleted();
        });
    }

    private void invokeSafely(StreamObserver<?> observer, Runnable action) {
        try {
            action.run();
        } catch (Exception ex) {
            observer.onError(new StatusRuntimeException(
                    Status.INTERNAL.withDescription(ex.getMessage()).withCause(ex)));
        }
    }
}