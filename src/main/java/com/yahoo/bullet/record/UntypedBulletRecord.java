/*
 *  Copyright 2020, Yahoo Inc.
 *  Licensed under the terms of the Apache License, Version 2.0.
 *  See the LICENSE file associated with the project for terms.
 */
package com.yahoo.bullet.record;

public abstract class UntypedBulletRecord extends BulletRecord<Object> {
    private static final long serialVersionUID = -7477930424136052034L;

    @Override
    protected Object convert(Object object) {
        return object;
    }
}
