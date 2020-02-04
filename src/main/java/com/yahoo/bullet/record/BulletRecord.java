/*
 *  Copyright 2016, Yahoo Inc.
 *  Licensed under the terms of the Apache License, Version 2.0.
 *  See the LICENSE file associated with the project for terms.
 */
package com.yahoo.bullet.record;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Data sent to Bullet should be wrapped in a class that extends this abstract class. It is
 * {@link Serializable} and {@link Iterable} so records can be used in for-each loops. The various
 * set methods can be used to insert fields into the record. They are implemented by default to use
 * the base {@link #set(String, Object)} method which should be overridden in child classes. Field
 * names are to be provided as Strings and must be unique, otherwise the duplicate takes precedence
 * over the first by default.
 *
 * When inserting into the Record, methods are explicitly provided for each type supported (listed
 * below). When reading from the Record, which may not be something needed by users of this very
 * frequently (as it is the entry point into Bullet), a generic Object is returned instead. Casting
 * it is left to the user. See {@link TypedBulletRecord} for an alternative.
 *
 * For the types supported by this records, see {@link com.yahoo.bullet.typesystem.Type}.
 * Primitives: {@link com.yahoo.bullet.typesystem.Type#PRIMITIVES}
 * Complex: {@link com.yahoo.bullet.typesystem.Type#MAPS} and {@link com.yahoo.bullet.typesystem.Type#LISTS}
 *
 * Instead of setting a field to null (you cannot for top level Java primitives), avoid setting it instead.
 */
public abstract class BulletRecord<T> implements Iterable<Map.Entry<String, T>>, Serializable {
    private static final long serialVersionUID = 3319286957467020672L;
    private static final String KEY_DELIMITER = "\\.";

    /**
     * Insert a field into this BulletRecord. This is the glue method used by the other set methods and should remain
     * protected to not expose this to to the outside. It will call {@link #convert(Object)} first.
     *
     * @param field The non-null name of the field.
     * @param object The object to be set.
     * @return This object for chaining.
     */
    protected BulletRecord<T> set(String field,  Object object) {
        return rawSet(field, convert(object));
    }

    /**
     * Insert a field into this BulletRecord. This is the core method used by the other set methods and should remain
     * protected in child classes to ensure type safety.
     *
     * @param field The non-null name of the field.
     * @param object The object to be set.
     * @return This object for chaining.
     */
    protected abstract BulletRecord<T> rawSet(String field, T object);

    /**
     * Convert the given object into a the format stored in this record.
     *
     * @param object The object to convert.
     * @return The converted object.
     */
    protected abstract T convert(Object object);

    /**
     * Gets a field stored in the record.
     *
     * @param field The non-null name of the field.
     * @return The value of the field or null if it does not exist.
     */
    public abstract T get(String field);

    /**
     * Returns true iff the given top-level field exists in the record.
     *
     * @param field The field to check if it exists.
     * @return A boolean denoting whether there was a mapping for the field.
     */
    public abstract boolean hasField(String field);

    /**
     * Returns the number of fields in the record.
     *
     * @return An int representing the number of fields stored.
     */
    public abstract int fieldCount();

    /**
     * Removes and returns a top-level field from the record.
     *
     * @param field The field to remove from the record.
     * @return The removed object or null.
     */
    public abstract T getAndRemove(String field);

    /**
     * Removes a top-level field from the record.
     *
     * @param field The field to remove from the record.
     * @return This object for chaining.
     */
    public abstract BulletRecord<T> remove(String field);

    // ******************************************** GETTERS ********************************************

    /**
     * Gets an object from a {@link Map} stored in the record.
     *
     * @param field The field name in the record that is a {@link Map}.
     * @param subKey The subfield in the {@link Map} that is desired.
     * @return The value of the subfield in the {@link Map} or null if the field does not exist.
     * @throws ClassCastException if the field is not a {@link Map}.
     */
    @SuppressWarnings("unchecked")
    public T get(String field, String subKey) {
        Map<String, T> value = (Map<String, T>) get(field);
        if (value == null) {
            return null;
        }
        return value.get(subKey);
    }

    /**
     * Gets an object from a {@link Map} stored in the record.
     *
     * @param field The field name in the record that is a {@link Map}.
     * @param subKey The subfield map in the {@link Map} that is desired.
     * @param subSubKey The subfield in the subfield {@link Map} that is desired.
     * @return The value of the subfield in the subfield {@link Map} or null if the field does not exist.
     * @throws ClassCastException if the field or subfield is not a {@link Map}.
     */
    @SuppressWarnings("unchecked")
    public T get(String field, String subKey, String subSubKey) {
        Map<String, Map<String, T>> first = (Map<String, Map<String, T>>) get(field);
        if (first == null) {
            return null;
        }
        Map<String, T> second = first.get(subKey);
        return second == null ? null : second.get(subSubKey);
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
    @SuppressWarnings("unchecked")
    public T get(String field, int index) {
        List<T> value = (List<T>) get(field);
        if (value == null) {
            return null;
        }
        return value.get(index);
    }

    /**
     * Gets an object from a {@link List} stored in the record.
     *
     * @param field The field name in the record that is a {@link List}.
     * @param index The position in the {@link List} that is desired.
     * @param subKey The subfield of the {@link List} element that is desired.
     * @return The object at the index or null if the field does not exist.
     * @throws IndexOutOfBoundsException for invalid indices.
     * @throws ClassCastException if the field is not a {@link List} or the indexed element is not a {@link Map}
     */
    @SuppressWarnings("unchecked")
    public T get(String field, int index, String subKey) {
        List<Map<String, T>> first = (List<Map<String, T>>) get(field);
        if (first == null) {
            return null;
        }
        Map<String, T> second = first.get(index);
        return second.get(subKey);
    }

    /**
     * A helper to gets a field from the record by a custom identifier format.<br>
     * <br>
     * For example, suppose a record has a map of boolean maps called "aaa". Then <br>
     * - "aaa" identifies that map of maps <br>
     * - "aaa.bbb" identifies the inner map that "aaa" maps "bbb" to (if it exists) <br>
     * - "aaa.bbb.ccc" identifies the boolean that "aaa.bbb" (if it exists) maps "ccc" to (if it exists) <br>
     * <br>
     * For a list element, the index is the key, e.g. "my_list.0" or "my_list.0.some_key"
     *
     * @param identifier The non-null identifier of the field to get.
     * @return The value of the field or null if it does not exist.
     */
    @SuppressWarnings("unchecked")
    public T extractField(String identifier) {
        try {
            String[] keys = identifier.split(KEY_DELIMITER, 3);
            T first = get(keys[0]);
            if (keys.length == 1) {
                return first;
            }
            T second;
            if (first instanceof Map) {
                second = ((Map<String, T>) first).get(keys[1]);
            } else if (first instanceof List) {
                second = ((List<T>) first).get(Integer.parseInt(keys[1]));
            } else {
                return null;
            }
            if (keys.length == 2) {
                return second;
            }
            return ((Map<String, T>) second).get(keys[2]);
        } catch (Exception e) {
            return null;
        }
    }

    // ******************************************** SETTERS ********************************************

    /**
     * Insert a Boolean field.
     *
     * @param field The non-null name of the field.
     * @param object The value to insert.
     * @return This object for chaining.
     */
    public BulletRecord<T> setBoolean(String field, Boolean object) {
        return set(field, object);
    }

    /**
     * Insert a String field.
     *
     * @param field The non-null name of the field.
     * @param object The value to insert.
     * @return This object for chaining.
     */
    public BulletRecord<T> setString(String field, String object) {
        return set(field, object);
    }

    /**
     * Insert an Integer field.
     *
     * @param field The non-null name of the field.
     * @param object The value to insert.
     * @return This object for chaining.
     */
    public BulletRecord<T> setInteger(String field, Integer object) {
        return set(field, object);
    }

    /**
     * Insert a Long field.
     *
     * @param field The non-null name of the field.
     * @param object The value to insert.
     * @return This object for chaining.
     */
    public BulletRecord<T> setLong(String field, Long object) {
        return set(field, object);
    }

    /**
     * Insert a Float field.
     *
     * @param field The non-null name of the field.
     * @param object The value to insert.
     * @return This object for chaining.
     */
    public BulletRecord<T> setFloat(String field, Float object) {
        return set(field, object);
    }

    /**
     * Insert a Double field.
     *
     * @param field The non-null name of the field.
     * @param object The value to insert.
     * @return This object for chaining.
     */
    public BulletRecord<T> setDouble(String field, Double object) {
        return set(field, object);
    }

    /**
     * Insert a Map of String to Boolean field.
     *
     * @param field The non-null name of the field.
     * @param object The value to insert.
     * @return This object for chaining.
     */
    public BulletRecord<T> setBooleanMap(String field, Map<String, Boolean> object) {
        return set(field, object);
    }

    /**
     * Insert a Map of String to String field.
     *
     * @param field The non-null name of the field.
     * @param object The value to insert.
     * @return This object for chaining.
     */
    public BulletRecord<T> setStringMap(String field, Map<String, String> object) {
        return set(field, object);
    }

    /**
     * Insert a Map of String to Integer field.
     *
     * @param field The non-null name of the field.
     * @param object The value to insert.
     * @return This object for chaining.
     */
    public BulletRecord<T> setIntegerMap(String field, Map<String, Integer> object) {
        return set(field, object);
    }

    /**
     * Insert a Map of String to Long field.
     *
     * @param field The non-null name of the field.
     * @param object The value to insert.
     * @return This object for chaining.
     */
    public BulletRecord<T> setLongMap(String field, Map<String, Long> object) {
        return set(field, object);
    }

    /**
     * Insert a Map of String to Float field.
     *
     * @param field The non-null name of the field.
     * @param object The value to insert.
     * @return This object for chaining.
     */
    public BulletRecord<T> setFloatMap(String field, Map<String, Float> object) {
        return set(field, object);
    }

    /**
     * Insert a Map of String to Double field.
     *
     * @param field The non-null name of the field.
     * @param object The value to insert.
     * @return This object for chaining.
     */
    public BulletRecord<T> setDoubleMap(String field, Map<String, Double> object) {
        return set(field, object);
    }

    /**
     * Insert a List of Boolean field.
     *
     * @param field The non-null name of the field.
     * @param object The value to insert.
     * @return This object for chaining.
     */
    public BulletRecord<T> setBooleanList(String field, List<Boolean> object) {
        return set(field, object);
    }

    /**
     * Insert a List of String field.
     *
     * @param field The non-null name of the field.
     * @param object The value to insert.
     * @return This object for chaining.
     */
    public BulletRecord<T> setStringList(String field, List<String> object) {
        return set(field, object);
    }

    /**
     * Insert a List of Integer field.
     *
     * @param field The non-null name of the field.
     * @param object The value to insert.
     * @return This object for chaining.
     */
    public BulletRecord<T> setIntegerList(String field, List<Integer> object) {
        return set(field, object);
    }

    /**
     * Insert a List of Long field.
     *
     * @param field The non-null name of the field.
     * @param object The value to insert.
     * @return This object for chaining.
     */
    public BulletRecord<T> setLongList(String field, List<Long> object) {
        return set(field, object);
    }

    /**
     * Insert a List of Float field.
     *
     * @param field The non-null name of the field.
     * @param object The value to insert.
     * @return This object for chaining.
     */
    public BulletRecord<T> setFloatList(String field, List<Float> object) {
        return set(field, object);
    }

    /**
     * Insert a List of Double field.
     *
     * @param field The non-null name of the field.
     * @param object The value to insert.
     * @return This object for chaining.
     */
    public BulletRecord<T> setDoubleList(String field, List<Double> object) {
        return set(field, object);
    }

    /**
     * Insert a Map of String to Map of String to Boolean field.
     *
     * @param field The non-null name of the field.
     * @param object The value to insert.
     * @return This object for chaining.
     */
    public BulletRecord<T> setMapOfBooleanMap(String field, Map<String, Map<String, Boolean>> object) {
        return set(field, object);
    }

    /**
     * Insert a Map of String to Map of String to String field.
     *
     * @param field The non-null name of the field.
     * @param object The value to insert.
     * @return This object for chaining.
     */
    public BulletRecord<T> setMapOfStringMap(String field, Map<String, Map<String, String>> object) {
        return set(field, object);
    }

    /**
     * Insert a Map of String to Map of String to Integer field.
     *
     * @param field The non-null name of the field.
     * @param object The value to insert.
     * @return This object for chaining.
     */
    public BulletRecord<T> setMapOfIntegerMap(String field, Map<String, Map<String, Integer>> object) {
        return set(field, object);
    }

    /**
     * Insert a Map of String to Map of String to Long field.
     *
     * @param field The non-null name of the field.
     * @param object The value to insert.
     * @return This object for chaining.
     */
    public BulletRecord<T> setMapOfLongMap(String field, Map<String, Map<String, Long>> object) {
        return set(field, object);
    }

    /**
     * Insert a Map of String to Map of String to Float field.
     *
     * @param field The non-null name of the field.
     * @param object The value to insert.
     * @return This object for chaining.
     */
    public BulletRecord<T> setMapOfFloatMap(String field, Map<String, Map<String, Float>> object) {
        return set(field, object);
    }

    /**
     * Insert a Map of String to Map of String to Double field.
     *
     * @param field The non-null name of the field.
     * @param object The value to insert.
     * @return This object for chaining.
     */
    public BulletRecord setMapOfDoubleMap(String field, Map<String, Map<String, Double>> object) {
        return set(field, object);
    }

    /**
     * Insert a List of Map of String to Boolean field.
     *
     * @param field The non-null name of the field.
     * @param object The value to insert.
     * @return This object for chaining.
     */
    public BulletRecord<T> setListOfBooleanMap(String field, List<Map<String, Boolean>> object) {
        return set(field, object);
    }

    /**
     * Insert a List of Map of String to String field.
     *
     * @param field The non-null name of the field.
     * @param object The value to insert.
     * @return This object for chaining.
     */
    public BulletRecord<T> setListOfStringMap(String field, List<Map<String, String>> object) {
        return set(field, object);
    }

    /**
     * Insert a List of Map of String to Integer field.
     *
     * @param field The non-null name of the field.
     * @param object The value to insert.
     * @return This object for chaining.
     */
    public BulletRecord<T> setListOfIntegerMap(String field, List<Map<String, Integer>> object) {
        return set(field, object);
    }

    /**
     * Insert a List of Map of String to Long field.
     *
     * @param field The non-null name of the field.
     * @param object The value to insert.
     * @return This object for chaining.
     */
    public BulletRecord<T> setListOfLongMap(String field, List<Map<String, Long>> object) {
        return set(field, object);
    }

    /**
     * Insert a List of Map of String to Float field.
     *
     * @param field The non-null name of the field.
     * @param object The value to insert.
     * @return This object for chaining.
     */
    public BulletRecord<T> setListOfFloatMap(String field, List<Map<String, Float>> object) {
        return set(field, object);
    }

    /**
     * Insert a List of Map of String to Double field.
     *
     * @param field The non-null name of the field.
     * @param object The value to insert.
     * @return This object for chaining.
     */
    public BulletRecord<T> setListOfDoubleMap(String field, List<Map<String, Double>> object) {
        return set(field, object);
    }

    /**
     * Insert into this record a field from another record.
     *
     * @param field The name in this record to insert the field as.
     * @param that The non-null record to extract the field from.
     * @param thatField The name of the field in that record to get the field from.
     * @return This object for chaining.
     */
    public BulletRecord<T> set(String field, BulletRecord that, String thatField) {
        return forceSet(field, that.get(thatField));
    }

    /**
     * Insert into this record a Map sub-field from another record. The item is not deep copied.
     *
     * @param field The name in this record to insert the field as.
     * @param that The non-null record to extract the field from.
     * @param thatMapField The name of the Map field in that record to get the key from.
     * @param thatMapKey The name of the key in the Map field in that record.
     * @return This object for chaining.
     */
    public BulletRecord<T> set(String field, BulletRecord that, String thatMapField, String thatMapKey) {
        return forceSet(field, that.get(thatMapField, thatMapKey));
    }

    /**
     * Insert into this record a sub-field from a Map of Maps in another record. The item is not deep copied.
     *
     * @param field The name in this record to insert the field as.
     * @param that The non-null record to extract the field from.
     * @param thatMapOfMapField The name of the Map of Map field in that record to get the field from.
     * @param thatMapOfMapKey The name of the Map key in the Map of Map field in that record.
     * @param thatMapKey The name of the key in the Map from the Map of Map field in that record to copy.
     * @return This object for chaining.
     */
    public BulletRecord<T> set(String field, BulletRecord that, String thatMapOfMapField, String thatMapOfMapKey, String thatMapKey) {
        return forceSet(field, that.get(thatMapOfMapField, thatMapOfMapKey, thatMapKey));
    }

    /**
     * Insert into this record a List index from another record. The item is not deep copied.
     *
     * @param field The name in this record to insert the field as.
     * @param that The non-null record to extract the field from.
     * @param thatListField The name of the List field in that record to get the index of.
     * @param thatListIndex The index of the field in the List to get.
     * @return This object for chaining.
     */
    public BulletRecord<T> set(String field, BulletRecord that, String thatListField, int thatListIndex) {
        return forceSet(field, that.get(thatListField, thatListIndex));
    }

    /**
     * Insert into this record a key from a Map at a List index from another record. The item is not deep copied.
     *
     * @param field The name in this record to insert the field as.
     * @param that The non-null record to extract the field from.
     * @param thatListField The name of the List field in that record to get the index of.
     * @param thatListIndex The index of the Map field in the List to get.
     * @param thatListMapKey The key in the Map field in the List to get.
     * @return This object for chaining.
     */
    public BulletRecord<T> set(String field, BulletRecord that, String thatListField, int thatListIndex, String thatListMapKey) {
        return forceSet(field, that.get(thatListField, thatListIndex, thatListMapKey));
    }

    /**
     * Try to forcibly set an arbitrary object as a top-level field in this record. This might be unsafe and your record
     * may no longer serialize properly! Use the appropriate set method if you know the type of your field. Only use
     * this method if you are sure of the type of the object and just do not want to bother casting or checking it. You
     * can also use it to temporarily store something in the record as long as you remove it before serializing it.
     *
     * @param field The name in this record to insert the object as.
     * @param object The object to be set.
     * @return This object for chaining.
     */
    public BulletRecord<T> forceSet(String field, Object object) {
        return set(field, object);
    }

    /**
     * Renames a top-level field in the record.
     *
     * @param field The non-null original field name.
     * @param newName The non-null new name.
     * @return This object for chaining.
     */
    public BulletRecord<T> rename(String field, String newName) {
        if (hasField(field)) {
            forceSet(newName, getAndRemove(field));
        }
        return this;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder().append("{");
        String prefix = "";
        for (Map.Entry<String, T> fields : this) {
            builder.append(prefix).append(fields.getKey()).append(":").append(fields.getValue());
            prefix = ", ";
        }
        return builder.append("}").toString();
    }

    /**
     * For Testing.
     *
     * Insert a map field with values as Pairs or Map.Entry. The value of
     * the entries must be in "Primitives".
     *
     * @param field The non-null name of the field.
     * @param entries The non-null entries to insert.
     * @return This object for chaining.
     */
    BulletRecord<T> setMap(String field, Map.Entry<String, Object>... entries) {
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
     * @return This object for chaining.
     */
    BulletRecord<T> setListMap(String field, Map<String, Object>... entries) {
        Objects.requireNonNull(entries);
        List<Map<String, Object>> data = new ArrayList<>();
        for (Map<String, Object> entry : entries) {
            data.add(entry);
        }
        return set(field, data);
    }
}
