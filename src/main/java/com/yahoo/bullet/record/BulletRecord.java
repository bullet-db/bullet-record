/*
 *  Copyright 2018, Oath Inc.
 *  Licensed under the terms of the Apache License, Version 2.0.
 *  See the LICENSE file associated with the project for terms.
 */
package com.yahoo.bullet.record;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Data sent to bullet should be wrapped in a class that implements this interface. It extends {@link Serializable}
 * and {@link Iterable} so records can be used in for-each loops. The various set methods can be used to insert fields
 * into the record. Field names are to be provided as Strings and must be unique, otherwise the duplicate takes
 * precedence over the first by default.
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
 * Primitives: Boolean, Integer, Long, Float, Double, String
 * Complex: {@code Map <String, "Primitives">, Map<String, Map<String, "Primitives">>, List<Map<String, "Primitives">},
 *           where "Primitives" refers to the afore-mentioned Primitives.
 * </pre>
 *
 * Instead of setting a field to null (you cannot for top level Java primitives), avoid setting it instead.
 */
public interface BulletRecord extends Iterable<Map.Entry<String, Object>>, Serializable {
    /**
     * Gets a byte[] version of this record. You can use this method to get access to the raw
     * byte[] that represents the data in this BulletRecord. This is <strong>not</strong> a backing
     * store. If you insert more into the record, the byte[] will not reflect it. Also, this is
     * <strong>not</strong> the serialized version of this BulletRecord. It is the serialized version
     * of the <strong>data</strong> in the record. Use this if you do not want to serialize the entire
     * record and just want to deal with the raw bytes.
     *
     * @return the byte[] representation of the data in the record.
     * @throws IOException if the record cannot be serialized into a byte[].
     */
    public byte[] getAsByteArray() throws IOException;

    /**
     * Force read and convert the raw serialized data to valid Java objects. This method will force the record to
     * become reified. Otherwise, it will keep the raw data as is till fields are attempted to be extracted from it.
     *
     * @return true iff the data was able to be read from the raw data.
     */
    public boolean forceReadData();

    // ******************************************** GETTERS ********************************************

    /**
     * Gets a field stored in the record.
     *
     * @param field The non-null name of the field
     * @return The value of the field or null if it does not exist.
     */
    public Object get(String field);

    /**
     * Gets an object from a {@link Map} stored in the record.
     *
     * @param field The field name in the record that is a {@link Map}.
     * @param subKey The subfield in the {@link Map} that is desired.
     * @return The value of the subfield in the {@link Map} or null if the field does not exist.
     * @throws ClassCastException if the field is not a {@link Map}.
     */
    public Object get(String field, String subKey);

    /**
     * Gets an object from a {@link List} stored in the record.
     *
     * @param field The field name in the record that is a {@link List}.
     * @param index The position in the {@link List} that is desired.
     * @return The object at the index or null if the field does not exist.
     * @throws IndexOutOfBoundsException for invalid indices.
     * @throws ClassCastException if the field is not a {@link List}.
     */
    public Object get(String field, int index);

    /**
     * Returns true iff the given top-level field exists in the record.
     *
     * @param field The field to check if it exists.
     * @return A boolean denoting whether there was a mapping for the field.
     */
    public boolean hasField(String field);

    /**
     * Returns the number of fields in the record.
     *
     * @return An int representing the number of fields stored.
     */
    public int fieldCount();

    /**
     * Removes and returns a top-level field from the record.
     *
     * @param field The field to remove from the record.
     * @return The removed object or null.
     */
    public Object getAndRemove(String field);

    /**
     * Removes a top-level field from the record.
     *
     * @param field The field to remove from the record.
     * @return This object for chaining.
     */
    public BulletRecord remove(String field);

    /**
     * Renames a top-level field in the record.
     *
     * @param field The non-null original field name.
     * @param newName The non-null new name.
     * @return This object for chaining.
     */
    public BulletRecord rename(String field, String newName);

    // ******************************************** SETTERS ********************************************

    /**
     * Insert a boolean field.
     *
     * @param field The non-null name of the field.
     * @param object The value to insert.
     * @return this object for chaining.
     */
    public BulletRecord setBoolean(String field, Boolean object);

    /**
     * Insert a String field.
     *
     * @param field The non-null name of the field.
     * @param object The value to insert.
     * @return this object for chaining.
     */
    public BulletRecord setString(String field, String object);

    /**
     * Insert an int field.
     *
     * @param field The non-null name of the field.
     * @param object The value to insert.
     * @return this object for chaining.
     */
    public BulletRecord setInteger(String field, Integer object);

    /**
     * Insert a long field.
     *
     * @param field The non-null name of the field.
     * @param object The value to insert.
     * @return this object for chaining.
     */
    public BulletRecord setLong(String field, Long object);

    /**
     * Insert a float field.
     *
     * @param field The non-null name of the field.
     * @param object The value to insert.
     * @return this object for chaining.
     */
    public BulletRecord setFloat(String field, Float object);

    /**
     * Insert a double field.
     *
     * @param field The non-null name of the field.
     * @param object The value to insert.
     * @return this object for chaining.
     */
    public BulletRecord setDouble(String field, Double object);

    /**
     * Insert a Map of String to Boolean field.
     *
     * @param field The non-null name of the field.
     * @param object The value to insert.
     * @return this object for chaining.
     */
    public BulletRecord setBooleanMap(String field, Map<String, Boolean> object);

