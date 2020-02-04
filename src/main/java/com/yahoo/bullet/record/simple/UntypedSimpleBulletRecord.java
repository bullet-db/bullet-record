/*
 *  Copyright 2018, Oath Inc.
 *  Licensed under the terms of the Apache License, Version 2.0.
 *  See the LICENSE file associated with the project for terms.
 */
package com.yahoo.bullet.record.simple;

import com.yahoo.bullet.record.UntypedBulletRecord;
import lombok.AccessLevel;
import lombok.Setter;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

/**
 * A simple implementation of {@link UntypedBulletRecord}.
 */
public class UntypedSimpleBulletRecord extends UntypedBulletRecord {
    private static final long serialVersionUID = -4045166645513428587L;

    // Exposed for testing.
    @Setter(AccessLevel.PACKAGE)
    private Map<String, Object> data = new HashMap<>();

    @Override
    protected UntypedSimpleBulletRecord rawSet(String field, Object object) {
        Objects.requireNonNull(field);
        data.put(field, object);
        return this;
    }


    @Override
    protected Object convert(Object object) {
        return object;
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
        return data.remove(field);
    }

    @Override
    public UntypedSimpleBulletRecord remove(String field) {
        data.remove(field);
        return this;
    }

    @Override
    public Iterator<Map.Entry<String, Object>> iterator() {
        return data.entrySet().iterator();
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof UntypedSimpleBulletRecord)) {
            return false;
        }
        UntypedSimpleBulletRecord that = (UntypedSimpleBulletRecord) object;
        return data == that.data || (data != null && data.equals(that.data));
    }

    @Override
    public int hashCode() {
        // Value doesn't matter when data is null
        return data == null ? 42 : data.hashCode();
    }
}
