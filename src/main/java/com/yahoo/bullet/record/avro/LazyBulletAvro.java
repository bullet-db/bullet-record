/*
 *  Copyright 2020, Yahoo Inc.
 *  Licensed under the terms of the Apache License, Version 2.0.
 *  See the LICENSE file associated with the project for terms.
 */
package com.yahoo.bullet.record.avro;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DatumReader;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

@Slf4j @NoArgsConstructor
@Getter(AccessLevel.PACKAGE) @Setter(AccessLevel.PACKAGE)
class LazyBulletAvro implements Serializable, Iterable<Map.Entry<String, Object>> {
    private boolean isDeserialized = true;
    private Map<String, Object> data = new HashMap<>();
    private byte[] serializedData;

    private static final long serialVersionUID = -5368363606317600282L;
    private static final SpecificDatumWriter<BulletAvro> WRITER = new SpecificDatumWriter<>(BulletAvro.class);

    /**
     * Constructor.
     *
     * @param other The {@link LazyBulletAvro} to copy.
     * @throws RuntimeException if failed to copy data from the source.
     */
    LazyBulletAvro(LazyBulletAvro other) {
        try {
            serializedData = other.getAsByteArray();
            isDeserialized = false;
        } catch (IOException e) {
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
     */
    public void set(String field, Object object) {
        Objects.requireNonNull(field);
        forceReadData();
        data.put(field, object);
    }

    /**
     * Retrieves a field.
     *
     * @param field The name of the field.
     * @return The value of field or null if it was not present.
     */
    public Object get(String field) {
        if (!forceReadData()) {
            return null;
        }
        return data.get(field);
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
    public Object getAndRemove(String field) {
        return hasField(field) ? data.remove(field) : null;
    }

    /**
     * Removes a field.
     *
     * @param field The name of the field.
     */
    public void remove(String field) {
        if (hasField(field)) {
            data.remove(field);
        }
    }

    /**
     * Returns an {@link Iterator} over the data as {@link Map.Entry} pairs of String keys to Objects.
     * {@inheritDoc}
     *
     * @return An iterator over the data stored.
     */
    @Override
    public Iterator<Map.Entry<String, Object>> iterator() {
        return forceReadData() ? data.entrySet().iterator() : Collections.<String, Object>emptyMap().entrySet().iterator();
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof LazyBulletAvro)) {
            return false;
        }
        LazyBulletAvro that = (LazyBulletAvro) object;
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
        BulletAvro record = new BulletAvro(data);
        ByteArrayOutputStream stream = new ByteArrayOutputStream(2048);
        EncoderFactory encoderFactory = new EncoderFactory();
        Encoder encoder = encoderFactory.binaryEncoder(stream, null);
        WRITER.write(record, encoder);
        encoder.flush();
        return stream.toByteArray();
    }

    private Map<String, Object> reify(byte[] data) throws IOException {
        DatumReader<BulletAvro> reader = new SpecificDatumReader<>(BulletAvro.class);
        BinaryDecoder decoder = DecoderFactory.get().binaryDecoder(data, null);
        BulletAvro avro = reader.read(null, decoder);
        return avro.getData();
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        if (isDeserialized) {
            serializedData = serialize(data);
            // Don't get rid of the map in case more data is inserted into it after serialization
        }
        out.writeObject(this.serializedData);
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