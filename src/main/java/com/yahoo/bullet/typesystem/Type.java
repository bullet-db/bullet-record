/*
 *  Copyright 2020, Yahoo Inc.
 *  Licensed under the terms of the Apache License, Version 2.0.
 *  See the LICENSE file associated with the project for terms.
 */
package com.yahoo.bullet.typesystem;

import lombok.Getter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Stores the various types that can be understood by the type system in Bullet.
 */
@Getter
public enum Type {
    // Doesn't matter what underlyingClass is for NULL, just need something that isn't encountered
    NULL(Type.class),
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
    // Doesn't matter what underlyingClass is for UNKNOWN, just need something that isn't encountered
    UNKNOWN(Type.class);

    /**
     * A {@link Set} of the {@link #BOOLEAN}, {@link #INTEGER} {@link #LONG}, {@link #FLOAT}, {@link #DOUBLE} and
     * {@link #STRING} types.
     */
    public static Set<Type> PRIMITIVES = set(BOOLEAN, INTEGER, LONG, FLOAT, DOUBLE, STRING);

    /**
     * A {@link Set} of the {@link #INTEGER} {@link #LONG}, {@link #FLOAT} and {@link #DOUBLE} types.
     */
    public static Set<Type> NUMERICS = set(INTEGER, LONG, FLOAT, DOUBLE);

    /**
     * A {@link Set} of the {@link #BOOLEAN_MAP}, {@link #INTEGER_MAP} {@link #LONG_MAP}, {@link #FLOAT_MAP},
     * {@link #DOUBLE_MAP} and {@link #STRING_MAP} types.
     */
    public static Set<Type> PRIMITIVE_MAPS = set(STRING_MAP, BOOLEAN_MAP, INTEGER_MAP, LONG_MAP, FLOAT_MAP, DOUBLE_MAP);

    /**
     * A {@link Set} of the {@link #BOOLEAN_MAP_MAP}, {@link #INTEGER_MAP_MAP} {@link #LONG_MAP_MAP},
     * {@link #FLOAT_MAP_MAP}, {@link #DOUBLE_MAP_MAP} and {@link #STRING_MAP_MAP} types.
     */
    public static Set<Type> COMPLEX_MAPS = set(STRING_MAP_MAP, BOOLEAN_MAP_MAP, INTEGER_MAP_MAP, LONG_MAP_MAP, FLOAT_MAP_MAP, DOUBLE_MAP_MAP);

    /**
     * A {@link Set} of the {@link #PRIMITIVE_MAPS}, {@link #COMPLEX_MAPS} types.
     */
    public static Set<Type> MAPS = set(PRIMITIVE_MAPS, COMPLEX_MAPS);

    /**
     * A {@link Set} of the {@link #BOOLEAN_LIST}, {@link #INTEGER_LIST} {@link #LONG_LIST}, {@link #FLOAT_LIST},
     * {@link #DOUBLE_LIST} and {@link #STRING_LIST} types.
     */
    public static Set<Type> PRIMITIVE_LISTS = set(STRING_LIST, BOOLEAN_LIST, INTEGER_LIST, LONG_LIST, FLOAT_LIST, DOUBLE_LIST);

    /**
     * A {@link Set} of the {@link #BOOLEAN_MAP_LIST}, {@link #INTEGER_MAP_LIST} {@link #LONG_MAP_LIST},
     * {@link #FLOAT_MAP_LIST}, {@link #DOUBLE_MAP_LIST} and {@link #STRING_MAP_LIST} types.
     */
    public static Set<Type> COMPLEX_LISTS = set(STRING_MAP_LIST, BOOLEAN_MAP_LIST, INTEGER_MAP_LIST, LONG_MAP_LIST, FLOAT_MAP_LIST, DOUBLE_MAP_LIST);

    /**
     * A {@link Set} of the {@link #PRIMITIVE_LISTS} and {@link #COMPLEX_LISTS} types.
     */
    public static Set<Type> LISTS = set(PRIMITIVE_LISTS, COMPLEX_LISTS);

