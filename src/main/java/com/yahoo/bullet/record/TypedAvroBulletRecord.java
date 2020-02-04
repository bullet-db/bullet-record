/*
 *  Copyright 2020, Yahoo Inc.
 *  Licensed under the terms of the Apache License, Version 2.0.
 *  See the LICENSE file associated with the project for terms.
 */
package com.yahoo.bullet.record;

import com.yahoo.bullet.typesystem.TypedObject;

import java.util.Iterator;
import java.util.Map;

public class TypedAvroBulletRecord extends TypedBulletRecord {
    @Override
    protected BulletRecord<TypedObject> rawSet(String field, TypedObject object) {
        return null;
    }

    @Override
    public TypedObject get(String field) {
        return null;
    }

    @Override
    public boolean hasField(String field) {
        return false;
    }

    @Override
    public int fieldCount() {
        return 0;
    }

    @Override
    public TypedObject getAndRemove(String field) {
        return null;
    }

    @Override
    public BulletRecord<TypedObject> remove(String field) {
        return null;
    }

    @Override
    public Iterator<Map.Entry<String, TypedObject>> iterator() {
        return null;
    }
}
