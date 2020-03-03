/*
 *  Copyright 2020, Yahoo Inc.
 *  Licensed under the terms of the Apache License, Version 2.0.
 *  See the LICENSE file associated with the project for terms.
 */
package com.yahoo.bullet.record.avro;

import com.yahoo.bullet.record.BulletRecord;
import com.yahoo.bullet.typesystem.TypedObject;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TypedAvroBulletRecordProviderTest {
    @Test
    public void testInstance() {
        BulletRecord recordA = new TypedAvroBulletRecordProvider().getInstance();
        BulletRecord recordB = new TypedAvroBulletRecordProvider().getInstance();
        Assert.assertTrue(recordA instanceof TypedAvroBulletRecord);
        Assert.assertTrue(recordB instanceof TypedAvroBulletRecord);

        recordA.setString("someField", "someValue");
        Assert.assertEquals(recordB.get("someField"), TypedObject.NULL);

        BulletRecord recordC = new TypedAvroBulletRecordProvider().getInstance();
        Assert.assertEquals(recordC.get("someField"), TypedObject.NULL);
    }
}