    /**
     * A {@link Set} of the {@link #NULL}, {@link #PRIMITIVES}, {@link #MAPS} and {@link #LISTS} types.
     */
    public static Set<Type> ACTUAL_TYPES = set(set(NULL), PRIMITIVES, MAPS, LISTS);

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
     * at least one value that also satisfies this property if it too is a complex type. If not, the whole type is
     * derived to be {@link Type#UNKNOWN} and not {@link Type#LISTS} of {@link Type#UNKNOWN} or {@link Type#MAPS} of
     * {@link Type#UNKNOWN}. Note that if the object is a composite type that contains multiple different types of
     * values, any one of them maybe used to determine the type leading to undefined behavior. It might be possible to
     * create such a composite object and then use {@link #forceCast(Type, Type, Serializable)} to force this object to
     * a desired type if the cast can be done (even with loss of precision).
     *
     * @param object The object whose type is to be determined.
     * @return {@link Type} for this object, the {@link Type#NULL} if the object was null or {@link Type#UNKNOWN}
     *         if the type could not be determined.
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
     * Takes an object and casts it to this type. Follows widening conventions for primitive numeric data. For all other
     * data, if the cast cannot be done safely, this will throw an instance {@link RuntimeException}. See
     * {@link #forceCast(Type, Serializable)} to force type changes when the cast cannot be done naturally.
     *
     * @param object The object that is being cast.
     * @return The casted {@link Serializable} object.
     * @throws RuntimeException if the cast cannot be done safely.
     */
    public Serializable cast(Serializable object) {
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
     * Attempt to force cast the {@link Serializable} object of this {@link Type} type to the given {@link Type}
     * castedType. Follows widening conventions for primitive numeric data, including within maps and lists. However,
     * types will be forced with possible loss of precision or data in other cases that can be done. Otherwise, the cast
     * will fail with a {@link RuntimeException} when it is not possible to cast at all. See {@link #cast(Serializable)}
     * if you are only interested in performing safe casts.
     *
     * Note that maps will be casted to {@link HashMap} and lists will be casted to {@link ArrayList}.
     *
     * @param castedType The {@link Type} to be casted to.
     * @param object The object to be casted.
     * @return The {@link Serializable} result of the cast.
     * @throws RuntimeException if the cast cannot be done.
     */
    public Serializable forceCast(Type castedType, Serializable object) {
        return forceCast(castedType, this, object);
    }

    // *************************************** Type classification methods ***************************************

    /**
     * Returns true if the given type has an unknown type.
     *
     * @param type The type.
     * @return A boolean denoting if the type is {@link Type#UNKNOWN}.
     */
    public static boolean isUnknown(Type type) {
        return type == Type.UNKNOWN;
    }

    /**
     * Returns true if the given type is the {@link Type#NULL} type.
     *
     * @param type The type.
     * @return A boolean denoting if the type is {@link Type#NULL}.
     */
    public static boolean isNull(Type type) {
        return type == Type.NULL;
    }

    /**
     * Returns true if the given type is in {@link Type#PRIMITIVES}.
     *
     * @param type The type.
     * @return A boolean denoting if the type is primitive.
     */
    public static boolean isPrimitive(Type type) {
        return PRIMITIVES.contains(type);
    }

    /**
     * Returns true if the given type is a numeric primitive type. See {@link Type#NUMERICS}.
     *
     * @param type The type.
     * @return A boolean denoting if the type is a numeric primitive.
     */
    public static boolean isNumeric(Type type) {
        return NUMERICS.contains(type);
    }

    /**
     * Returns true if the given type is in {@link Type#PRIMITIVE_MAPS}.
     *
     * @param type The type.
     * @return A boolean denoting if the type is a primitive map.
     */
    public static boolean isPrimitiveMap(Type type) {
        return PRIMITIVE_MAPS.contains(type);
    }

    /**
     * Returns true if the given type is in {@link Type#COMPLEX_MAPS}.
     *
     * @param type The type.
     * @return A boolean denoting if the type is a complex map.
     */
    public static boolean isComplexMap(Type type) {
        return COMPLEX_MAPS.contains(type);
    }

    /**
     * Returns true if the given type is in {@link Type#MAPS}.
     *
     * @param type The type.
     * @return A boolean denoting if the type is a map.
     */
    public static boolean isMap(Type type) {
        return MAPS.contains(type);
    }

    /**
     * Returns true if the given type is in {@link Type#PRIMITIVE_LISTS}.
     *
     * @param type The type.
     * @return A boolean denoting if the type is a primitive list.
     */
    public static boolean isPrimitiveList(Type type) {
        return PRIMITIVE_LISTS.contains(type);
    }

    /**
     * Returns true if the given type is in {@link Type#COMPLEX_LISTS}.
     *
     * @param type The type.
     * @return A boolean denoting if the type is a complex list.
     */
    public static boolean isComplexList(Type type) {
        return COMPLEX_LISTS.contains(type);
    }

    /**
     * Returns true if the given type is in {@link Type#LISTS}.
     *
     * @param type The type.
     * @return A boolean denoting if the type is a list.
     */
    public static boolean isList(Type type) {
        return LISTS.contains(type);
    }

    // ********************************** Type casting and operation helpers ***********************************

    /**
     * Check if it is possible to compare the given {@link Type}. It will allow comparing only {@link Type#PRIMITIVES}.
     *
     * @param first The first non-null {@link Type}.
     * @param second The second non-null {@link Type}.
     * @return A boolean denoting if the comparison can be done.
     */
    public static boolean canCompare(Type first, Type second) {
        // Null Types can
        if (first == NULL && second == NULL) {
            return true;
        }
        // UNKNOWNS and non-Primitives Types cannot
        if (!areBothIn(first, second, PRIMITIVES)) {
            return false;
        }
        // Equal Type Primitives can
        if (first == second) {
            return true;
        }
        // Non-equal Numerics can
        return areBothIn(first, second, NUMERICS);
    }

    /**
     * Check if it is possible to cast to the given {@link Type} from the given {@link Type}. This cast does not make
     * sure that information is not lost. If it is possible to do the cast, this will return true.
     *
     * @param finalType The result type of the cast.
     * @param initialType The source type to cast from.
     * @return A boolean denoting if the cast can be done at all.
     */
    public static boolean canForceCast(Type finalType, Type initialType) {
        if (finalType == initialType) {
            return true;
        }
        // Only going across nesting levels we can't do
        return areBothIn(initialType, finalType, PRIMITIVES) ||
               areBothIn(initialType, finalType, PRIMITIVE_MAPS) || areBothIn(initialType, finalType, COMPLEX_MAPS) ||
               areBothIn(initialType, finalType, PRIMITIVE_LISTS) || areBothIn(initialType, finalType, COMPLEX_LISTS);
    }

    /**
     * Checks if it possible to cast to the given {@link Type} from the given {@link Type} safely. This follows the
     * widening convention for {@link Type#NUMERICS}.
     *
     * @param finalType The result type of the cast.
     * @param initialType The source type to cast from.
     * @return A boolean denoting if the cast can be done safely.
     */
    public static boolean canSafeCast(Type finalType, Type initialType) {
        if (finalType == initialType) {
            return true;
        }
        switch (finalType) {
            case STRING:
                return PRIMITIVES.contains(initialType);
            case LONG:
                return initialType == INTEGER;
            case LONG_MAP:
                return initialType == INTEGER_MAP;
            case LONG_LIST:
                return initialType == INTEGER_LIST;
            case LONG_MAP_MAP:
                return initialType == INTEGER_MAP_MAP;
            case LONG_MAP_LIST:
                return initialType == INTEGER_MAP_LIST;
            case DOUBLE:
                return NUMERICS.contains(initialType);
            case DOUBLE_MAP:
                return PRIMITIVE_MAPS.contains(initialType) && canSafeCast(DOUBLE, initialType.getSubType());
            case DOUBLE_LIST:
                return PRIMITIVE_LISTS.contains(initialType) && canSafeCast(DOUBLE, initialType.getSubType());
            case DOUBLE_MAP_MAP:
                return COMPLEX_MAPS.contains(initialType) && canSafeCast(DOUBLE_MAP, initialType.getSubType());
            case DOUBLE_MAP_LIST:
                return COMPLEX_LISTS.contains(initialType) && canSafeCast(DOUBLE_MAP, initialType.getSubType());
            default:
                return false;
        }
    }

    private static Serializable forceCast(Type targetType, Type sourceType, Serializable object) {
        if (object == null) {
            return null;
        }
        if (canForceCast(targetType, sourceType)) {
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
            }
        }
        throw new ClassCastException("Cannot cast to " + targetType + " from " + sourceType);
    }

    private static Integer castToInteger(Type type, Serializable object) {
        Integer result = null;
        switch (type) {
            case INTEGER:
                result = (Integer) object;
                break;
            case LONG:
                result = ((Long) object).intValue();
                break;
            case FLOAT:
                result = (int) (((Float) object).floatValue());
                break;
            case DOUBLE:
                result = (int) (((Double) object).doubleValue());
                break;
            case STRING:
                result = (int) (Double.parseDouble((String) object));
                break;
            case BOOLEAN:
                result = ((Boolean) object) ? 1 : 0;
                break;
        }
        return result;
    }

    private static Long castToLong(Type type, Serializable object) {
        Long result = null;
        switch (type) {
            case INTEGER:
                result = ((Integer) object).longValue();
                break;
            case LONG:
                result = (Long) object;
                break;
            case FLOAT:
                result = (long) (((Float) object).floatValue());
                break;
            case DOUBLE:
                result = (long) (((Double) object).doubleValue());
                break;
            case STRING:
                result = (long) (Double.parseDouble((String) object));
                break;
            case BOOLEAN:
                result = ((Boolean) object) ? 1L : 0L;
                break;
        }
        return result;
    }

    private static Float castToFloat(Type type, Serializable object) {
        Float result = null;
        switch (type) {
            case INTEGER:
                result = (float) (Integer) object;
                break;
            case LONG:
                result = (float) (Long) object;
                break;
            case FLOAT:
                result = (Float) object;
                break;
            case DOUBLE:
                result = ((Double) object).floatValue();
                break;
            case STRING:
                result = (float) (Double.parseDouble((String) object));
                break;
            case BOOLEAN:
                result = ((Boolean) object) ? 1.0f : 0.0f;
                break;
        }
        return result;
    }

    private static Double castToDouble(Type type, Serializable object) {
        Double result = null;
        switch (type) {
            case INTEGER:
                result = (double) (Integer) object;
                break;
            case LONG:
                result = (double) (Long) object;
                break;
            case FLOAT:
                result = ((Float) object).doubleValue();
                break;
            case DOUBLE:
                result = (Double) object;
                break;
            case STRING:
                result = Double.parseDouble((String) object);
                break;
            case BOOLEAN:
                result = ((Boolean) object) ? 1.0 : 0.0;
                break;
        }
        return result;
    }

    private static Boolean castToBoolean(Type type, Serializable object) {
        Boolean result = null;
        switch (type) {
            case INTEGER:
                result = (Integer) object != 0;
                break;
            case LONG:
                result = (Long) object != 0;
                break;
            case FLOAT:
                result = (Float) object != 0;
                break;
            case DOUBLE:
                result = (Double) object != 0;
                break;
            case STRING:
                result = Boolean.parseBoolean((String) object);
                break;
            case BOOLEAN:
                result = (Boolean) object;
                break;
        }
        return result;
    }

    private static HashMap<String, Serializable> castToMap(Type targetMapType, Type sourceType, Serializable object) {
        // sourceType is in MAPS, i.e. object is a map
        Map<?, ?> asMap = (Map<?, ?>) object;
        HashMap<String, Serializable> map = new HashMap<>();
        for (Object entryObject : asMap.entrySet()) {
            Map.Entry entry = (Map.Entry) entryObject;
            // We'll cast keys to strings just in case as well
            Object key = entry.getKey();
            String keyString = key == null ? null : key.toString();
            map.put(keyString, forceCast(targetMapType.subType, sourceType.subType, (Serializable) entry.getValue()));
        }
        return map;
    }

    private static ArrayList<Serializable> castToList(Type targetListType, Type sourceType, Serializable object) {
        // sourceType is in LISTS, i.e. object is a list
        List asList = (List) object;
        ArrayList<Serializable> list = new ArrayList<>();
        for (Object member : asList) {
            list.add(forceCast(targetListType.subType, sourceType.subType, (Serializable) member));
        }
        return list;
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

    private static Type findSubType(Map<?, ?> map) {
        if (map.isEmpty() || !hasStringKeys(map)) {
            return UNKNOWN;
        }
        return findNestedValueType(map.values());
    }

    private static Type findSubType(List<?> list) {
        if (list.isEmpty()) {
            return UNKNOWN;
        }
        return findNestedValueType(list);
    }

    private static Type findNestedValueType(Collection nestedValue) {
        Type nestedType = UNKNOWN;
        // Try till a nested type is gotten because we could have objects that are null, empty or have null mappings
        for (Iterator it = nestedValue.iterator(); it.hasNext() && nestedType == UNKNOWN;) {
            Object value = it.next();
            if (value == null) {
                continue;
            }
            if (value instanceof Map) {
                // Only have Map of Map of Primitives or List of Map of Primitives, so type has to be in Primitive Maps
                nestedType = findTypeWithSubType(PRIMITIVE_MAPS, findSubType((Map<?, ?>) value));
            } else {
                nestedType = getPrimitiveType(value);
            }
        }
        return nestedType;
    }

    private static Type findTypeWithSubType(Collection<Type> types, Type subType) {
        return types.stream().filter(t -> subType.equals(t.getSubType())).findFirst().orElse(UNKNOWN);
    }

    private static boolean hasStringKeys(Map<?, ?> map) {
        return map.keySet().stream().allMatch(o -> o instanceof String || o == null);
    }

    // *************************************** Type collection builders ***************************************

    private static Set<Type> set(Type... types) {
        return EnumSet.copyOf(Arrays.asList(types));
    }

    @SafeVarargs
    private static Set<Type> set(Set<Type>... types) {
        Set<Type> result = EnumSet.copyOf(types[0]);
        for (int i = 1; i < types.length; ++i) {
            result.addAll(types[i]);
        }
        return result;
    }
}
