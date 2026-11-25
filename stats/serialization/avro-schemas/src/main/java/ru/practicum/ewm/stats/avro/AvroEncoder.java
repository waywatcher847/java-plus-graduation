package ru.practicum.ewm.stats.avro;

import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class AvroEncoder implements Serializer<SpecificRecordBase> {

    private final EncoderFactory encoderFactory = EncoderFactory.get();
    private BinaryEncoder encoder;

    @Override
    public byte[] serialize(String topic, SpecificRecordBase data) {
        if (data == null) {
            return new byte[0];
        }
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            DatumWriter<SpecificRecordBase> writer = new SpecificDatumWriter<>(data.getSchema());
            encoder = encoderFactory.binaryEncoder(output, encoder);
            writer.write(data, encoder);
            encoder.flush();
            return output.toByteArray();
        } catch (IOException ex) {
            throw new SerializationException("Не удалось закодировать данные для топика [" + topic + "]", ex);
        }
    }
}