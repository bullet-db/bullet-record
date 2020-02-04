/*
 *  Copyright 2018, Oath Inc.
 *  Licensed under the terms of the Apache License, Version 2.0.
 *  See the LICENSE file associated with the project for terms.
 */
package com.yahoo.bullet.record;

import org.testng.Assert;
import org.testng.annotations.Test;

public class UntypedSimpleBulletRecordProviderTest {
    @Test
    public void testInstanceNotPassedByReference() {
        BulletRecord recordA = new UntypedSimpleBulletRecordProvider().getInstance();
        BulletRecord recordB = new UntypedSimpleBulletRecordProvider().getInstance();
        Assert.assertTrue(recordA instanceof UntypedSimpleBulletRecord);
        Assert.assertTrue(recordB instanceof UntypedSimpleBulletRecord);

        recordA.setString("someField", "someValue");
        Assert.assertNull(recordB.get("someField"));

        BulletRecord recordC = new UntypedSimpleBulletRecordProvider().getInstance();
        Assert.assertNull(recordC.get("someField"));
    }
}
