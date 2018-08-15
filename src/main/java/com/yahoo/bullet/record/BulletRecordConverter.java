package com.yahoo.bullet.record;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import javafx.util.Pair;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Creates a converter for a POJO. Field::get() is 60x slower than accessing fields directly. Method::invoke() a getter
 * is 30x slower than accessing fields directly. Probably shouldn't even support this... Code generation is probably
 * better.
 *
 * Assumes maps and lists are declared as their base classes.
 *
 * Try to find getters automatically?
 *
 * @param <T>
 */
public class BulletRecordConverter<T> {
    private static final List<Class> PRIMITIVES = Arrays.asList(Boolean.class, Integer.class, Long.class, Float.class,
                                                                Double.class, String.class);
    private static final Predicate<Field> IS_VALID_TYPE = field -> {
        Type type = field.getType();
        if (PRIMITIVES.contains(type)) {
            return true;
        } else if (type == Map.class) {
            ParameterizedType pt = (ParameterizedType) field.getGenericType();
            Class keyType = (Class)pt.getActualTypeArguments()[0];
            Class valueType = (Class)pt.getActualTypeArguments()[1];
            if (keyType == String.class) {
                if (PRIMITIVES.contains(valueType)) {
                    return true;
                } else if (valueType == Map.class) {
                    pt = (ParameterizedType) field.getGenericType();
                    keyType = (Class) pt.getActualTypeArguments()[0];
                    valueType = (Class) pt.getActualTypeArguments()[1];
                    return keyType == String.class && PRIMITIVES.contains(valueType);
                }
            }
        } else if (type == List.class) {
            ParameterizedType pt = (ParameterizedType) field.getGenericType();
            Class listType = (Class)pt.getActualTypeArguments()[0];
            if (PRIMITIVES.contains(listType)) {
                return true;
            } else if (listType == Map.class) {
                pt = (ParameterizedType) field.getGenericType();
                Class keyType = (Class)pt.getActualTypeArguments()[0];
                Class valueType = (Class)pt.getActualTypeArguments()[1];
                return keyType == String.class && PRIMITIVES.contains(valueType);
            }
        }
        return false;
    };

    private class BulletRecordField {
        String name;
        String getter;
    }
    private List<Field> fields;
    private List<Pair<String, Method>> getters;

    /**
     * Takes all fields regardless of access. does not include inherited fields
     */
    private BulletRecordConverter(Class<T> type) {
        fields = Arrays.asList(type.getDeclaredFields());
        if (!fields.stream().allMatch(IS_VALID_TYPE)) {
            throw new RuntimeException("Object contains a field with an unsupported type.");
        }
        for (Field field : fields) {
            field.setAccessible(true);
        }
    }

    /**
     * Fields defined by config
     */
    private BulletRecordConverter(Class<T> type, String schema) {
        JsonReader reader;
        try {
            reader = new JsonReader(new FileReader(schema));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(schema + " file not found", e);
        }

        List<BulletRecordField> fieldList =
                new Gson().fromJson(reader, new TypeToken<List<BulletRecordField>>(){}.getType());

        for (BulletRecordField field : fieldList) {
            Field f;
            try {
                f = type.getDeclaredField(field.name);
                if (!IS_VALID_TYPE.test(f)) {
                    throw new RuntimeException("Object contains a listed field with an unsupported type.");
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
    public static <T> BulletRecordConverter<T> from(Class<T> type) {
        return new BulletRecordConverter<T>(type);
    }

    /**
     * Takes fields listed in json files. Looks for getters.
     *
     * @param type
     * @param schema
     * @param <T>
     * @return
     */
    public static <T> BulletRecordConverter<T> from(Class<T> type, String schema) {
        return new BulletRecordConverter<T>(type, schema);
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
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Object contains inaccessible field.", e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException("Exception thrown by getter.", e);
        }
        return record;
    }
}
