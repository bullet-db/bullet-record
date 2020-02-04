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
        UntypedBulletRecord recordA = BulletRecordProvider.from("com.yahoo.bullet.record.AvroBulletRecordProvider").getUntypedInstance();
        UntypedBulletRecord recordB = BulletRecordProvider.from("com.yahoo.bullet.record.AvroBulletRecordProvider").getUntypedInstance();
        Assert.assertTrue(recordA instanceof UntypedAvroBulletRecord);
        Assert.assertTrue(recordB instanceof UntypedAvroBulletRecord);

        recordB.setString("someField", "someValue");
        Assert.assertEquals(recordB.get("someField"), "someValue");
        Assert.assertNull(recordA.get("someField"));

        UntypedBulletRecord recordC = BulletRecordProvider.from("com.yahoo.bullet.record.AvroBulletRecordProvider").getUntypedInstance();
        Assert.assertNull(recordC.get("someField"));
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testGetBulletRecordCatchesAndThrows() {
        BulletRecordProvider bulletRecordProvider = BulletRecordProvider.from("this.class.does.not.exist");
    }
}
