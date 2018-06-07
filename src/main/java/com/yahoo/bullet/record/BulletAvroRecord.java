/*
 *  Copyright 2016, Yahoo Inc.
 *  Licensed under the terms of the Apache License, Version 2.0.
 *  See the LICENSE file associated with the project for terms.
 */
package com.yahoo.bullet.record;

import lombok.AccessLevel;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * The default implementation of {@link BulletRecord}.
 */
@Slf4j @Setter(AccessLevel.PACKAGE) @NoArgsConstructor
public class BulletAvroRecord implements BulletRecord {
    public static final long serialVersionUID = 926415013785021742L;

    protected boolean isDeserialized = true;
    protected byte[] serializedData;
    protected Map<String, Object> data = new HashMap<>();
    protected static final SpecificDatumWriter<BulletAvro> WRITER = new SpecificDatumWriter<>(BulletAvro.class);

    /**
     * Constructor that takes in the raw serialized byte[] that represents the data in the BulletAvroRecord. The user is
     * responsible for ensuring that this byte[] was indeed produced by the same {@link #getAsByteArray()} method. The
     * user is also responsible for ensuring that the byte[] was produced by the same version of the BulletAvroRecord as
     * is the version that the user is invoking this constructor on.
     *
     * @param data The serialized contents that represent this BulletAvroRecord.
     */
    public BulletAvroRecord(byte[] data) {
        serializedData = data;
        isDeserialized = false;
    }

    /**
     * Constructor that lets you set an arbitrary {@link Map} of field names to values as the data for the BulletAvroRecord.
     * No checks are performed so this method is <strong>unsafe</strong> to use if you are not absolutely sure the
     * data can be placed into a BulletAvroRecord. This is meant as a convenience method to copy the same data into
     * multiple BulletRecords.
     *
     * @param data The mapping of field names to their typed values that constitutes the data for this BulletAvroRecord.
     */
    public BulletAvroRecord(Map<String, Object> data) {
        this.data = data;
        isDeserialized = true;
    }

