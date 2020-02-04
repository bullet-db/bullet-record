/*
 *  Copyright 2017, Yahoo Inc.
 *  Licensed under the terms of the Apache License, Version 2.0.
 *  See the LICENSE file associated with the project for terms.
 */
package com.yahoo.bullet.typesystem;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Getter
public enum Type {
    // Primitives
    STRING(String.class),
    BOOLEAN(Boolean.class),
    INTEGER(Integer.class),
    LONG(Long.class),
    FLOAT(Float.class),
    DOUBLE(Double.class),
    // First Order Maps - Maps of Strings to Primitives
    STRING_MAP(Map.class, Type.STRING),
    BOOLEAN_MAP(Map.class, Type.BOOLEAN),
    INTEGER_MAP(Map.class, Type.INTEGER),
    LONG_MAP(Map.class, Type.LONG),
    FLOAT_MAP(Map.class, Type.FLOAT),
    DOUBLE_MAP(Map.class, Type.DOUBLE),
    // Second Order Maps - Maps of Strings to First Order Maps
    STRING_MAP_MAP(Map.class, Type.STRING_MAP),
    BOOLEAN_MAP_MAP(Map.class, Type.BOOLEAN_MAP),
    INTEGER_MAP_MAP(Map.class, Type.INTEGER_MAP),
    LONG_MAP_MAP(Map.class, Type.LONG_MAP),
    FLOAT_MAP_MAP(Map.class, Type.FLOAT_MAP),
    DOUBLE_MAP_MAP(Map.class, Type.DOUBLE_MAP),
    // First Order Lists  - List of Primitives
    STRING_LIST(List.class, Type.STRING),
    BOOLEAN_LIST(List.class, Type.BOOLEAN),
    INTEGER_LIST(List.class, Type.INTEGER),
    LONG_LIST(List.class, Type.LONG),
    FLOAT_LIST(List.class, Type.FLOAT),
    DOUBLE_LIST(List.class, Type.DOUBLE),
    // Second Order Lists  - List of First Order Maps
    STRING_MAP_LIST(List.class, Type.STRING_MAP),
    BOOLEAN_MAP_LIST(List.class, Type.BOOLEAN_MAP),
    INTEGER_MAP_LIST(List.class, Type.INTEGER_MAP),
    LONG_MAP_LIST(List.class, Type.LONG_MAP),
    FLOAT_MAP_LIST(List.class, Type.FLOAT_MAP),
    DOUBLE_MAP_LIST(List.class, Type.DOUBLE_MAP),
    // Doesn't matter what underlyingClass is for NULL and UNKNOWN, just need something that isn't encountered
    NULL(Type.class),
    UNKNOWN(Type.class);

    public static final String NULL_EXPRESSION = "null";

    public static Set<Type> PRIMITIVES = set(BOOLEAN, INTEGER, LONG, FLOAT, DOUBLE, STRING);
    public static Set<Type> NUMERICS = set(INTEGER, LONG, FLOAT, DOUBLE);
    public static Set<Type> PRIMITIVE_MAPS = set(STRING_MAP, BOOLEAN_MAP, INTEGER_MAP, LONG_MAP, FLOAT_MAP, DOUBLE_MAP);
    public static Set<Type> COMPLEX_MAPS = set(STRING_MAP_MAP, BOOLEAN_MAP_MAP, INTEGER_MAP_MAP, LONG_MAP_MAP, FLOAT_MAP_MAP, DOUBLE_MAP_MAP);
    public static Set<Type> MAPS = set(PRIMITIVE_MAPS, COMPLEX_MAPS);
    public static Set<Type> PRIMITIVE_LISTS = set(STRING_LIST, BOOLEAN_LIST, INTEGER_LIST, LONG_LIST, FLOAT_LIST, DOUBLE_LIST);
    public static Set<Type> COMPLEX_LISTS = set(STRING_MAP_LIST, BOOLEAN_MAP_LIST, INTEGER_MAP_LIST, LONG_MAP_LIST, FLOAT_MAP_LIST, DOUBLE_MAP_LIST);
    public static Set<Type> LISTS = set(PRIMITIVE_LISTS, COMPLEX_LISTS);
    public static Set<Type> ACTUAL_TYPES = set(PRIMITIVES, MAPS, LISTS);

    private final Class underlyingClass;
    private final Type subType;

    Type(Class underlyingClass) {
        this.underlyingClass = underlyingClass;
        this.subType = null;
    }

    Type(Class underlyingClass, Type subType) {
        this.underlyingClass = underlyingClass;
        this.subType = subType;
    }

