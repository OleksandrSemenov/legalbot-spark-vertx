package com.spark.util;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.google.inject.Inject;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;

import java.util.UUID;

/**
 * @author Taras Zubrei
 */
public class CustomMessageCodec<T> implements MessageCodec<T, T> {
    private final Kryo kryo;

    @Inject
    public CustomMessageCodec(Kryo kryo) {
        this.kryo = kryo;
    }

    @Override
    public void encodeToWire(Buffer buffer, T o) {
        try (final Output output = new Output()) {
            kryo.writeClassAndObject(output, o);
            buffer.appendBytes(output.toBytes());
            output.close();
        }
    }

    @Override
    public T decodeFromWire(int pos, Buffer buffer) {
        try (final Input output = new Input(buffer.getBytes())) {
            return (T) kryo.readClassAndObject(output);
        }
    }

    @Override
    public T transform(T o) {
        return o;
    }

    @Override
    public String name() {
        return this.getClass().getSimpleName() + "_" + UUID.randomUUID();
    }

    @Override
    public byte systemCodecID() {
        return -1;
    }
}
