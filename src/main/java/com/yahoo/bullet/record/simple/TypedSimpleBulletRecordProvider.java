/*
 *  Copyright 2020, Yahoo Inc.
 *  Licensed under the terms of the Apache License, Version 2.0.
 *  See the LICENSE file associated with the project for terms.
 */
package com.yahoo.bullet.record.simple;

import com.yahoo.bullet.record.BulletRecord;
import com.yahoo.bullet.record.BulletRecordProvider;

public class TypedSimpleBulletRecordProvider implements BulletRecordProvider {
    private static final long serialVersionUID = 8099118781438239229L;

    @Override
    public BulletRecord getInstance() {
        return new TypedSimpleBulletRecord();
    }
}
