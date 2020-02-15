/*
 *  Copyright 2018, Oath Inc.
 *  Licensed under the terms of the Apache License, Version 2.0.
 *  See the LICENSE file associated with the project for terms.
 */
package com.yahoo.bullet.record.simple;

import com.yahoo.bullet.record.BulletRecord;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TypedSimpleBulletRecordProviderTest {
    @Test
    public void testInstanceNotPassedByReference() {
        BulletRecord recordA = new TypedSimpleBulletRecordProvider().getInstance();
        BulletRecord recordB = new TypedSimpleBulletRecordProvider().getInstance();
        Assert.assertTrue(recordA instanceof TypedSimpleBulletRecord);
        Assert.assertTrue(recordB instanceof TypedSimpleBulletRecord);

        recordA.setString("someField", "someValue");
        Assert.assertNull(recordB.get("someField"));

        BulletRecord recordC = new TypedSimpleBulletRecordProvider().getInstance();
        Assert.assertNull(recordC.get("someField"));
    }
}
