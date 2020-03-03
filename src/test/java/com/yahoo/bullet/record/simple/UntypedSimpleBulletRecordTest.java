/*
 *  Copyright 2016, Yahoo Inc.
 *  Licensed under the terms of the Apache License, Version 2.0.
 *  See the LICENSE file associated with the project for terms.
 */
package com.yahoo.bullet.record.simple;

import com.yahoo.bullet.record.BulletRecordTest;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@SuppressWarnings("unchecked")
public class UntypedSimpleBulletRecordTest extends BulletRecordTest<Object> {
    private UntypedSimpleBulletRecord simpleRecord;
    private UntypedSimpleBulletRecord simpleAnother;

    @Override
    protected Object revert(Object data) {
        return data;
    }

    @BeforeMethod
    public void setup() {
        simpleRecord = new UntypedSimpleBulletRecord();
        simpleAnother = new UntypedSimpleBulletRecord();
        record = simpleRecord;
        another = simpleAnother;
    }

    @Test
    public void testHashcodeEdgeCases() {
        Assert.assertEquals(record.hashCode(), another.hashCode());

        simpleRecord.setData(null);
        simpleAnother.setData(null);
        Assert.assertEquals(simpleRecord.hashCode(), simpleAnother.hashCode());
    }

    @Test
    public void testEqualsAndHashcodeCorruptedRecord() {
        simpleRecord.setData(null);
        simpleAnother.setData(null);
        Assert.assertTrue(simpleRecord.equals(simpleAnother));
        Assert.assertEquals(simpleRecord.hashCode(), simpleAnother.hashCode());

        simpleAnother = new UntypedSimpleBulletRecord();
        simpleAnother.setString("foo", "bar");

        Assert.assertFalse(simpleRecord.equals(simpleAnother));
        // This may or may not be true. Much more likely to be true, so leaving it in
        Assert.assertNotEquals(simpleRecord.hashCode(), simpleAnother.hashCode());
    }
}
