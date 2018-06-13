/*
 *  Copyright 2018, Oath Inc.
 *  Licensed under the terms of the Apache License, Version 2.0.
 *  See the LICENSE file associated with the project for terms.
 */
package com.yahoo.bullet.record;

import org.testng.Assert;
import org.testng.annotations.Test;

public class BulletRecordProviderTest {
    @Test
    public void testFromMakesNewInstance() {
        BulletRecord recordA = BulletRecordProvider.from("com.yahoo.bullet.record.AvroBulletRecordProvider").getInstance();
        BulletRecord recordB = BulletRecordProvider.from("com.yahoo.bullet.record.AvroBulletRecordProvider").getInstance();
        Assert.assertTrue(recordA instanceof AvroBulletRecord);
        Assert.assertTrue(recordB instanceof AvroBulletRecord);

        recordB.setString("someField", "someValue");
        Assert.assertEquals(recordB.get("someField"), "someValue");
        Assert.assertNull(recordA.get("someField"));

        BulletRecord recordC = BulletRecordProvider.from("com.yahoo.bullet.record.AvroBulletRecordProvider").getInstance();
        Assert.assertNull(recordC.get("someField"));
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testGetBulletRecordCatchesAndThrows() {
        BulletRecordProvider bulletRecordProvider = BulletRecordProvider.from("this.class.does.not.exist");
    }
}
