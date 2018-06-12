/*
 *  Copyright 2018, Oath Inc.
 *  Licensed under the terms of the Apache License, Version 2.0.
 *  See the LICENSE file associated with the project for terms.
 */
package com.yahoo.bullet.record;

import org.testng.Assert;
import org.testng.annotations.Test;

public class AvroBulletRecordProviderTest {
    @Test
    public void testInstanceNotPassedByReference() {
        BulletRecord recordA = new AvroBulletRecordProvider().getInstance();
        BulletRecord recordB = new AvroBulletRecordProvider().getInstance();
        Assert.assertTrue(recordA instanceof AvroBulletRecord);
        Assert.assertTrue(recordB instanceof AvroBulletRecord);

        recordA.setString("someField", "someValue");
        Assert.assertNull(recordB.get("someField"));

        BulletRecord recordC = new AvroBulletRecordProvider().getInstance();
        Assert.assertNull(recordC.get("someField"));
    }
}
