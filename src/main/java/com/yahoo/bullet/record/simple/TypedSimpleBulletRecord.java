/*
 *  Copyright 2020, Yahoo Inc.
 *  Licensed under the terms of the Apache License, Version 2.0.
 *  See the LICENSE file associated with the project for terms.
 */
package com.yahoo.bullet.record.simple;

import com.yahoo.bullet.record.TypedBulletRecord;
import com.yahoo.bullet.typesystem.TypedObject;
import lombok.AccessLevel;
import lombok.Setter;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

public class TypedSimpleBulletRecord extends TypedBulletRecord {
    private static final long serialVersionUID = -4428637875399391902L;

    // Exposed for testing.
    @Setter(AccessLevel.PACKAGE)
    private Map<String, TypedObject> data = new HashMap<>();

    @Override
    protected TypedSimpleBulletRecord rawSet(String field, TypedObject object) {
        Objects.requireNonNull(field);
        data.put(field, object);
        return this;
    }

    @Override
    public TypedObject get(String field) {
        TypedObject object = data.get(field);
        return object == null ? TypedObject.NULL : object;
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
    public TypedObject getAndRemove(String field) {
        return data.remove(field);
    }

    @Override
    public TypedSimpleBulletRecord remove(String field) {
        data.remove(field);
        return this;
    }

    @Override
    public TypedSimpleBulletRecord copy() {
        TypedSimpleBulletRecord copy = new TypedSimpleBulletRecord();
        copy.data.putAll(this.data);
        return copy;
    }

    @Override
    public Iterator<Map.Entry<String, TypedObject>> iterator() {
        return data.entrySet().iterator();
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof TypedSimpleBulletRecord)) {
            return false;
        }
        TypedSimpleBulletRecord that = (TypedSimpleBulletRecord) object;
        return Objects.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        // Value doesn't matter when data is null
        return data == null ? 42 : data.hashCode();
    }
}
