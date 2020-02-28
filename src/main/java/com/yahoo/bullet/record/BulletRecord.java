/*
 *  Copyright 2016, Yahoo Inc.
 *  Licensed under the terms of the Apache License, Version 2.0.
 *  See the LICENSE file associated with the project for terms.
 */
package com.yahoo.bullet.record;

import com.yahoo.bullet.typesystem.TypedObject;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Data sent to Bullet should be wrapped in a class that extends this abstract class. It is
 * {@link Serializable} and {@link Iterable} so records can be used in for-each loops. The various
 * set methods can be used to insert fields into the record. They are implemented by default to use
 * the {@link #set(String, Object)} method which should be overridden in child classes. This method
 * delegates to the {@link #rawSet(String, Object)} method after calling {@link #convert(Object)} on
 * it. Field names are to be provided as Strings and must be unique, otherwise the duplicate takes precedence
 * over the first by default.
 *
 * When inserting into the Record, methods are explicitly provided for each type supported (listed
 * below). You may also use {@link #typedSet(String, TypedObject)} to set a {@link TypedObject} into
 * the record and use the various {@link #typedGet(String)} methods to read data out as {@link TypedObject}
 * When reading raw data from the Record, which may not be something needed by users of this very
 * frequently (as it is the entry point into Bullet), you may use {@link #get(String)}. Casting
 * it is left to the user. See {@link TypedBulletRecord} for an alternative.
 *
 * For the types supported by this record, see {@link com.yahoo.bullet.typesystem.Type}.
 * Primitives: {@link com.yahoo.bullet.typesystem.Type#PRIMITIVES}
 * Complex: {@link com.yahoo.bullet.typesystem.Type#MAPS} and {@link com.yahoo.bullet.typesystem.Type#LISTS}
 *
 * Instead of setting a field to null (you cannot for top level Java primitives), avoid setting it instead.
 */
public abstract class BulletRecord<T> implements Iterable<Map.Entry<String, T>>, Serializable {
    private static final long serialVersionUID = 3319286957467020672L;

    /**
     * Convert the given object into the format stored in this record.
     *
     * @param object The object to convert.
     * @return The converted object.
     */
    protected abstract T convert(Object object);

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

    /**
     * Gets a field stored in the record as a {@link TypedObject}. This is intended to be the primary way to get fields
     * out of the record.
     *
     * @param field The non-null name of the field.
     * @return The value of the field as a {@link TypedObject} or {@link TypedObject#NULL} if it does not exist.
     */
    public abstract TypedObject typedGet(String field);

    /**
     * Creates a copy of this record. This might be a shallow copy so the original (i.e. this) is recommended to be
     * used as a read-only record after.
     *
     * @return A copy of this record.
     */
    public abstract BulletRecord<T> copy();

    // ****************************************** TypedObject Nested Getters ******************************************

    /**
     * Gets a {@link TypedObject} from a {@link Map} stored in the record.
     *
     * @param field The field name in the record that is a {@link Map}.
     * @param subKey The subfield in the {@link Map} that is desired.
     * @return The value of the subfield in the {@link Map} as a {@link TypedObject} or {@link TypedObject#NULL} if the field does not exist.
     * @throws ClassCastException if the field is not a {@link Map}.
     */
    @SuppressWarnings("unchecked")
    public TypedObject typedGet(String field, String subKey) {
        TypedObject value = typedGet(field);
        if (value.isNull()) {
            return TypedObject.NULL;
        }
        if (!value.isMap()) {
            throw new ClassCastException(field + " is not a map. It has type " + value.getType());
        }
        Map<String, Object> map = (Map<String, Object>) value.getValue();
        Object fieldValue = map.get(subKey);
        return fieldValue == null ? TypedObject.NULL : new TypedObject(value.getType().getSubType(), fieldValue);
    }

    /**
     * Gets an object from a {@link Map} stored in the record.
     *
     * @param field The field name in the record that is a {@link Map}.
     * @param subKey The subfield map in the {@link Map} that is desired.
     * @param subSubKey The subfield in the subfield {@link Map} that is desired.
     * @return The value of the subfield in the map {@link Map} as a {@link TypedObject} or {@link TypedObject#NULL} if the field does not exist.
     * @throws ClassCastException if the field or subfield is not a {@link Map}.
     */
    @SuppressWarnings("unchecked")
    public TypedObject typedGet(String field, String subKey, String subSubKey) {
        TypedObject value = typedGet(field);
        if (value.isNull()) {
            return TypedObject.NULL;
        }
        if (!value.isComplexMap()) {
            throw new ClassCastException(field + " is not a map of maps. It has type " + value.getType());
        }
        Map<String, Map<String, Object>> first = (Map<String, Map<String, Object>>) value.getValue();
        Map<String, Object> second = first.get(subKey);
        if (second == null) {
            return TypedObject.NULL;
        }
        Object fieldValue = second.get(subSubKey);
        return fieldValue == null ? TypedObject.NULL : new TypedObject(value.getType().getSubType().getSubType(), fieldValue);
    }

    /**
     * Gets an object from a {@link List} stored in the record.
     *
     * @param field The field name in the record that is a {@link List}.
     * @param index The position in the {@link List} that is desired.
     * @return The object at the index as a {@link TypedObject} or {@link TypedObject#NULL} if the field does not exist.
     * @throws IndexOutOfBoundsException for invalid indices.
     * @throws ClassCastException if the field is not a {@link List}.
     */
    @SuppressWarnings("unchecked")
    public TypedObject typedGet(String field, int index) {
        TypedObject value = typedGet(field);
        if (value.isNull()) {
            return TypedObject.NULL;
        }
        if (!value.isList()) {
            throw new ClassCastException(field + " is not a list. It has type " + value.getType());
        }
        List<Object> list = (List<Object>) value.getValue();
        Object listValue = list.get(index);
        return listValue == null ? TypedObject.NULL : new TypedObject(value.getType().getSubType(), listValue);
    }

    /**
     * Gets an object from a {@link List} stored in the record.
     *
     * @param field The field name in the record that is a {@link List}.
     * @param index The position in the {@link List} that is desired.
     * @param subKey The subfield of the {@link List} element that is desired.
     * @return The object at the index as a {@link TypedObject} or {@link TypedObject#NULL} if the field does not exist.
     * @throws IndexOutOfBoundsException for invalid indices.
     * @throws ClassCastException if the field is not a {@link List} or the indexed element is not a {@link Map}.
     */
    @SuppressWarnings("unchecked")
    public TypedObject typedGet(String field, int index, String subKey) {
        TypedObject value = typedGet(field);
        if (value.isNull()) {
            return TypedObject.NULL;
        }
        if (!value.isComplexList()) {
            throw new ClassCastException(field + " is not a list of maps. It has type " + value.getType());
        }
        List<Map<String, Object>> first = (List<Map<String, Object>>) value.getValue();
        Map<String, Object> second = first.get(index);
        if (second == null) {
            return TypedObject.NULL;
        }
        Object mapValue = second.get(subKey);
        return mapValue == null ? TypedObject.NULL : new TypedObject(value.getType().getSubType().getSubType(), mapValue);
    }

    // ******************************************** Setters ********************************************

    protected void validateObject(TypedObject object) {
        Objects.requireNonNull(object);
        if (object.isNull() || object.isUnknown()) {
            throw new UnsupportedOperationException("You may not set a null or unknown typed object");
        }
    }

    /**
     * Sets a field in the record as a {@link TypedObject}. This is intended to be the primary way to set fields into
     * the record.
     *
     * @param field The non-null name of the field.
     * @param object The non-null {@link TypedObject} to set.
     * @return This object for chaining.
     * @throws RuntimeException if the set cannot be done.
     */
    public BulletRecord<T> typedSet(String field, TypedObject object) {
        validateObject(object);
        return set(field, object.getValue());
    }

    /**
     * Insert a field into this BulletRecord. This is the glue method used by the other set methods and is protected to
     * not expose this to the outside, preserving type safety. It will call {@link #convert(Object)} first.
     *
     * @param field The non-null name of the field.
     * @param object The object to be set.
     * @return This object for chaining.
     */
    protected BulletRecord<T> set(String field, Object object) {
        return rawSet(field, convert(object));
    }

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
        TypedObject thatValue = that.typedGet(thatField);
        return set(field, thatValue.getValue());
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
        TypedObject thatField = that.typedGet(thatMapField, thatMapKey);
        return set(field, thatField.getValue());
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
        TypedObject thatField = that.typedGet(thatMapOfMapField, thatMapOfMapKey, thatMapKey);
        return set(field, thatField.getValue());
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
        TypedObject thatField = that.typedGet(thatListField, thatListIndex);
        return set(field, thatField.getValue());
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
        TypedObject thatField = that.typedGet(thatListField, thatListIndex, thatListMapKey);
        return set(field, thatField.getValue());
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
            rawSet(newName, getAndRemove(field));
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
}
