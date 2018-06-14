/*
 *  Copyright 2018, Oath Inc.
 *  Licensed under the terms of the Apache License, Version 2.0.
 *  See the LICENSE file associated with the project for terms.
 */
package com.yahoo.bullet.record;

import org.testng.Assert;
import org.testng.annotations.Test;

public class SimpleBulletRecordProviderTest {
    @Test
    public void testInstanceNotPassedByReference() {
        BulletRecord recordA = new SimpleBulletRecordProvider().getInstance();
        BulletRecord recordB = new SimpleBulletRecordProvider().getInstance();
        Assert.assertTrue(recordA instanceof SimpleBulletRecord);
        Assert.assertTrue(recordB instanceof SimpleBulletRecord);

        recordA.setString("someField", "someValue");
        Assert.assertNull(recordB.get("someField"));

        BulletRecord recordC = new SimpleBulletRecordProvider().getInstance();
        Assert.assertNull(recordC.get("someField"));
    }
}
