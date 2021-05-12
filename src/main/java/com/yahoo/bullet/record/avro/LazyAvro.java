/*
 *  Copyright 2021, Yahoo Inc.
 *  Licensed under the terms of the Apache License, Version 2.0.
 *  See the LICENSE file associated with the project for terms.
 */
package com.yahoo.bullet.record.avro;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecord;

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
 * This class wraps all Bullet compatible AVRO data and makes it lazily de-serializable. The data is reified only when
 * needed to. It operates on AVRO {@link org.apache.avro.specific.SpecificRecord} types. Subclasses should override
 * {@link #getReader()} to provide the appropriate {@link SpecificDatumReader}, {@link #getWriter()} to provide the
 * {@link SpecificDatumWriter}, {@link #getData(SpecificRecord)} to convert the AVRO record into a {@link Map} of fields
 * to their values and {@link #getRecord} to convert a {@link Map} of fields to the AVRO record.
 *
 * @param <T> The type of the AVRO generated class.
 */
@Slf4j @NoArgsConstructor
public abstract class LazyAvro<T extends SpecificRecord> implements Serializable, Iterable<Map.Entry<String, Serializable>> {
    private static final long serialVersionUID = 5465735405328695881L;

    protected byte[] serializedData;
    protected transient Map<String, Object> data = new HashMap<>();
    protected transient boolean isDeserialized = true;

    /**
     * Copy constructor.
     *
     * @param other The {@link LazyAvro} to copy.
     * @throws RuntimeException if failed to copy data from the source.
     */
    public LazyAvro(LazyAvro<T> other) {
        try {
            serializedData = other.getAsByteArray();
            isDeserialized = false;
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

    /**
     * Get a {@link SpecificDatumReader} for this type.
     *
     * @return A reader for this type.
     */
    public abstract SpecificDatumReader<T> getReader();

    /**
     * Get a {@link SpecificDatumWriter} for this type.
     *
     * @return A writer for this type.
     */
    public abstract SpecificDatumWriter<T> getWriter();

    /**
     * Given a {@link Map} of fields, returns an AVRO record of this type wrapping them. This is the inverse of the
     * {@link #getData(SpecificRecord)} method.
     *
     * @param data The {@link Map} of fields to values matching types supported by Bullet.
     * @return A {@link }
     */
    public abstract T getRecord(Map<String, Object> data);

    /**
     * Given {@link SpecificRecord}, returns a {@link Map} of fields to values, whose types are supported in Bullet.
     * This is the inverse of the {@link #getRecord(Map)} method.
     *
     * @param record The {@link SpecificRecord} containing the data.
     * @return A {@link Map} of the fields.
     */
    public abstract Map<String, Object> getData(T record);

    private void forceFailIfCannotRead() {
        if (!forceReadData()) {
            throw new RuntimeException("Cannot read from record. Unable to proceed.");
        }
    }

    private byte[] serialize(Map<String, Object> data) throws IOException {
        data = (data == null) ? Collections.emptyMap() : data;
        T record = getRecord(data);
        ByteArrayOutputStream stream = new ByteArrayOutputStream(2048);
        EncoderFactory encoderFactory = new EncoderFactory();
        Encoder encoder = encoderFactory.binaryEncoder(stream, null);
        SpecificDatumWriter<T> writer = getWriter();
        writer.write(record, encoder);
        encoder.flush();
        return stream.toByteArray();
    }

    private Map<String, Object> reify(byte[] data) throws IOException {
        BinaryDecoder decoder = DecoderFactory.get().binaryDecoder(data, null);
        T avro = getReader().read(null, decoder);
        return getData(avro);
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        if (isDeserialized) {
            serializedData = serialize(data);
            // Don't get rid of the map in case more data is inserted into it after serialization
        }
        out.writeObject(serializedData);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        serializedData = (byte[]) in.readObject();
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
