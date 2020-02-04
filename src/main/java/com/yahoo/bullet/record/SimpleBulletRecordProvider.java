/*
 *  Copyright 2018, Oath Inc.
 *  Licensed under the terms of the Apache License, Version 2.0.
 *  See the LICENSE file associated with the project for terms.
 */

package com.yahoo.bullet.record;

/**
 * Factory for simple BulletRecords.
 */
public class SimpleBulletRecordProvider implements BulletRecordProvider {
    private static final long serialVersionUID = 6155338160701668740L;

    @Override
    public UntypedSimpleBulletRecord getUntypedInstance() {
        return new UntypedSimpleBulletRecord();
    }

    @Override
    public TypedSimpleBulletRecord getTypedInstance() {
        throw new UnsupportedOperationException("Not supported yet!");
    }
}
