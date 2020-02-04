/*
 *  Copyright 2018, Oath Inc.
 *  Licensed under the terms of the Apache License, Version 2.0.
 *  See the LICENSE file associated with the project for terms.
 */
package com.yahoo.bullet.record;

/**
 * Factory for Avro based BulletRecords.
 */
public class AvroBulletRecordProvider implements BulletRecordProvider {
    private static final long serialVersionUID = 8064632505561405799L;

    @Override
    public UntypedAvroBulletRecord getUntypedInstance() {
        return new UntypedAvroBulletRecord();
    }

    @Override
    public TypedAvroBulletRecord getTypedInstance() {
        throw new UnsupportedOperationException("Not supported yet!");
    }
}
