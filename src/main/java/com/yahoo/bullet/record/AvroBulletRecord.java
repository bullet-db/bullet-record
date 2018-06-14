/*
 *  Copyright 2018, Oath Inc.
 *  Licensed under the terms of the Apache License, Version 2.0.
 *  See the LICENSE file associated with the project for terms.
 */
package com.yahoo.bullet.record;

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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

/**
 * An implementation of {@link BulletRecord} using Avro for serialization.
 *
 * By default, after serialization the record deserializes lazily. It will only deserialize when one of
 * the get/set methods are called. This makes the object cheap to send through repeated read-write cycles
 * without modifications. You can force a read by either calling a get/set method or using {@link #forceReadData()}.
 */
@Slf4j @Setter(AccessLevel.PACKAGE) @NoArgsConstructor
public class AvroBulletRecord extends BulletRecord {
    public static final long serialVersionUID = 926415013785021742L;

    @Getter(AccessLevel.PACKAGE)
    private boolean isDeserialized = true;
    @Getter(AccessLevel.PACKAGE)
    private Map<String, Object> data = new HashMap<>();
    private byte[] serializedData;
    private static final SpecificDatumWriter<BulletAvro> WRITER = new SpecificDatumWriter<>(BulletAvro.class);

    /**
     * Constructor.
     *
     * @param other The AvroBulletRecord to copy.
     * @throws IOException if failed to serialize the AvroBulletRecord object.
     */
    public AvroBulletRecord(AvroBulletRecord other) throws IOException {
        serializedData = other.getAsByteArray();
        isDeserialized = false;
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

    @Override
    protected BulletRecord set(String field, Object object) {
        Objects.requireNonNull(field);
        forceReadData();
        data.put(field, object);
        return this;
    }

    @Override
    public Object get(String field) {
        if (!forceReadData()) {
            return null;
        }
        return data.get(field);
    }

    @Override
    public boolean hasField(String field) {
        forceFailIfCannotRead();
        return data.containsKey(field);
    }

    @Override
    public int fieldCount() {
        forceFailIfCannotRead();
        return data.size();
    }

    @Override
    public Object getAndRemove(String field) {
        return hasField(field) ? data.remove(field) : null;
    }

    @Override
    public BulletRecord remove(String field) {
        if (hasField(field)) {
            data.remove(field);
        }
        return this;
    }

    @Override
    public Iterator<Map.Entry<String, Object>> iterator() {
        return forceReadData() ? data.entrySet().iterator() : Collections.<String, Object>emptyMap().entrySet().iterator();
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof AvroBulletRecord)) {
            return false;
        }
        AvroBulletRecord that = (AvroBulletRecord) object;
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
