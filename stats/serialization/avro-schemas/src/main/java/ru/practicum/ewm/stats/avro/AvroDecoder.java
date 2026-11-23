package ru.practicum.ewm.stats.avro;

import org.apache.avro.Schema;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;

public abstract class AvroDecoder<T extends SpecificRecordBase> implements Deserializer<T> {

    private final DecoderFactory decoderFactory;
    private final Schema schema;

    protected AvroDecoder(Schema schema) {
        this(DecoderFactory.get(), schema);
    }

    protected AvroDecoder(DecoderFactory decoderFactory, Schema schema) {
        this.decoderFactory = decoderFactory;
        this.schema = schema;
    }

    @Override
    public T deserialize(String topic, byte[] data) {
        if (data == null) {
            return null;
        }
        try {
            Decoder decoder = decoderFactory.binaryDecoder(data, null);
            SpecificDatumReader<T> reader = new SpecificDatumReader<>(schema);
            return reader.read(null, decoder);
        } catch (Exception ex) {
            throw new SerializationException("Не удалось декодировать данные из топика [" + topic + "]", ex);
        }
    }
}