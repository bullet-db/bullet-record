/*
 *  Copyright 2018, Oath Inc.
 *  Licensed under the terms of the Apache License, Version 2.0.
 *  See the LICENSE file associated with the project for terms.
 */
package com.yahoo.bullet.record;

import lombok.AccessLevel;
import lombok.Setter;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A simple implementation of {@link BulletRecord}.
 */
public class SimpleBulletRecord extends BulletRecord {
    private static final long serialVersionUID = -4045166645513428587L;

    // Exposed for testing.
    @Setter(AccessLevel.PACKAGE)
    private Map<String, Object> data = new HashMap<>();

    @Override
    protected BulletRecord set(String field, Object object) {
        Objects.requireNonNull(field);
        data.put(field, object);
        return this;
    }

    @Override
    public Object get(String field) {
        return data.get(field);
    }

    @Override
    public boolean hasField(String field) {
        return data.containsKey(field);
    }

    @Override
    public int fieldCount() {
        return data.size();
    }

    @Override
    public Object getAndRemove(String field) {
        return hasField(field) ? data.remove(field) : null;
    }

    @Override
    public BulletRecord remove(String field) {
        if (hasField(field)) {
            data.remove(field);
        }
        return this;
    }

    @Override
    public Iterator<Pair<String, Object>> iterator() {
        return new Iterator<Pair<String, Object>>() {
            Iterator<Map.Entry<String, Object>> entries = data.entrySet().iterator();

            @Override
            public boolean hasNext() {
                return entries.hasNext();
            }

            @Override
            public Pair<String, Object> next() {
                Map.Entry<String, Object> entry = entries.next();
                return new ImmutablePair<>(entry.getKey(), entry.getValue());
            }
        };
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
        if (!(object instanceof SimpleBulletRecord)) {
            return false;
        }
        SimpleBulletRecord that = (SimpleBulletRecord) object;
        return data == that.data || (data != null && data.equals(that.data));
    }

    @Override
    public int hashCode() {
        // Value doesn't matter when data is null
        return data == null ? 42 : data.hashCode();
    }

    /**
     * For Testing.
     * <p>
     * Insert a map field with values as Pairs or Map.Entry. The value of
     * the entries must be in "Primitives".
     *
     * @param field The non-null name of the field.
     * @param entries The non-null entries to insert.
     * @return this object for chaining.
     */
    BulletRecord setMap(String field, Map.Entry<String, Object>... entries) {
        Objects.requireNonNull(entries);
        Map<String, Object> newMap = new HashMap<>(entries.length);
        for (Map.Entry<String, Object> entry : entries) {
            newMap.put(entry.getKey(), entry.getValue());
        }
        return set(field, newMap);
    }

    /**
     * For Testing.
     * <p>
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
