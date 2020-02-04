/*
 *  Copyright 2020, Yahoo Inc.
 *  Licensed under the terms of the Apache License, Version 2.0.
 *  See the LICENSE file associated with the project for terms.
 */
package com.yahoo.bullet.record;

import com.yahoo.bullet.typesystem.TypedObject;

public abstract class UntypedBulletRecord extends BulletRecord<Object> {
    private static final long serialVersionUID = -7477930424136052034L;

    @Override
    protected Object convert(Object object) {
        return object;
    }

    /**
     * By default will extract a field and compute its {@link com.yahoo.bullet.typesystem.Type}.
     * {@inheritDoc}.
     *
     * @param field The non-null name of the field.
     * @return The {@link TypedObject} or null if the field does not exist.
     */
    @Override
    public TypedObject typedGet(String field) {
        return hasField(field) ? new TypedObject(get(field)) : null;
    }
}