    /**
     * Tries to get the type of a given object. If the object is a complex or composite type, then it must contain
     * at least one value. Note that if the object is a composite type that contains multiple different types of
     * values, any one of them maybe used to determine the type leading to undefined behavior. It might be possible to
     * create such a composite object and then use {@link #forceCast(Type, Type, Object)} to force this object to a
     * desired type if the cast can be done (even with loss of precision).
     *
     * @param object The object whose type is to be determined.
     * @return {@link Type} for this object, the {@link Type#NULL} if the object was null or {@link Type#UNKNOWN}
     * if the type could not be determined.
     */
    public static Type getType(Object object) {
        if (object == null) {
            return NULL;
        } else if (object instanceof Map) {
            return findTypeWithSubType(MAPS, findSubType((Map) object));
        } else if (object instanceof List) {
            return findTypeWithSubType(LISTS, findSubType((List) object));
        } else {
            return getPrimitiveType(object);
        }
    }

    /**
     * Checks to see if a given string is the {@link #NULL_EXPRESSION}.
     *
     * @param string The string to check if it is null.
     * @return A boolean denoting whether the given string represented a null.
     */
    public static boolean isNullExpression(String string) {
        return NULL_EXPRESSION.compareToIgnoreCase(string) == 0;
    }

    /**
     * Takes an object and casts it to this type. Follows widening conventions for primitive numeric data. For all other
     * data, if the cast cannot be done safely, this will throw an instance {@link RuntimeException}. See
     * {@link #forceCast(Type, Object)} to force type changes when the cast cannot be done naturally.
     *
     * @param object The object that is being cast.
     * @return The casted object.
     * @throws RuntimeException if the cast cannot be done safely.
     */
    public Object castObject(Object object) {
        // Any null object can be casted to anything
        if (object == null) {
            return null;
        }
        // object isn't null and this is the UNKNOWN or the NULL type: the cast cannot be done
        if (this == UNKNOWN || this == NULL) {
            throw new ClassCastException("Cannot cast non-null object " + object + " to " + this);
        }
        Type objectType = getType(object);
        if (canSafeCast(this, objectType)) {
            return forceCast(this, objectType, object);
        }
        throw new ClassCastException("Cannot safely cast non-null object " + object + " to " + this);
    }

    /**
     * Takes a String value and casts it to this type. Only works for {@link #PRIMITIVES} {@link Type} String
     * representations. Additionally, can cast {@link #NULL_EXPRESSION} strings to objects as well. If the cast cannot
     * be done safely, an instance of {@link RuntimeException} will be thrown.
     *
     * @param value The string value that is being cast.
     * @return The casted object.
     * @throws RuntimeException if the cast cannot be done safely.
     */
    public Object forceCastString(String value) {
        switch (this) {
            case BOOLEAN:
                return Boolean.valueOf(value);
            case INTEGER:
                return Integer.valueOf(value);
            case LONG:
                return Long.valueOf(value);
            case FLOAT:
                return Float.valueOf(value);
            case DOUBLE:
                return Double.valueOf(value);
            case STRING:
                return value;
            case NULL:
                return value == null || isNullExpression(value) ? null : value;
            default:
                throw new ClassCastException("Cannot safely cast the string" + value + " to type " + this);
        }
    }

    /**
     * Attempt to force cast the Object of this {@link Type} type to the given {@link Type} castedType. Follows widening
     * conventions for primitive numeric data, including within maps and lists. However, types will be forced with
     * possible loss of precision or data in other cases that can be done. Otherwise, the cast will fail with a
     * {@link RuntimeException} when it is not possible to cast at all. See {@link #castObject(Object)} if you are only
     * interested in performing safe casts.
     *
     * Note that maps will be casted to {@link HashMap} and lists will be casted to {@link ArrayList}.
     *
     * @param castedType The {@link Type} to be casted to.
     * @param object The object to be casted.
     * @return The {@link Object} to be casted.
     * @throws RuntimeException if the cast cannot be done.
     */
    public Object forceCast(Type castedType, Object object) {
        return forceCast(castedType, this, object);
    }

    // *************************************** Type casting helpers ***************************************

