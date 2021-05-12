/*
 *  Copyright 2021, Yahoo Inc.
 *  Licensed under the terms of the Apache License, Version 2.0.
 *  See the LICENSE file associated with the project for terms.
 */
package com.yahoo.bullet.record.avro;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * This class wraps all Bullet compatible AVRO data and makes lazily de-serializable. The data is reified only when
 * needed to. It operates on AVRO {@link org.apache.avro.specific.SpecificRecord} types. You should provide an
 * {@link AvroDataProvider} of the same type to extract the data out of the lazily de-serialized AVRO data.
 *
 * @param <T> The type of the AVRO generated class.
 */
@Slf4j
public class LazyAvro<T> implements Serializable, Iterable<Map.Entry<String, Serializable>> {
    private static final long serialVersionUID = 5465735405328695881L;

    @Getter(AccessLevel.PACKAGE) @Setter(AccessLevel.PACKAGE)
    private byte[] serializedData;
    @Getter(AccessLevel.PACKAGE) @Setter(AccessLevel.PACKAGE)
    private transient Map<String, Object> data = new HashMap<>();
    @Getter(AccessLevel.PACKAGE) @Setter(AccessLevel.PACKAGE)
    private transient boolean isDeserialized = true;

    private Class<T> klazz;
    private AvroDataProvider<T> provider;
    private transient SpecificDatumReader<T> reader;

    /**
     * Constructor that takes AVRO serialized data, the type of the AVRO generated record for that data and a
     * {@link AvroDataProvider} to extract data from a reified record of that type as a {@link Map}.
     *
     * @param data The serialized AVRO byte[] of the given type.
     * @param avroGeneratedClass The type of AVRO generated class.
     * @param avroDataProvider The {@link AvroDataProvider} that can get a {@link Map} of field names to Bullet types.
     */
    public LazyAvro(byte[] data, Class<T> avroGeneratedClass, AvroDataProvider<T> avroDataProvider) {
        serializedData = data;
        klazz = avroGeneratedClass;
        reader = new CustomAvroReader<>(avroGeneratedClass);
        provider = avroDataProvider;
        // If no data, then it might as well be deserialized
        isDeserialized = data == null;
    }

