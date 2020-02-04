/*
 *  Copyright 2017, Yahoo Inc.
 *  Licensed under the terms of the Apache License, Version 2.0.
 *  See the LICENSE file associated with the project for terms.
 */
package com.yahoo.bullet.typesystem;

import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

@Getter
public class TypedObject implements Comparable<TypedObject> {
    private final Type type;
    // value is undefined if type is Type.UNKNOWN
    private final Object value;

    public static final Predicate<TypedObject> IS_PRIMITIVE_OR_NULL = (t) -> t.getType() == Type.NULL || Type.PRIMITIVES.contains(t.getType());
    public static final Predicate<TypedObject> IS_NOT_NULL = (t) -> t.getType() != Type.NULL;
    public static final TypedObject UNKNOWN = new TypedObject(Type.UNKNOWN, null);
    public static final TypedObject NULL = new TypedObject(Type.NULL, null);

    private static final String NULL_EXPRESSION = "null";

    /**
     * Constructor that wraps an Object into a type.
     *
     * @param value The value who is being wrapped.
     */
    public TypedObject(Object value) {
        this(Type.getType(value), value);
    }

    /**
     * Create a TypedObject with the given non-null type.
     *
     * @param type The type of the value.
     * @param value The payload.
     */
    public TypedObject(Type type, Object value) {
        Objects.requireNonNull(type);
        this.type = type;
        this.value = value;
    }

    /**
     * Returns true if this does not have an actual type (type is {@link Type#UNKNOWN}).
     *
     * @return A boolean denoting if the type is {@link Type#UNKNOWN}.
     */
    public boolean isUnknown() {
        return Type.isUnknown(type);
    }

    /**
     * Returns true if the given type is the {@link Type#NULL} type.
     *
     * @return A boolean denoting if the type is {@link Type#NULL}.
     */
    public boolean isNull() {
        return Type.isNull(type);
    }

    /**
     * Returns true if the given type is in {@link Type#PRIMITIVES}.
     *
     * @return A boolean denoting if the type is primitive.
     */
    public boolean isPrimitive() {
        return Type.isPrimitive(type);
    }

    /**
     * Returns true if the type is in {@link Type#PRIMITIVE_MAPS}.
     *
     * @return A boolean denoting if the type is a primitive map.
     */
    public boolean isPrimitiveMap() {
        return Type.isPrimitiveMap(type);
    }

    /**
     * Returns true if the type is in {@link Type#COMPLEX_MAPS}.
     *
     * @return A boolean denoting if the type is a complex map.
     */
    public boolean isComplexMap() {
        return Type.isComplexMap(type);
    }

    /**
     * Returns true if the type is in {@link Type#MAPS}.
     *
     * @return A boolean denoting if the type is a map.
     */
    public boolean isMap() {
        return Type.isMap(type);
    }

    /**
     * Returns true if the type is in {@link Type#PRIMITIVE_LISTS}.
     *
     * @return A boolean denoting if the type is a primitive list.
     */
    public boolean isPrimitiveList() {
        return Type.isPrimitiveList(type);
    }

    /**
     * Returns true if the type is in {@link Type#COMPLEX_LISTS}.
     *
     * @return A boolean denoting if the type is a complex list.
     */
    public boolean isComplexList() {
        return Type.isComplexList(type);
    }

    /**
     * Returns true if the type is in {@link Type#LISTS}.
     *
     * @return A boolean denoting if the type is a list.
     */
    public boolean isList() {
        return Type.isList(type);
    }

    /**
     * Takes an object and returns a casted TypedObject according to this type. Note that this only casts safely. It can
     * widen numeric types if loss of precision does not occur. It will not handle null representation of Strings and
     * convert them to nulls.
     *
     * @param object The Object that is being cast.
     * @return The casted TypedObject with this {@link Type} or {@link TypedObject#UNKNOWN} if the cast failed.
     */
    public TypedObject safeCastFromObject(Object object) {
        return safeCastFromObject(type, object);
    }

    /**
     * Force cast to the {@link TypedObject} with given {@link Type} castedType.
     *
     * @param castedType The {@link Type} to be casted to
     * @return The casted {@link TypedObject}
     */
    public TypedObject forceCast(Type castedType) {
        return new TypedObject(castedType, type.forceCast(castedType, value));
    }

    /**
     * Get the size of the value. Currently only {@link Type#LISTS}, {@link Type#MAPS} and {@link Type#STRING} support
     * getting a size.
     *
     * @return The size of the value.
     * @throws UnsupportedOperationException if not supported.
     */
    public int size() {
        if (isList()) {
            return ((List) value).size();
        } else if (isMap()) {
            return ((Map) value).size();
        } else if (type == Type.STRING) {
            return ((String) value).length();
        }
        throw new UnsupportedOperationException("This type does not support getting a size: " + type);
    }

    /**
     * Returns true if the value or its underlying values contain a mapping for the specified key. Only
     * {@link Type#COMPLEX_LISTS} and {@link Type#MAPS} support getting a mapping.
     *
     * @param key The key to be tested.
     * @return A Boolean to indicate if the value or its underlying values contain a mapping for the specified key.
     * @throws UnsupportedOperationException if not supported.
     */
    @SuppressWarnings("unchecked")
    public boolean containsKey(String key) {
        if (isComplexList()) {
            return ((List) value).stream().anyMatch(e -> ((Map) e).containsKey(key));
        } else if (isComplexMap()) {
            Map map = (Map) value;
            return map.containsKey(key) || map.values().stream().anyMatch(e -> ((Map) e).containsKey(key));
        } else if (isPrimitiveMap()) {
            Map map = (Map) value;
            return map.containsKey(key);
        }
        throw new UnsupportedOperationException("This type does not support mappings: " + type);
    }

