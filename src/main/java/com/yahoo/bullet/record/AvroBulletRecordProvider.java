/*
 *  Copyright 2018, Oath Inc.
 *  Licensed under the terms of the Apache License, Version 2.0.
 *  See the LICENSE file associated with the project for terms.
 */

package com.yahoo.bullet.record;

/**
 * Factory for AvroBulletRecord.
 */
public class AvroBulletRecordProvider implements BulletRecordProvider {
    private static final long serialVersionUID = 8064632505561405799L;

    @Override
    public BulletRecord getInstance() {
        return new AvroBulletRecord();
    }
}