    private static Object forceCast(Type targetType, Type sourceType, Object object) {
        if (object == null) {
            return null;
        }
        if (!canCast(targetType, sourceType)) {
            throw new ClassCastException("Cannot cast to " + targetType + " from " + sourceType);
        }
        switch (targetType) {
            case INTEGER:
                return castToInteger(sourceType, object);
            case BOOLEAN:
                return castToBoolean(sourceType, object);
            case STRING:
                return object.toString();
            case LONG:
                return castToLong(sourceType, object);
            case FLOAT:
                return castToFloat(sourceType, object);
            case DOUBLE:
                return castToDouble(sourceType, object);
            case STRING_MAP:
            case BOOLEAN_MAP:
            case INTEGER_MAP:
            case LONG_MAP:
            case FLOAT_MAP:
            case DOUBLE_MAP:
            case STRING_MAP_MAP:
            case BOOLEAN_MAP_MAP:
            case INTEGER_MAP_MAP:
            case LONG_MAP_MAP:
            case FLOAT_MAP_MAP:
            case DOUBLE_MAP_MAP:
                return castToMap(targetType, sourceType, object);
            case STRING_LIST:
            case BOOLEAN_LIST:
            case INTEGER_LIST:
            case LONG_LIST:
            case FLOAT_LIST:
            case DOUBLE_LIST:
            case STRING_MAP_LIST:
            case BOOLEAN_MAP_LIST:
            case INTEGER_MAP_LIST:
            case LONG_MAP_LIST:
            case FLOAT_MAP_LIST:
            case DOUBLE_MAP_LIST:
                return castToList(targetType, sourceType, object);
            default:
                // Unreachable because canCast will cover these other enums
                throw new UnsupportedOperationException("Nothing can be casted to " + targetType);
        }
    }

    private static Integer castToInteger(Type type, Object object) {
        switch (type) {
            case INTEGER:
                return (Integer) object;
            case LONG:
                return ((Long) object).intValue();
            case FLOAT:
                return (int) (((Float) object).floatValue());
            case DOUBLE:
                return (int) (((Double) object).doubleValue());
            case STRING:
                return (int) (Double.parseDouble((String) object));
            case BOOLEAN:
                return ((Boolean) object) ? 1 : 0;
            default:
                throw new ClassCastException("Can not cast to Integer from type: " + type);
        }
    }

    private static Long castToLong(Type type, Object object) {
        switch (type) {
            case INTEGER:
                return ((Integer) object).longValue();
            case LONG:
                return (Long) object;
            case FLOAT:
                return (long) (((Float) object).floatValue());
            case DOUBLE:
                return (long) (((Double) object).doubleValue());
            case STRING:
                return (long) (Double.parseDouble((String) object));
            case BOOLEAN:
                return ((Boolean) object) ? 1L : 0L;
            default:
                throw new ClassCastException("Can not cast to Long from type: " + type);
        }
    }

    private static Float castToFloat(Type type, Object object) {
        switch (type) {
            case INTEGER:
                return (float) (Integer) object;
            case LONG:
                return (float) (Long) object;
            case FLOAT:
                return (Float) object;
            case DOUBLE:
                return ((Double) object).floatValue();
            case STRING:
                return (float) (Double.parseDouble((String) object));
            case BOOLEAN:
                return ((Boolean) object) ? 1.0f : 0.0f;
            default:
                throw new ClassCastException("Can not cast to Float from type: " + type);
        }
    }

    private static Double castToDouble(Type type, Object object) {
        switch (type) {
            case INTEGER:
                return (double) (Integer) object;
            case LONG:
                return (double) (Long) object;
            case FLOAT:
                return ((Float) object).doubleValue();
            case DOUBLE:
                return (Double) object;
            case STRING:
                return Double.parseDouble((String) object);
            case BOOLEAN:
                return ((Boolean) object) ? 1.0 : 0.0;
            default:
                throw new ClassCastException("Can not cast to Double from type: " + type);
        }
    }

    private static Boolean castToBoolean(Type type, Object object) {
        switch (type) {
            case INTEGER:
                return (Integer) object != 0;
            case LONG:
                return ((Long) object) != 0;
            case FLOAT:
                return ((Float) object) != 0;
            case DOUBLE:
                return ((Double) object) != 0;
            case STRING:
                return Boolean.parseBoolean((String) object);
            case BOOLEAN:
                return (Boolean) object;
            default:
                throw new ClassCastException("Can not cast to Boolean from type: " + type);
        }
    }

