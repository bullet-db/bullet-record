/*
 *  Copyright 2018, Oath Inc.
 *  Licensed under the terms of the Apache License, Version 2.0.
 *  See the LICENSE file associated with the project for terms.
 */
package com.yahoo.bullet.record;

import java.io.Serializable;
import java.lang.reflect.Constructor;

/**
 * Factories used to produce BulletRecords should implement this interface.
 */
public interface BulletRecordProvider extends Serializable {
    /**
     * Get an instance of an {@link UntypedBulletRecord}.
     *
     * @return A {@link UntypedBulletRecord} instance.
     */
    UntypedBulletRecord getUntypedInstance();

    /**
     * Get an instance of a {@link TypedBulletRecord}. Using this kind of {@link BulletRecord} can provide a higher
     * degree of type safety and may improve runtime performance of Bullet since all the fields retrieved from this
     * record would already be a {@link com.yahoo.bullet.typesystem.TypedObject}. This would come at the upfront cost of
     * creating a {@link com.yahoo.bullet.typesystem.TypedObject} when inserting into the record.
     *
     * @return A {@link TypedBulletRecord} instance.
     */
    TypedBulletRecord getTypedInstance();

    /**
     * Create a BulletRecordProvider instance using the specified class.
     *
     * @param bulletRecordProviderClassName The name of the BulletRecordProvider class.
     * @return a new instance of specified BulletRecordProvider class.
     * @throws RuntimeException if BulletRecordProvider creation fails.
     */
    static BulletRecordProvider from(String bulletRecordProviderClassName) {
        try {
            Class<BulletRecordProvider> recordProviderClass = (Class<BulletRecordProvider>) Class.forName(bulletRecordProviderClassName);
            Constructor<BulletRecordProvider> constructor = recordProviderClass.getConstructor();
            return constructor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Cannot create BulletRecordProvider.", e);
        }
    }
}
