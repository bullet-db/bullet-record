/*
 *  Copyright 2018, Yahoo Inc.
 *  Licensed under the terms of the Apache License, Version 2.0.
 *  See the LICENSE file associated with the project for terms.
 */
package com.yahoo.bullet.record.avro;

import com.yahoo.bullet.record.BulletRecord;
import com.yahoo.bullet.record.BulletRecordProvider;

/**
 * Factory for Avro based UntypedBulletRecords.
 */
public class UntypedAvroBulletRecordProvider implements BulletRecordProvider {
    private static final long serialVersionUID = 8064632505561405799L;

    @Override
    public BulletRecord getInstance() {
        return new UntypedAvroBulletRecord();
    }
}