    private static Map castToMap(Type targetMapType, Type sourceType, Object object) {
        if (!MAPS.contains(sourceType)) {
            throw new ClassCastException("Cannot cast non-map" + object + " to a Map");
        }
        // sourceType is in MAPS, i.e. object is a map
        Map asMap = (Map) object;
        Map map = new HashMap();
        for (Object entryObject : asMap.entrySet()) {
            Map.Entry entry = (Map.Entry) entryObject;
            // We'll cast keys to strings just in case as well
            Object key = entry.getKey();
            String keyString = key == null ? null : key.toString();
            map.put(keyString, forceCast(targetMapType.subType, sourceType.subType, entry.getValue()));
        }
        return map;
    }

    private static List castToList(Type targetListType, Type sourceType, Object object) {
        if (!LISTS.contains(sourceType)) {
            throw new ClassCastException("Cannot cast non-list" + object + " to a List");
        }
        // sourceType is in LISTS, i.e. object is a list
        List asList = (List) object;
        List list = new ArrayList();
        for (Object member : asList) {
            list.add(forceCast(targetListType.subType, sourceType.subType, member));
        }
        return list;
    }


    private static boolean canCast(Type finalType, Type initialType) {
        if (finalType == initialType) {
            return true;
        }
        // Only going across nesting levels we can't do
        return areBothIn(initialType, finalType, PRIMITIVES) ||
               areBothIn(initialType, finalType, PRIMITIVE_MAPS) || areBothIn(initialType, finalType, COMPLEX_MAPS) ||
               areBothIn(initialType, finalType, PRIMITIVE_LISTS) || areBothIn(initialType, finalType, COMPLEX_LISTS);
    }

    private static boolean canSafeCast(Type finalType, Type initialType) {
        if (finalType == initialType) {
            return true;
        }
        switch (finalType) {
            case STRING:
                // Only let primitives be converted to Strings since we do support primitives from Strings
                return PRIMITIVES.contains(initialType);
            case LONG:
                return initialType == INTEGER;
            case LONG_MAP:
                return initialType == INTEGER_MAP;
            case LONG_MAP_MAP:
                return initialType == INTEGER_MAP_MAP;
            case LONG_MAP_LIST:
                return initialType == INTEGER_MAP_LIST;
            case DOUBLE:
                return initialType == FLOAT;
            case DOUBLE_MAP:
                return initialType == FLOAT_MAP;
            case DOUBLE_LIST:
                return initialType == FLOAT_LIST;
            case DOUBLE_MAP_LIST:
                return initialType == FLOAT_MAP_LIST;
            default:
                return false;
        }
    }

    private static boolean areBothIn(Type a, Type b, Set<Type> set) {
        return set.contains(a) && set.contains(b);
    }

    // *************************************** Type finding helpers ***************************************

    private static Type getPrimitiveType(Object object) {
        for (Type type : PRIMITIVES) {
            if (type.getUnderlyingClass().isInstance(object)) {
                return type;
            }
        }
        return UNKNOWN;
    }

    private static Type findSubType(Map map) {
        if (map.isEmpty() || !hasStringKeys(map)) {
            return UNKNOWN;
        }
        return findNestedValueType(anyNonNullValue(map));
    }

    private static Type findSubType(List list) {
        if (list.isEmpty()) {
            return UNKNOWN;
        }
        return findNestedValueType(anyNonNullValue(list));
    }

    private static Type findNestedValueType(Optional<Object> nestedValue) {
        if (!nestedValue.isPresent()) {
            return UNKNOWN;
        }
        Object value = nestedValue.get();
        if (value instanceof Map) {
            // Only have Map of Map of Primitives or List of Map of Primitives, so the type has to be in Primitive Maps
            return findTypeWithSubType(PRIMITIVE_MAPS, findSubType((Map) value));
        } else {
            return getPrimitiveType(value);
        }
    }

    private static Type findTypeWithSubType(Collection<Type> types, Type subType) {
        return types.stream().map(Type::getSubType).filter(subType::equals).findFirst().orElse(UNKNOWN);
    }

    private static boolean hasStringKeys(Map map) {
        return map.keySet().stream().allMatch(o -> o instanceof String || o == null);
    }

    private static Optional<Object> anyNonNullValue(Map map) {
        return map.values().stream().filter(Objects::nonNull).findAny();
    }

    private static Optional<Object> anyNonNullValue(List list) {
        return list.stream().filter(Objects::nonNull).findAny();
    }

    // *************************************** Type collection builders ***************************************

    private static Set<Type> set(Type... types) {
        return new HashSet<>(Arrays.asList(types));
    }

    @SafeVarargs
    private static Set<Type> set(Set<Type>... types) {
        Set<Type> result = new HashSet<>();
        for (Set<Type> typeSet : types) {
            result.addAll(typeSet);
        }
        return result;
    }
}
