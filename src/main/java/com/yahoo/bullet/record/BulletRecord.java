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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * This wraps the data that should be sent to Bullet. It is a {@link Serializable} object.
 * It also implements {@link Iterable} and can be used in for-each loops. The various
 * set methods can be used to insert fields into the record. It maintains the insertion order.
 * Field names are to be provided as Strings and must be unique, otherwise the duplicate takes
 * precedence over the first.
 *
 * When inserting into the Record, methods are explicitly provided for each type supported (listed
 * below). When reading from the Record, which may not be something needed by users of this very
 * frequently (as it is the entry point into Bullet), a generic Object is returned instead. Casting
 * it is left to the user.
 *
 * By default, when reading, the record will not present the data as valid Java objects. It will
 * keep it serialized till any of the get/set methods are called. This makes the object cheap to
 * send through repeated read-write cycles without modifications. You can force a read by either
 * calling a get/set method or using {@link #forceReadData()}.
 *
 * The record currently supports these fields:
 * <pre>
 * Primitives: Boolean, Long, Double, String
 * Complex: {@code Map <String, "Primitives">, Map<String, Map<String, "Primitives">>, List<Map<String, "Primitives">},
 *           where "Primitives" refers to the afore-mentioned Primitives.
 * </pre>
 *
 * Instead of setting a field to null (you cannot for top level Java primitives), avoid setting it instead.
 */
@Slf4j @Setter(AccessLevel.PACKAGE) @NoArgsConstructor
public class BulletRecord implements Iterable<Map.Entry<String, Object>>, Serializable {
    public static final long serialVersionUID = 926415013785021742L;

    private boolean isDeserialized = true;
    private byte[] serializedData;
    private Map<String, Object> data = new LinkedHashMap<>();

    /**
     * Constructor that takes in the raw serialized byte[] that represents the data in the BulletRecord. The user is
     * responsible for ensuring that this byte[] was indeed produced by the same {@link #getAsByteArray()} method. The
     * user is also responsible for ensuring that the byte[] was produced by the same version of the BulletRecord as
     * is the version that the user is invoking this constructor on.
     *
     * @param data The serialized contents that represent this BulletRecord.
     */
    public BulletRecord(byte[] data) {
        serializedData = data;
        isDeserialized = false;
    }

    /**
     * Gets a byte[] version of this record. You can use this method to get access to the raw
     * byte[] that represents the data in this BulletRecord. This is <strong>not</strong> a backing
     * store. If you insert more into the record, the byte[] will not reflect it. Also, this is
     * <strong>not</strong> the serialized version of this BulletRecord. It is the serialized version
     * of the <strong>data</strong> in the record. Use this if you do not want to serialize the entire
     * record and just want to deal with the raw bytes. You can recreate the BulletRecord with the same
     * contents (but not the same state) by using the {@link #BulletRecord(byte[])} constructor.
     *
     * @return the byte[] representation of the data in the record.
     * @throws IOException if the record cannot be serialized into a byte[].
     */
    public byte[] getAsByteArray() throws IOException {
        if (isDeserialized) {
            return serialize(data);
        }
        return serializedData;
    }

    /**
     * Force read and convert the raw serialized data to valid Java objects. This
     * method will force the record to become reified. Otherwise, it will keep the raw data as is till fields
     * are attempted to be extracted from it.
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

    private byte[] serialize(Map<String, Object> data) throws IOException {
        data = (data == null) ? Collections.emptyMap() : data;
        BulletAvro record = new BulletAvro(data);
        ByteArrayOutputStream stream = new ByteArrayOutputStream(256 * 1024);
        SpecificDatumWriter<BulletAvro> writer = new SpecificDatumWriter<>(BulletAvro.class);
        EncoderFactory encoderFactory = new EncoderFactory();
        Encoder encoder = encoderFactory.directBinaryEncoder(stream, null);
        stream.reset();
        writer.write(record, encoder);
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
        if (!(object instanceof BulletRecord)) {
            return false;
        }
        BulletRecord that = (BulletRecord) object;
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

    // ******************************************** GETTERS ********************************************

    /**
     * Gets a field stored in the record.
     *
     * @param field The non-null name of the field
     * @return The value of the field or null if it does not exist.
     */
    public Object get(String field) {
        if (!forceReadData()) {
            return null;
        }
        return data.get(field);
    }

    /**
     * Gets an object from a {@link Map} stored in the record.
     *
     * @param field The field name in the record that is a {@link Map}.
     * @param subKey The subfield in the {@link Map} that is desired.
     * @return The value of the subfield in the {@link Map} or null if the field does not exist.
     * @throws ClassCastException if the field is not a {@link Map}.
     */
    public Object get(String field, String subKey) {
        Object value = get(field);
        if (value == null) {
            return null;
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> casted = (Map<String, Object>) value;
        return casted.get(subKey);
    }

    /**
     * Gets an object from a {@link List} stored in the record.
     *
     * @param field The field name in the record that is a {@link List}.
     * @param index The position in the {@link List} that is desired.
     * @return The object at the index or null if the field does not exist.
     * @throws IndexOutOfBoundsException for invalid indices.
     * @throws ClassCastException if the field is not a {@link List}.
     */
    public Object get(String field, int index) {
        Object value = get(field);
        if (value == null) {
            return null;
        }
        @SuppressWarnings("unchecked")
        List<Object> casted = (List<Object>) value;
        return casted.get(index);
    }

    // ******************************************** SETTERS ********************************************

    /**
     * Insert a boolean field.
     *
     * @param field The non-null name of the field.
     * @param object The value to insert.
     * @return this object for chaining.
     */
    public BulletRecord setBoolean(String field, boolean object) {
        return set(field, object);
    }

    /**
     * Insert a String field.
     *
     * @param field The non-null name of the field.
     * @param object The value to insert.
     * @return this object for chaining.
     */
    public BulletRecord setString(String field, String object) {
        return set(field, object);
    }

    /**
     * Insert a long field.
     *
     * @param field The non-null name of the field.
     * @param object The value to insert.
     * @return this object for chaining.
     */
    public BulletRecord setLong(String field, long object) {
        return set(field, object);
    }

    /**
     * Insert a double field.
     *
     * @param field The non-null name of the field.
     * @param object The value to insert.
     * @return this object for chaining.
     */
    public BulletRecord setDouble(String field, double object) {
        return set(field, object);
    }

    /**
     * Insert a Map of String to Boolean field.
     *
     * @param field The non-null name of the field.
     * @param object The value to insert.
     * @return this object for chaining.
     */
    public BulletRecord setBooleanMap(String field, Map<String, Boolean> object) {
        return set(field, object);
    }

    /**
     * Insert a Map of String to String field.
     *
     * @param field The non-null name of the field.
     * @param object The value to insert.
     * @return this object for chaining.
     */
    public BulletRecord setStringMap(String field, Map<String, String> object) {
        return set(field, object);
    }

    /**
     * Insert a Map of String to Long field.
     *
     * @param field The non-null name of the field.
     * @param object The value to insert.
     * @return this object for chaining.
     */
    public BulletRecord setLongMap(String field, Map<String, Long> object) {
        return set(field, object);
    }

    /**
     * Insert a Map of String to Double field.
     *
     * @param field The non-null name of the field.
     * @param object The value to insert.
     * @return this object for chaining.
     */
    public BulletRecord setDoubleMap(String field, Map<String, Double> object) {
        return set(field, object);
    }

    /**
     * Insert a Map of String to Map of String to Boolean field.
     *
     * @param field The non-null name of the field.
     * @param object The value to insert.
     * @return this object for chaining.
     */
    public BulletRecord setMapOfBooleanMap(String field, Map<String, Map<String, Boolean>> object) {
        return set(field, object);
    }

    /**
     * Insert a Map of String to Map of String to String field.
     *
     * @param field The non-null name of the field.
     * @param object The value to insert.
     * @return this object for chaining.
     */
    public BulletRecord setMapOfStringMap(String field, Map<String, Map<String, String>> object) {
        return set(field, object);
    }

    /**
     * Insert a Map of String to Map of String to Long field.
     *
     * @param field The non-null name of the field.
     * @param object The value to insert.
     * @return this object for chaining.
     */
    public BulletRecord setMapOfLongMap(String field, Map<String, Map<String, Long>> object) {
        return set(field, object);
    }

    /**
     * Insert a Map of String to Map of String to Double field.
     *
     * @param field The non-null name of the field.
     * @param object The value to insert.
     * @return this object for chaining.
     */
    public BulletRecord setMapOfDoubleMap(String field, Map<String, Map<String, Double>> object) {
        return set(field, object);
    }

    /**
     * Insert a List of Map of String to Boolean field.
     *
     * @param field The non-null name of the field.
     * @param object The value to insert.
     * @return this object for chaining.
     */
    public BulletRecord setListOfBooleanMap(String field, List<Map<String, Boolean>> object) {
        return set(field, object);
    }

    /**
     * Insert a List of Map of String to String field.
     *
     * @param field The non-null name of the field.
     * @param object The value to insert.
     * @return this object for chaining.
     */
    public BulletRecord setListOfStringMap(String field, List<Map<String, String>> object) {
        return set(field, object);
    }

    /**
     * Insert a List of Map of String to Long field.
     *
     * @param field The non-null name of the field.
     * @param object The value to insert.
     * @return this object for chaining.
     */
    public BulletRecord setListOfLongMap(String field, List<Map<String, Long>> object) {
        return set(field, object);
    }

    /**
     * Insert a List of Map of String to Double field.
     *
     * @param field The non-null name of the field.
     * @param object The value to insert.
     * @return this object for chaining.
     */
    public BulletRecord setListOfDoubleMap(String field, List<Map<String, Double>> object) {
        return set(field, object);
    }

    /**
     * Insert into this record a field from another record.
     *
     * @param field The name in this record to insert the field as.
     * @param that The non-null record to extract the field from.
     * @param thatField The name of the field in that record to get the field from.
     * @return this object for chaining.
     */
    public BulletRecord set(String field, BulletRecord that, String thatField) {
        return set(field, that.get(thatField));
    }

    /**
     * Insert into this record a Map sub-field from another record.
     *
     * @param field The name in this record to insert the field as.
     * @param that The non-null record to extract the field from.
     * @param thatMapField The name of the Map field in that record to get the key from.
     * @param thatMapKey The name of the key in the Map field in that record.
     * @return this object for chaining.
     */
    public BulletRecord set(String field, BulletRecord that, String thatMapField, String thatMapKey) {
        return set(field, that.get(thatMapField, thatMapKey));
    }

    /**
     * Insert into this record a List index from another record. The item is not copied.
     *
     * @param field The name in this record to insert the field as.
     * @param that The non-null record to extract the field from.
     * @param thatListField The name of the List field in that record to get the index of.
     * @param thatListIndex The index of the field in the List to get.
     * @return this object for chaining.
     */
    public BulletRecord set(String field, BulletRecord that, String thatListField, int thatListIndex) {
        return set(field, that.get(thatListField, thatListIndex));
    }

    private BulletRecord set(String field, Object object) {
        Objects.requireNonNull(field);
        forceReadData();
        data.put(field, object);
        return this;
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
    BulletRecord setMap(String field, Map.Entry<String, Object>... entries) {
        Objects.requireNonNull(entries);
        Map<String, Object> newMap = new LinkedHashMap<>(entries.length);
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
    BulletRecord setListMap(String field, Map<String, Object>... entries) {
        Objects.requireNonNull(entries);
        List<Map<String, Object>> data = new ArrayList<>();
        for (Map<String, Object> entry : entries) {
            data.add(entry);
        }
        return set(field, data);
    }
}