    /**
     * Insert a Map of String to String field.
     *
     * @param field The non-null name of the field.
     * @param object The value to insert.
     * @return this object for chaining.
     */
    public BulletRecord setStringMap(String field, Map<String, String> object);

    /**
     * Insert a Map of String to Integer field.
     *
     * @param field The non-null name of the field.
     * @param object The value to insert.
     * @return this object for chaining.
     */
    public BulletRecord setIntegerMap(String field, Map<String, Integer> object);

    /**
     * Insert a Map of String to Long field.
     *
     * @param field The non-null name of the field.
     * @param object The value to insert.
     * @return this object for chaining.
     */
    public BulletRecord setLongMap(String field, Map<String, Long> object);

    /**
     * Insert a Map of String to Float field.
     *
     * @param field The non-null name of the field.
     * @param object The value to insert.
     * @return this object for chaining.
     */
    public BulletRecord setFloatMap(String field, Map<String, Float> object);

    /**
     * Insert a Map of String to Double field.
     *
     * @param field The non-null name of the field.
     * @param object The value to insert.
     * @return this object for chaining.
     */
    public BulletRecord setDoubleMap(String field, Map<String, Double> object);

    /**
     * Insert a Map of String to Map of String to Boolean field.
     *
     * @param field The non-null name of the field.
     * @param object The value to insert.
     * @return this object for chaining.
     */
    public BulletRecord setMapOfBooleanMap(String field, Map<String, Map<String, Boolean>> object);

    /**
     * Insert a Map of String to Map of String to String field.
     *
     * @param field The non-null name of the field.
     * @param object The value to insert.
     * @return this object for chaining.
     */
    public BulletRecord setMapOfStringMap(String field, Map<String, Map<String, String>> object);

    /**
     * Insert a Map of String to Map of String to Integer field.
     *
     * @param field The non-null name of the field.
     * @param object The value to insert.
     * @return this object for chaining.
     */
    public BulletRecord setMapOfIntegerMap(String field, Map<String, Map<String, Integer>> object);

    /**
     * Insert a Map of String to Map of String to Long field.
     *
     * @param field The non-null name of the field.
     * @param object The value to insert.
     * @return this object for chaining.
     */
    public BulletRecord setMapOfLongMap(String field, Map<String, Map<String, Long>> object);

    /**
     * Insert a Map of String to Map of String to Float field.
     *
     * @param field The non-null name of the field.
     * @param object The value to insert.
     * @return this object for chaining.
     */
    public BulletRecord setMapOfFloatMap(String field, Map<String, Map<String, Float>> object);

    /**
     * Insert a Map of String to Map of String to Double field.
     *
     * @param field The non-null name of the field.
     * @param object The value to insert.
     * @return this object for chaining.
     */
    public BulletRecord setMapOfDoubleMap(String field, Map<String, Map<String, Double>> object);

    /**
     * Insert a List of Map of String to Boolean field.
     *
     * @param field The non-null name of the field.
     * @param object The value to insert.
     * @return this object for chaining.
     */
    public BulletRecord setListOfBooleanMap(String field, List<Map<String, Boolean>> object);

    /**
     * Insert a List of Map of String to String field.
     *
     * @param field The non-null name of the field.
     * @param object The value to insert.
     * @return this object for chaining.
     */
    public BulletRecord setListOfStringMap(String field, List<Map<String, String>> object);

    /**
     * Insert a List of Map of String to Integer field.
     *
     * @param field The non-null name of the field.
     * @param object The value to insert.
     * @return this object for chaining.
     */
    public BulletRecord setListOfIntegerMap(String field, List<Map<String, Integer>> object);

    /**
     * Insert a List of Map of String to Long field.
     *
     * @param field The non-null name of the field.
     * @param object The value to insert.
     * @return this object for chaining.
     */
    public BulletRecord setListOfLongMap(String field, List<Map<String, Long>> object);

    /**
     * Insert a List of Map of String to Float field.
     *
     * @param field The non-null name of the field.
     * @param object The value to insert.
     * @return this object for chaining.
     */
    public BulletRecord setListOfFloatMap(String field, List<Map<String, Float>> object);

    /**
     * Insert a List of Map of String to Double field.
     *
     * @param field The non-null name of the field.
     * @param object The value to insert.
     * @return this object for chaining.
     */
    public BulletRecord setListOfDoubleMap(String field, List<Map<String, Double>> object);

    /**
     * Insert into this record a field from another record.
     *
     * @param field The name in this record to insert the field as.
     * @param that The non-null record to extract the field from.
     * @param thatField The name of the field in that record to get the field from.
     * @return this object for chaining.
     */
    public BulletRecord set(String field, BulletRecord that, String thatField);

    /**
     * Insert into this record a Map sub-field from another record.
     *
     * @param field The name in this record to insert the field as.
     * @param that The non-null record to extract the field from.
     * @param thatMapField The name of the Map field in that record to get the key from.
     * @param thatMapKey The name of the key in the Map field in that record.
     * @return this object for chaining.
     */
    public BulletRecord set(String field, BulletRecord that, String thatMapField, String thatMapKey);

    /**
     * Insert into this record a List index from another record. The item is not copied.
     *
     * @param field The name in this record to insert the field as.
     * @param that The non-null record to extract the field from.
     * @param thatListField The name of the List field in that record to get the index of.
     * @param thatListIndex The index of the field in the List to get.
     * @return this object for chaining.
     */
    public BulletRecord set(String field, BulletRecord that, String thatListField, int thatListIndex);
}
