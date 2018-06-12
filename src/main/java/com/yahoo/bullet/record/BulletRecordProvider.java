/*
 *  Copyright 2018, Oath Inc.
 *  Licensed under the terms of the Apache License, Version 2.0.
 *  See the LICENSE file associated with the project for terms.
 */
package com.yahoo.bullet.record;

/**
 * Factories used to produce BulletRecords should implement this interface.
 */
public interface BulletRecordProvider {
    /**
     * Get an instance of a BulletRecord.
     *
     * @return A BulletRecord.
     */
    public BulletRecord getInstance();
}
