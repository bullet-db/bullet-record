/*
 *  Copyright 2020, Yahoo Inc.
 *  Licensed under the terms of the Apache License, Version 2.0.
 *  See the LICENSE file associated with the project for terms.
 */
package com.yahoo.bullet.record;

import com.yahoo.bullet.typesystem.Type;
import com.yahoo.bullet.typesystem.TypedObject;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public abstract class TypedBulletRecord extends BulletRecord<TypedObject> {
    private static final long serialVersionUID = 5093501631768714558L;

    @Override
    protected TypedObject convert(Object object) {
        return new TypedObject((Serializable) object);
    }

    /**
     * Set a {@link TypedObject} field directly into this record.
     * {@inheritDoc}
     *
     * @param field The name of the field.
     * @param object The non-null {@link TypedObject} with a valid type.
     * @return This record for chaining.
     * @throws NullPointerException if {@link TypedObject} is null. Just avoid setting it instead.
     * @throws UnsupportedOperationException if the {@link TypedObject} has {@link Type#NULL} or {@link Type#UNKNOWN}.
     */
    @Override
    public BulletRecord<TypedObject> typedSet(String field, TypedObject object) {
        validateObject(object);
        return rawSet(field, object);
    }

    @Override
    public TypedObject typedGet(String field) {
        return get(field);
    }

    @Override
    protected Map<String, Serializable> getRawDataMap() {
        Map<String, Serializable> data = new HashMap<>();
        this.forEach(e -> data.put(e.getKey(), e.getValue().getValue()));
        return data;
    }
}
