/*
 *  Copyright 2020, Yahoo Inc.
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
    private final Object value;

    public static final Predicate<TypedObject> IS_PRIMITIVE_OR_NULL = (t) -> t.getType() == Type.NULL || Type.PRIMITIVES.contains(t.getType());
    public static final Predicate<TypedObject> IS_NOT_NULL = (t) -> t.getType() != Type.NULL;
    public static final TypedObject UNKNOWN = new TypedObject(Type.UNKNOWN, null);
    public static final TypedObject NULL = new TypedObject(Type.NULL, null);

    /**
     * Constructor that wraps an Object into a type. See {@link Type#getType(Object)} to see how the type of the
     * corresponding object will be determined.
     *
     * @param value The value who is being wrapped.
     */
    public TypedObject(Object value) {
        this(Type.getType(value), value);
    }

    /**
     * Create a TypedObject with the given non-null type. Note that the value is not validated to be of that type. If
     * it is not, all operation results are undefined. You should use {@link Type#cast(Object)} or
     * {@link Type#forceCast(Type, Object)} to force the value to the desired type if it is not.
     *
     * @param type The type of the value.
     * @param value The value being wrapped.
     */
    public TypedObject(Type type, Object value) {
        Objects.requireNonNull(type);
        this.type = type;
        this.value = value;
    }

    /**
     * Returns true if this does not have an actual type, i.e. type is {@link Type#UNKNOWN}.
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
     * Force cast this object to the given {@link Type} castedType. Will return a new {@link TypedObject}.
     *
     * @param castedType The {@link Type} to be casted to.
     * @return The casted {@link TypedObject}.
     */
    public TypedObject forceCast(Type castedType) {
        return new TypedObject(castedType, type.forceCast(castedType, value));
    }

    /**
     * Returns true if this equals the specified object. The object can be a {@link TypedObject} or be constructed as
     * a {@link TypedObject}. Note that this is not the same as {@link #equals(Object)}. This will do a safe cast to
     * unify {@link Type#NUMERICS} and compare. This will basically use {@link #compareTo(TypedObject)} and check if the
     * result is 0.
     *
     * @param target The object to compare to.
     * @return A boolean to indicate if this equals the specified object.
     */
    public boolean equalTo(TypedObject target) {
        return compareTo(target) == 0;
    }

    /**
     * Compares this TypedObject to another. Only works on objects that have a type in {@link Type#PRIMITIVES}.
     * An exception will be thrown when comparing objects whose types are not in {@link Type#PRIMITIVES}, excepting
     * {@link Type#NULL}. This will force {@link Type#NUMERICS} that are not of the same {@link Type} to {@link Double}
     * instances to compare them.
     * {@inheritDoc}
     *
     * @param other The other non-null TypedObject.
     * @return {@inheritDoc}
     * @throws UnsupportedOperationException if the other object could not compared to this.
     */
    @Override
    public int compareTo(TypedObject other) {
        if (!Type.canCompare(type, other.type)) {
            throw new UnsupportedOperationException("Types are not comparable for " + this + " with " + other);
        }
        // Both are NULL
        if (type == Type.NULL) {
            return 0;
        }
        // If the types are not the same, not NULL and they can be compared, they must be numeric
        if (type != other.type) {
            return Double.compare(((Number) value).doubleValue(), ((Number) other.value).doubleValue());
        }
        // Types are the same and are primitive, but they aren't NULL or UNKNOWN
        switch (type) {
            case BOOLEAN:
                return ((Boolean) value).compareTo((Boolean) other.value);
            case INTEGER:
                return ((Integer) value).compareTo((Integer) other.value);
            case LONG:
                return ((Long) value).compareTo((Long) other.value);
            case FLOAT:
                return ((Float) value).compareTo((Float) other.value);
            case DOUBLE:
                return ((Double) value).compareTo((Double) other.value);
            default:
                return value.toString().compareTo((String) other.value);
        }
    }

    @Override
    public String toString() {
        return value + "::" + type;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof TypedObject)) {
            return false;
        }
        TypedObject o = (TypedObject) other;
        return o.type == type && (value == null && o.value == null || value != null && value.equals(o.value));
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, value);
    }

    /**
     * Takes an object and returns a casted TypedObject according to the given type. Note that this only casts safely.
     * It can widen numeric types if loss of precision does not occur. See {@link Type#cast(Object)}.
     *
     * @param type The {@link Type} to cast the values to.
     * @param object The Object that is being cast.
     * @return The casted TypedObject with the {@link Type} or {@link TypedObject#UNKNOWN} if the cast failed.
     */
    public static TypedObject safeCastFromObject(Type type, Object object) {
        // No longer makes a UNKNOWN object if object was null
        try {
            return new TypedObject(type, type.cast(object));
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
    public static TypedObject forceCastStringToNumber(Object value) {
        if (value == null) {
            return UNKNOWN;
        }
        try {
            return new TypedObject(Type.DOUBLE, Type.STRING.forceCast(Type.DOUBLE, value.toString()));
        } catch (RuntimeException e) {
            return UNKNOWN;
        }
    }

    private boolean equalTo(Object object) {
        return equalTo(new TypedObject(object));
    }

    private static boolean containsValueInPrimitiveMap(Map<?, ?> map, TypedObject target) {
        return map.values().stream().anyMatch(target::equalTo);
    }
}
