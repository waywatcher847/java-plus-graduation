package ru.practicum.ewm.stats.avro;

public class SimilarityDecoder extends AvroDecoder<EventSimilarityAvro> {
    public SimilarityDecoder() {
        super(EventSimilarityAvro.getClassSchema());
    }
}