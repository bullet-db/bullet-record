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
        UntypedAvroBulletRecord recordA = new AvroBulletRecordProvider().getUntypedInstance();
        UntypedAvroBulletRecord recordB = new AvroBulletRecordProvider().getUntypedInstance();
        Assert.assertTrue(recordA instanceof UntypedAvroBulletRecord);
        Assert.assertTrue(recordB instanceof UntypedAvroBulletRecord);

        recordA.setString("someField", "someValue");
        Assert.assertNull(recordB.get("someField"));

        BulletRecord recordC = new AvroBulletRecordProvider().getUntypedInstance();
        Assert.assertNull(recordC.get("someField"));
    }
}