    /**
     * Copy constructor.
     *
     * @param other The {@link LazyAvro} to copy.
     * @throws RuntimeException if failed to copy data from the source.
     */
    public LazyAvro(LazyAvro<T> other) {
        try {
            serializedData = other.getAsByteArray();
            klazz = other.klazz;
            isDeserialized = false;
            reader = other.reader;
            provider = other.provider;
        } catch (Exception e) {
            log.error("Unable to serialize the other record", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Force read and convert the raw serialized data to valid Java objects. This method will force the record to
     * become reified. Otherwise, it will keep the raw data as is till fields are attempted to be extracted from it.
     *
     * @return true iff the data was able to be read from the raw data.
     */
    public boolean forceReadData() {
        if (isDeserialized) {
            return true;
        }
        try {
            data = reify(serializedData);
            // Get rid of the byte array to not store both. It will need to be recreated (since we have no dirty flag)
            // when writing out anyway since more things may have been inserted into the record.
            serializedData = null;
            isDeserialized = true;
            return true;
        } catch (Exception e) {
            log.error("Unable to read record from raw data", e);
            return false;
        }
    }

    /**
     * Sets a field.
     *
     * @param field The name of the field.
     * @param object The value of the field. Must be a supported type in the {@link BulletAvro} data field.
     * @return This object for chaining.
     */
    public LazyAvro set(String field, Serializable object) {
        Objects.requireNonNull(field);
        forceReadData();
        data.put(field, object);
        return this;
    }

    /**
     * Retrieves a field.
     *
     * @param field The name of the field.
     * @return The value of field or null if it was not present.
     */
    public Serializable get(String field) {
        if (!forceReadData()) {
            return null;
        }
        return (Serializable) data.get(field);
    }

    /**
     * Checks to see if a field exists.
     *
     * @param field The name of the field.
     * @return A boolean denoting if the field exists.
     */
    public boolean hasField(String field) {
        forceFailIfCannotRead();
        return data.containsKey(field);
    }

    /**
     * Gets the number of fields stored.
     *
     * @return The count of the number of fields.
     */
    public int fieldCount() {
        forceFailIfCannotRead();
        return data.size();
    }

    /**
     * Removes and returns a field.
     *
     * @param field The name of the field.
     * @return The value in the data or null if it does not exist.
     */
    public Serializable getAndRemove(String field) {
        return hasField(field) ? (Serializable) data.remove(field) : null;
    }

    /**
     * Removes a field.
     *
     * @param field The name of the field.
     * @return This object for chaining.
     */
    public LazyAvro remove(String field) {
        if (hasField(field)) {
            data.remove(field);
        }
        return this;
    }

    /**
     * Returns an {@link Iterator} over the data as {@link Map.Entry} pairs of String keys to Objects.
     * {@inheritDoc}
     *
     * @return An iterator over the data stored.
     */
    @Override
    public Iterator<Map.Entry<String, Serializable>> iterator() {
        return iterator(e -> (Serializable) e.getValue());
    }

    /**
     * Exposed at package since this exposes the underlying {@link Map} structure and raw values used by this class.
     *
     * Allows you to iterate over the data while applying a mapping {@link Function} to convert the underlying
     * raw objects to a type of {@link Serializable}.
     *
     * @param valueMapper The {@link Function} that takes {@link Map.Entry} of the key and raw object and returns a
     *                    sub-type of {@link Serializable}.
     * @param <T> The sub-type of {@link Serializable}.
     * @return An {@link Iterator} over the data.
     */
    <T extends Serializable> Iterator<Map.Entry<String, T>> iterator(Function<Map.Entry<String, Object>, T> valueMapper) {
        if (!forceReadData()) {
            return Collections.<String, T>emptyMap().entrySet().iterator();
        }
        Iterator<Map.Entry<String, Object>> iterator = data.entrySet().iterator();
        return new Iterator<Map.Entry<String, T>>() {
            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public Map.Entry<String, T> next() {
                Map.Entry<String, Object> entry = iterator.next();
                return new AbstractMap.SimpleEntry<>(entry.getKey(), valueMapper.apply(entry));
            }
        };
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof LazyAvro)) {
            return false;
        }
        LazyAvro that = (LazyAvro) object;
        // We need to force read the data since writing out data to bytes will give different byte arrays
        // if the content is the same but the order isn't.
        forceReadData();
        that.forceReadData();
        return data == that.data || (data != null && data.equals(that.data));
    }

    @Override
    public int hashCode() {
        forceReadData();
        // Value doesn't matter when data is null
        return data == null ? 42 : data.hashCode();
    }

    private void forceFailIfCannotRead() {
        if (!forceReadData()) {
            throw new RuntimeException("Cannot read from record. Unable to proceed.");
        }
    }

    private byte[] serialize(Map<String, Object> data) throws IOException {
        data = (data == null) ? Collections.emptyMap() : data;
        T record = provider.getRecord(data);
        ByteArrayOutputStream stream = new ByteArrayOutputStream(2048);
        EncoderFactory encoderFactory = new EncoderFactory();
        Encoder encoder = encoderFactory.binaryEncoder(stream, null);
        SpecificDatumWriter<T> writer = new SpecificDatumWriter<>(klazz);
        writer.write(record, encoder);
        encoder.flush();
        return stream.toByteArray();
    }

    private Map<String, Object> reify(byte[] data) throws IOException {
        BinaryDecoder decoder = DecoderFactory.get().binaryDecoder(data, null);
        T avro = reader.read(null, decoder);
        return provider.getData(avro);
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        if (isDeserialized) {
            serializedData = serialize(data);
            // Don't get rid of the map in case more data is inserted into it after serialization
        }
        out.writeObject(serializedData);
        out.writeObject(klazz);
        out.writeObject(provider);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        serializedData = (byte[]) in.readObject();
        klazz = (Class<T>) in.readObject();
        provider = (AvroDataProvider<T>) in.readObject();
        reader = new CustomAvroReader<>(klazz);
        data = null;
        isDeserialized = false;
    }

    private byte[] getAsByteArray() throws IOException {
        if (isDeserialized) {
            return serialize(data);
        }
        return serializedData;
    }
}
