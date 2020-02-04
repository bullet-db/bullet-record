/*
 *  Copyright 2020, Yahoo Inc.
 *  Licensed under the terms of the Apache License, Version 2.0.
 *  See the LICENSE file associated with the project for terms.
 */
package com.yahoo.bullet.record;

import com.yahoo.bullet.typesystem.Type;
import com.yahoo.bullet.typesystem.TypedObject;

import java.util.Objects;

public abstract class TypedBulletRecord extends BulletRecord<TypedObject> {
    private static final long serialVersionUID = 5093501631768714558L;

    @Override
    protected TypedObject convert(Object object) {
        return new TypedObject(object);
    }

    /**
     * Set a {@link TypedObject} field directly into this record.
     *
     * @param field The name of the field.
     * @param object The non-null {@link TypedObject} with a valid type.
     * @return This record for chaining.
     * @throws NullPointerException if {@link TypedObject} is null. Just avoid setting it instead.
     * @throws UnsupportedOperationException if the {@link TypedObject} has {@link Type#NULL} or {@link Type#UNKNOWN}.
     */
    public BulletRecord<TypedObject> setTypedObject(String field, TypedObject object) {
        Objects.requireNonNull(object);
        Type type = object.getType();
        if (type == Type.NULL || type == Type.UNKNOWN) {
            throw new UnsupportedOperationException("You may not set a NULL or UNKNOWN typed object");
        }
        return rawSet(field, object);
    }

    /**
     * Not supported. Will throw {@link UnsupportedOperationException}. You must use the appropriate set methods.
     *
     * @param field The name in this record to insert the object as.
     * @param object The object to be set.
     * @return Not relevant as this method always throws.
     * @throws UnsupportedOperationException all the time.
     */
    @Override
    public BulletRecord<TypedObject> forceSet(String field, Object object) {
        throw new UnsupportedOperationException("The Typed Bullet Record does not support force setting!");
    }
}
