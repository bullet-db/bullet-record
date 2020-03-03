/*
 *  Copyright 2018, Oath Inc.
 *  Licensed under the terms of the Apache License, Version 2.0.
 *  See the LICENSE file associated with the project for terms.
 */
package com.yahoo.bullet.record.avro;

import com.yahoo.bullet.record.BulletRecord;
import com.yahoo.bullet.record.BulletRecordProvider;
import org.testng.Assert;
import org.testng.annotations.Test;

public class UntypedAvroBulletRecordProviderTest {
    @Test
    public void testFromMakesNewInstance() {
        BulletRecord recordA = BulletRecordProvider.from("com.yahoo.bullet.record.avro.UntypedAvroBulletRecordProvider").getInstance();
        BulletRecord recordB = BulletRecordProvider.from("com.yahoo.bullet.record.avro.UntypedAvroBulletRecordProvider").getInstance();
        Assert.assertTrue(recordA instanceof UntypedAvroBulletRecord);
        Assert.assertTrue(recordB instanceof UntypedAvroBulletRecord);

        recordB.setString("someField", "someValue");
        Assert.assertEquals(recordB.get("someField"), "someValue");
        Assert.assertNull(recordA.get("someField"));

        BulletRecord recordC = BulletRecordProvider.from("com.yahoo.bullet.record.avro.UntypedAvroBulletRecordProvider").getInstance();
        Assert.assertNull(recordC.get("someField"));
    }

    @Test
    public void testInstanceNotPassedByReference() {
        BulletRecord recordA = new UntypedAvroBulletRecordProvider().getInstance();
        BulletRecord recordB = new UntypedAvroBulletRecordProvider().getInstance();
        Assert.assertTrue(recordA instanceof UntypedAvroBulletRecord);
        Assert.assertTrue(recordB instanceof UntypedAvroBulletRecord);

        recordA.setString("someField", "someValue");
        Assert.assertNull(recordB.get("someField"));

        BulletRecord recordC = new UntypedAvroBulletRecordProvider().getInstance();
        Assert.assertNull(recordC.get("someField"));
    }
}
