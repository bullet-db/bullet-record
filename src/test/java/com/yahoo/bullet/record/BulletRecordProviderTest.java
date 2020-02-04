/*
 *  Copyright 2018, Oath Inc.
 *  Licensed under the terms of the Apache License, Version 2.0.
 *  See the LICENSE file associated with the project for terms.
 */
package com.yahoo.bullet.record;

import org.testng.annotations.Test;

public class BulletRecordProviderTest {
    @Test(expectedExceptions = RuntimeException.class)
    public void testGetBulletRecordCatchesAndThrows() {
        BulletRecordProvider.from("this.class.does.not.exist");
    }
}
