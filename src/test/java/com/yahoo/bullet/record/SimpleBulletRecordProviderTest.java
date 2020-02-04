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
        UntypedBulletRecord recordA = new SimpleBulletRecordProvider().getUntypedInstance();
        UntypedBulletRecord recordB = new SimpleBulletRecordProvider().getUntypedInstance();
        Assert.assertTrue(recordA instanceof UntypedSimpleBulletRecord);
        Assert.assertTrue(recordB instanceof UntypedSimpleBulletRecord);

        recordA.setString("someField", "someValue");
        Assert.assertNull(recordB.get("someField"));

        UntypedBulletRecord recordC = new SimpleBulletRecordProvider().getUntypedInstance();
        Assert.assertNull(recordC.get("someField"));
    }
}
