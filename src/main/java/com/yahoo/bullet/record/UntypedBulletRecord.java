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

public abstract class UntypedBulletRecord extends BulletRecord<Serializable> {
    private static final long serialVersionUID = -7477930424136052034L;

    @Override
    protected Serializable convert(Object object) {
        return (Serializable) object;
    }

    /**
     * By default will extract a field and compute its {@link com.yahoo.bullet.typesystem.Type}.
     * {@inheritDoc}.
     *
     * @param field The non-null name of the field.
     * @return The {@link TypedObject} or null if the field does not exist.
     */
    @Override
    public TypedObject typedGet(String field, Type hint) {
        return hasField(field) ? new TypedObject(hint, get(field)) : TypedObject.NULL;
    }

    @Override
    protected Map<String, Serializable> getRawDataMap() {
        Map<String, Serializable> data = new HashMap<>();
        this.forEach(e -> data.put(e.getKey(), e.getValue()));
        return data;
    }
}
