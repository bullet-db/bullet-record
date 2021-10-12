/*
 *  Copyright 2018, Yahoo Inc.
 *  Licensed under the terms of the Apache License, Version 2.0.
 *  See the LICENSE file associated with the project for terms.
 */
package com.yahoo.bullet.record.simple;

import com.yahoo.bullet.record.BulletRecord;
import com.yahoo.bullet.record.BulletRecordProvider;

/**
 * Factory for simple BulletRecords.
 */
public class UntypedSimpleBulletRecordProvider implements BulletRecordProvider {
    private static final long serialVersionUID = 6155338160701668740L;

    @Override
    public BulletRecord getInstance() {
        return new UntypedSimpleBulletRecord();
    }
}
