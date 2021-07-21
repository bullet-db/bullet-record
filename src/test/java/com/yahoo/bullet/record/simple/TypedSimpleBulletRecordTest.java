/*
 *  Copyright 2020, Yahoo Inc.
 *  Licensed under the terms of the Apache License, Version 2.0.
 *  See the LICENSE file associated with the project for terms.
 */
package com.yahoo.bullet.record.simple;

import com.yahoo.bullet.record.BulletRecordTest;
import com.yahoo.bullet.typesystem.Type;
import com.yahoo.bullet.typesystem.TypedObject;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.Serializable;

@SuppressWarnings("unchecked")
public class TypedSimpleBulletRecordTest extends BulletRecordTest<TypedObject> {
    private TypedSimpleBulletRecord simpleRecord;
    private TypedSimpleBulletRecord simpleAnother;

    @Override
    protected Serializable revert(TypedObject data) {
        // If get was used to retrieve this, we need to handle nulls
        if (data == null) {
            return null;
        }
        return data.getType() == Type.NULL ? null : data.getValue();
    }

    @BeforeMethod
    public void setup() {
        simpleRecord = new TypedSimpleBulletRecord();
        simpleAnother = new TypedSimpleBulletRecord();
        record = simpleRecord;
        another = simpleAnother;
    }

    @Override
    @Test
    public void testToString() {
        Assert.assertEquals(record.toString(), "{}");
        record.setString("1", "bar");
        Assert.assertEquals(record.toString(), "{1:bar::STRING}");
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

        simpleAnother = new TypedSimpleBulletRecord();
        simpleAnother.setString("foo", "bar");

        Assert.assertFalse(simpleRecord.equals(simpleAnother));
        // This may or may not be true. Much more likely to be true, so leaving it in
        Assert.assertNotEquals(simpleRecord.hashCode(), simpleAnother.hashCode());
    }
}
