package com.yahoo.bullet.record;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import javafx.util.Pair;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Creates a converter for a POJO. Field::get() is 60x slower than accessing fields directly. Method::invoke() a getter
 * is 30x slower than accessing fields directly. Probably shouldn't even support this... Code generation is probably
 * better.
 *
 * Lists/maps are not copies.
 * Assumes maps and lists are declared as their base classes.
 *
 * Try to find getters automatically?
 *
 * @param <T>
 */
public class POJOBulletRecordConverter<T> {
    private static final List<Class> PRIMITIVES = Arrays.asList(Boolean.class, Integer.class, Long.class, Float.class,
                                                                Double.class, String.class);

    private static boolean isThisInterface(Class type, Class clazz) {
        return type == clazz || Arrays.asList(type.getInterfaces()).contains(clazz);
    }

    // Exposed for coverage
    static class BulletRecordField {
        String name;
        String getter;
    }
    private List<Field> fields;
    private List<Pair<String, Method>> getters;

    /**
     * Takes all fields regardless of access. does not include inherited fields
     */
    private POJOBulletRecordConverter(Class<T> type) {
        fields = Stream.of(type.getDeclaredFields()).filter(field -> !field.isSynthetic()).collect(Collectors.toList());
        for (Field field : fields) {
            if (!isValidType(field)) {
                throw new RuntimeException("Object contains a field with an unsupported type: " + field.getName());
            }
            field.setAccessible(true);
        }
        getters = Collections.emptyList();
    }

    /**
     * Fields defined by config.
     */
    private POJOBulletRecordConverter(Class<T> type, String schema) {
        JsonReader reader;
        try {
            reader = new JsonReader(new FileReader(schema));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(schema + " file not found.", e);
        }

        List<BulletRecordField> fieldList =
                new Gson().fromJson(reader, new TypeToken<List<BulletRecordField>>() { }.getType());

        fields = new ArrayList<>();
        getters = new ArrayList<>();

        for (BulletRecordField field : fieldList) {
            Field f;
            try {
                f = type.getDeclaredField(field.name);
                if (!isValidType(f)) {
                    throw new RuntimeException("Object contains a listed field with an unsupported type: " + field.name);
                }
                f.setAccessible(true);
            } catch (NoSuchFieldException e) {
                throw new RuntimeException("Object is missing listed field: " + field.name);
            }
            if (field.getter == null) {
                fields.add(f);
            } else {
                try {
                    Method m = type.getDeclaredMethod(field.getter);
                    m.setAccessible(true);
                    if (m.getReturnType() != f.getType()) {
                        throw new RuntimeException("Listed getter return type does not match field: " + field.getter);
                    }
                    getters.add(new Pair<>(field.name, m));
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException("Object is missing listed getter: " + field.getter);
                }
            }
        }
    }

    /**
     * Takes all fields. Does not look for getters.
     *
     * @param type
     * @param <T>
     * @return
     */
    public static <T> POJOBulletRecordConverter<T> from(Class<T> type) {
        return new POJOBulletRecordConverter<T>(type);
    }

    /**
     * Takes fields listed in json files. Looks for getters.
     *
     * @param type
     * @param schema
     * @param <T>
     * @return
     */
    public static <T> POJOBulletRecordConverter<T> from(Class<T> type, String schema) {
        return new POJOBulletRecordConverter<T>(type, schema);
    }

    /**
     * converts object of type T to a BulletRecord.
     *
     * @param object
     * @return
     */
    public BulletRecord convert(T object) {
        BulletRecord record = new SimpleBulletRecord();
        try {
            for (Field field : fields) {
                record.set(field.getName(), field.get(object));
            }
            for (Pair<String, Method> getter : getters) {
                record.set(getter.getKey(), getter.getValue().invoke(object));
            }
        } catch (IllegalAccessException ignore) {
            throw new RuntimeException("Object contains inaccessible field.", ignore);
        } catch (InvocationTargetException e) {
            throw new RuntimeException("Exception thrown by getter.", e);
        }
        return record;
    }

    private static boolean isValidType(Field field) {
        Class type = field.getType();
        if (PRIMITIVES.contains(type)) {
            return true;
        } else if (isThisInterface(type, Map.class)) {
            ParameterizedType pt = (ParameterizedType) field.getGenericType();
            Class keyType = (Class) pt.getActualTypeArguments()[0];
            Type valueType = pt.getActualTypeArguments()[1];
            if (keyType == String.class) {
                if (valueType instanceof Class) {
                    return PRIMITIVES.contains(valueType);
                } else {
                    pt = (ParameterizedType) valueType;
                    if (isThisInterface((Class) pt.getRawType(), Map.class)) {
                        keyType = (Class) pt.getActualTypeArguments()[0];
                        try {
                            valueType = (Class) pt.getActualTypeArguments()[1];
                            return keyType == String.class && PRIMITIVES.contains(valueType);
                        } catch (ClassCastException e) {
                            return false;
                        }
                    }
                }
            }
        } else if (isThisInterface(type, List.class)) {
            ParameterizedType pt = (ParameterizedType) field.getGenericType();
            Type listType = pt.getActualTypeArguments()[0];
            if (listType instanceof Class) {
                return PRIMITIVES.contains(listType);
            } else {
                pt = (ParameterizedType) listType;
                if (isThisInterface((Class) pt.getRawType(), Map.class)) {
                    Class keyType = (Class) pt.getActualTypeArguments()[0];
                    try {
                        Class valueType = (Class) pt.getActualTypeArguments()[1];
                        return keyType == String.class && PRIMITIVES.contains(valueType);
                    } catch (ClassCastException e) {
                        return false;
                    }
                }
            }
        }
        return false;
    }
}