    @Override
    public Iterator<Map.Entry<String, Object>> iterator() {
        return forceReadData() ? data.entrySet().iterator() : Collections.<String, Object>emptyMap().entrySet().iterator();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder().append("{");
        String prefix = "";
        for (Map.Entry<String, Object> fields : this) {
            builder.append(prefix).append(fields.getKey()).append(":").append(fields.getValue());
            prefix = ", ";
        }
        return builder.append("}").toString();
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof BulletAvroRecord)) {
            return false;
        }
        BulletAvroRecord that = (BulletAvroRecord) object;
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

    @Override
    public byte[] getAsByteArray() throws IOException {
        if (isDeserialized) {
            return serialize(data);
        }
        return serializedData;
    }

    @Override
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

    // ******************************************** GETTERS ********************************************

    @Override
    public Object get(String field) {
        if (!forceReadData()) {
            return null;
        }
        return data.get(field);
    }

    @Override
    public Object get(String field, String subKey) {
        Object value = get(field);
        if (value == null) {
            return null;
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> casted = (Map<String, Object>) value;
        return casted.get(subKey);
    }

    @Override
    public Object get(String field, int index) {
        Object value = get(field);
        if (value == null) {
            return null;
        }
        @SuppressWarnings("unchecked")
        List<Object> casted = (List<Object>) value;
        return casted.get(index);
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
        // Use hasField to deserialize if necessary
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
    public BulletRecord rename(String field, String newName) {
        if (hasField(field)) {
            set(newName, getAndRemove(field));
        }
        return this;
    }

    // ******************************************** SETTERS ********************************************

    @Override
    public BulletRecord setBoolean(String field, Boolean object) {
        return set(field, object);
    }

    @Override
    public BulletRecord setString(String field, String object) {
        return set(field, object);
    }

    @Override
    public  BulletRecord setInteger(String field, Integer object) {
        return set(field, object);
    }

    @Override
    public BulletRecord setLong(String field, Long object) {
        return set(field, object);
    }

    @Override
    public BulletRecord setFloat(String field, Float object) {
        return set(field, object);
    }

    @Override
    public BulletRecord setDouble(String field, Double object) {
        return set(field, object);
    }

    @Override
    public BulletRecord setBooleanMap(String field, Map<String, Boolean> object) {
        return set(field, object);
    }

    @Override
    public BulletAvroRecord setStringMap(String field, Map<String, String> object) {
        return set(field, object);
    }

    @Override
    public BulletRecord setIntegerMap(String field, Map<String, Integer> object) {
        return set(field, object);
    }

    @Override
    public BulletRecord setLongMap(String field, Map<String, Long> object) {
        return set(field, object);
    }

    @Override
    public BulletRecord setFloatMap(String field, Map<String, Float> object) {
        return set(field, object);
    }

    @Override
    public BulletRecord setDoubleMap(String field, Map<String, Double> object) {
        return set(field, object);
    }

    @Override
    public BulletRecord setMapOfBooleanMap(String field, Map<String, Map<String, Boolean>> object) {
        return set(field, object);
    }

    @Override
    public BulletRecord setMapOfStringMap(String field, Map<String, Map<String, String>> object) {
        return set(field, object);
    }

    @Override
    public BulletRecord setMapOfIntegerMap(String field, Map<String, Map<String, Integer>> object) {
        return set(field, object);
    }

    @Override
    public BulletRecord setMapOfLongMap(String field, Map<String, Map<String, Long>> object) {
        return set(field, object);
    }

    @Override
    public BulletRecord setMapOfFloatMap(String field, Map<String, Map<String, Float>> object) {
        return set(field, object);
    }

    @Override
    public BulletRecord setMapOfDoubleMap(String field, Map<String, Map<String, Double>> object) {
        return set(field, object);
    }

    @Override
    public BulletRecord setListOfBooleanMap(String field, List<Map<String, Boolean>> object) {
        return set(field, object);
    }

    @Override
    public BulletRecord setListOfStringMap(String field, List<Map<String, String>> object) {
        return set(field, object);
    }

    @Override
    public BulletRecord setListOfIntegerMap(String field, List<Map<String, Integer>> object) {
        return set(field, object);
    }

    @Override
    public BulletRecord setListOfLongMap(String field, List<Map<String, Long>> object) {
        return set(field, object);
    }

    @Override
    public BulletRecord setListOfFloatMap(String field, List<Map<String, Float>> object) {
        return set(field, object);
    }

    @Override
    public BulletRecord setListOfDoubleMap(String field, List<Map<String, Double>> object) {
        return set(field, object);
    }

    @Override
    public BulletRecord set(String field, BulletRecord that, String thatField) {
        return set(field, that.get(thatField));
    }

    @Override
    public BulletRecord set(String field, BulletRecord that, String thatMapField, String thatMapKey) {
        return set(field, that.get(thatMapField, thatMapKey));
    }

    @Override
    public BulletRecord set(String field, BulletRecord that, String thatListField, int thatListIndex) {
        return set(field, that.get(thatListField, thatListIndex));
    }

    private BulletAvroRecord set(String field, Object object) {
        Objects.requireNonNull(field);
        forceReadData();
        data.put(field, object);
        return this;
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

    /**
     * For Testing.
     *
     * Insert a map field with values as Pairs or Map.Entry. The value of
     * the entries must be in "Primitives".
     *
     * @param field The non-null name of the field.
     * @param entries The non-null entries to insert.
     * @return this object for chaining.
     */
    BulletAvroRecord setMap(String field, Map.Entry<String, Object>... entries) {
        Objects.requireNonNull(entries);
        Map<String, Object> newMap = new HashMap<>(entries.length);
        for (Map.Entry<String, Object> entry : entries) {
            newMap.put(entry.getKey(), entry.getValue());
        }
        return set(field, newMap);
    }

    /**
     * For Testing.
     *
     * Insert a list field with values as Pairs or Map.Entry of maps. The value of
     * the maps must be in "Primitives".
     *
     * @param field The non-null name of the field.
     * @param entries The non-null entries to insert.
     * @return this object for chaining.
     */
    BulletAvroRecord setListMap(String field, Map<String, Object>... entries) {
        Objects.requireNonNull(entries);
        List<Map<String, Object>> data = new ArrayList<>();
        for (Map<String, Object> entry : entries) {
            data.add(entry);
        }
        return set(field, data);
    }
}