    /**
     * Returns true if the value or its underlying values contain the specified value. Only LIST and MAP are supported.
     *
     * @param target The target {@link TypedObject} to be tested.
     * @return A Boolean to indicate if the value or its underlying values contain the specified value.
     * @throws UnsupportedOperationException if not supported.
     */
    @SuppressWarnings("unchecked")
    public boolean containsValue(TypedObject target) {
        if (isPrimitiveList()) {
            return ((List) value).stream().anyMatch(target::equalTo);
        } else if (isComplexList()) {
            return ((List) value).stream().anyMatch(e -> containsValueInPrimitiveMap((Map) e, target));
        } else if (isPrimitiveMap()) {
            return ((Map) value).values().stream().anyMatch(target::equalTo);
        } else if (isComplexMap()) {
            return ((Map) value).values().stream().anyMatch(e -> containsValueInPrimitiveMap((Map) e, target));

        }
        throw new UnsupportedOperationException("This type of field does not support contains value: " + type);
    }

    /**
     * Returns true if this equals the specified object. The object can be a {@link TypedObject} or be constructed as
     * a {@link TypedObject}.
     *
     * @param object The object to compare to.
     * @return A boolean to indicate if this equals the specified object.
     */
    public boolean equalTo(Object object) {
        TypedObject target = object instanceof TypedObject ? (TypedObject) object : new TypedObject(object);
        return compareTo(target) == 0;
    }

    /**
     * Compares this TypedObject to another. If this object has a null value and the other does not, returns a
     * {@link Integer#MIN_VALUE}. If this has an {@link Type#UNKNOWN} type, returns {@link Integer#MIN_VALUE}. Only
     * works on objects that have a type in {@link Type#PRIMITIVES}. Note that this will not cast at all and forcefully
     * interpret the other value as an object of the same type, which might result in a {@link RuntimeException}.
     * {@inheritDoc}
     *
     * @param o The other non-null TypedObject.
     * @return {@inheritDoc}
     * @throws RuntimeException if the other object could not compared to this.
     */
    @Override
    public int compareTo(TypedObject o) {
        Objects.requireNonNull(o);
        // If type casting/unification needs to happen, it should go here. Assume this.type == o.type for now
        switch (type) {
            case STRING:
                return value.toString().compareTo((String) o.value);
            case BOOLEAN:
                return ((Boolean) value).compareTo((Boolean) o.value);
            case INTEGER:
                return ((Integer) value).compareTo((Integer) o.value);
            case LONG:
                return ((Long) value).compareTo((Long) o.value);
            case FLOAT:
                return ((Float) value).compareTo((Float) o.value);
            case DOUBLE:
                return ((Double) value).compareTo((Double) o.value);
            case NULL:
                // Return Integer.MIN_VALUE if the type isn't null. We could throw an exception instead.
                return o.value == null ? 0 : Integer.MIN_VALUE;
            case UNKNOWN:
                return Integer.MIN_VALUE;
            default:
                throw new RuntimeException("Unsupported type cannot be compared: " + type);
        }
    }

    @Override
    public String toString() {
        return type == Type.NULL ? NULL_EXPRESSION : value.toString();
    }

    /**
     * Takes an object and returns a casted TypedObject according to this type.
     *
     * @param type The {@link Type} to cast the values to.
     * @param object The Object that is being cast.
     * @return The casted TypedObject with the {@link Type} or {@link TypedObject#UNKNOWN} if the cast failed.
     */
    public static TypedObject safeCastFromObject(Type type, Object object) {
        // No longer makes a UNKNOWN object if object was null
        try {
            return new TypedObject(type, type.castObject(object));
        } catch (RuntimeException e) {
            return UNKNOWN;
        }
    }

    /**
     * Takes a non-null value and returns a numeric TypedObject - it has a type in {@link Type#NUMERICS}. The value
     * is then a {@link Number}. It uses the String representation of the object to cast it.
     *
     * @param value The Object value that is being cast to a numeric.
     * @return The casted TypedObject with the type set to numeric or {@link TypedObject#UNKNOWN} if not.
     */
    public static TypedObject forceCastAsNumber(Object value) {
        if (value == null) {
            return UNKNOWN;
        }
        try {
            return TypedObject.forceCast(Type.DOUBLE, value.toString());
        } catch (RuntimeException e) {
            return UNKNOWN;
        }
    }

    /**
     * Force cast the value String to the {@link TypedObject} with given {@link Type} castedType.
     *
     * @param castedType The {@link Type} to be casted to
     * @param value The value String to be casted.
     * @return The casted {@link TypedObject}
     */
    public static TypedObject forceCast(Type castedType, String value) {
        return new TypedObject(castedType, Type.STRING.forceCast(castedType, value));
    }

    private static boolean containsValueInPrimitiveMap(Map<?, ?> map, TypedObject target) {
        return map.values().stream().anyMatch(target::equalTo);
    }
}
