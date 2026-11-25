package ru.practicum.ewm.stats.collector.resolver;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.grpc.stats.service.collector.UserActionControllerGrpc;
import ru.practicum.grpc.stats.service.collector.UserActionProto;

import java.time.Instant;

@GrpcService
@RequiredArgsConstructor
@Slf4j
public class ActionReceiver extends UserActionControllerGrpc.UserActionControllerImplBase {

    private final Producer<Long, SpecificRecordBase> publisher;

    @Value("${kafka.topics.user-actions}")
    private String actionsTopic;

    @Override
    public void collectUserAction(UserActionProto request, StreamObserver<Empty> responseObserver) {
        try {
            UserActionAvro action = mapToAvro(request);
            ProducerRecord<Long, SpecificRecordBase> record =
                    new ProducerRecord<>(actionsTopic, request.getUserId(), action);
            publisher.send(record);
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception ex) {
            log.error("Ошибка при приёме действия", ex);
            responseObserver.onError(new StatusRuntimeException(
                    Status.INTERNAL.withDescription(ex.getMessage()).withCause(ex)));
        }
    }

    @PreDestroy
    public void shutdown() {
        publisher.close();
    }

    private UserActionAvro mapToAvro(UserActionProto request) {
        return UserActionAvro.newBuilder()
                .setUserId(request.getUserId())
                .setEventId(request.getEventId())
                .setActionType(ActionTypeResolver.resolve(request.getActionType()))
                .setTimestamp(toInstant(request.getTimestamp()))
                .build();
    }

    private Instant toInstant(com.google.protobuf.Timestamp timestamp) {
        return Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos());
    }
}