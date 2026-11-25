package ru.practicum.ewm.stats.avro;

public class ActionDecoder extends AvroDecoder<UserActionAvro> {
    public ActionDecoder() {
        super(UserActionAvro.getClassSchema());
    }
}