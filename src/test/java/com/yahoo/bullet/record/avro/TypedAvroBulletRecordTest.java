/*
 *  Copyright 2020, Yahoo Inc.
 *  Licensed under the terms of the Apache License, Version 2.0.
 *  See the LICENSE file associated with the project for terms.
 */
package com.yahoo.bullet.record.avro;

import com.yahoo.bullet.record.BulletRecordTest;
import com.yahoo.bullet.typesystem.Type;
import com.yahoo.bullet.typesystem.TypedObject;
import org.apache.commons.lang3.tuple.Pair;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;

@SuppressWarnings("unchecked")
public class TypedAvroBulletRecordTest extends BulletRecordTest<TypedObject> {
    private TypedAvroBulletRecord avroRecord;
    private TypedAvroBulletRecord avroAnother;

    @Override
    protected Object revert(TypedObject data) {
        // If get or getAndRemove was used to retrive this, we need to handle nulls
        if (data == null) {
            return null;
        }
        return data.getType() == Type.NULL ? null : data.getValue();
    }

    public static byte[] getRecordBytes(TypedAvroBulletRecord record) {
        try {
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            ObjectOutputStream outputStream = new ObjectOutputStream(byteStream);
            outputStream.writeObject(record);
            outputStream.close();
            return byteStream.toByteArray();
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    public static TypedAvroBulletRecord fromRecordBytes(byte[] data) {
        try {
            ObjectInputStream stream = new ObjectInputStream(new ByteArrayInputStream(data));
            return (TypedAvroBulletRecord) stream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @BeforeMethod
    public void setup() {
        avroRecord = new TypedAvroBulletRecord();
        avroAnother = new TypedAvroBulletRecord();
        record = avroRecord;
        another = avroAnother;
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
        avroRecord.setData(null);
        avroAnother.setData(null);
        Assert.assertEquals(avroRecord.hashCode(), avroAnother.hashCode());
    }

    @Test
    public void testSerializationDeserialization() {
        setMap(record, "7", Pair.of("4.1", false), Pair.of("7.2", true), Pair.of("7.3", null));
        setMap(record, "8", Pair.of("8.1", "foo"), Pair.of("8.2", "bar"), Pair.of("8.3", "baz"));
        record.setString("1", "bar").setLong("2", 42L).setBoolean("3", false).setDouble("4", 0.34)
              .setListOfLongMap("9", singletonList(singletonMap("9.1", 3L)))
              .setListOfBooleanMap("10", singletonList(singletonMap("10.1", true)));

        byte[] serialized = getRecordBytes(avroRecord);
        Assert.assertNotNull(serialized);

        TypedAvroBulletRecord reified = fromRecordBytes(serialized);
        Assert.assertNotNull(reified);
        assertTypedEquals(reified.get("2"), Type.LONG, 42L);
        assertTypedEquals(reified.typedGet("7", "7.2"), Type.BOOLEAN, true);
        assertTypedEquals(reified.typedGet("7", "7.3"), TypedObject.NULL);
        assertTypedEquals(reified.typedGet("9", 0), Type.LONG_MAP, singletonMap("9.1", 3L));
    }

    @Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = "Cannot read from record.*")
    public void testFailingWhenCannotRead() {
        LazyBulletAvro bad = new LazyBulletAvro();
        bad.setSerializedData("foo".getBytes());
        bad.setDeserialized(false);
        avroRecord.setData(bad);
        avroRecord.hasField("foo");
    }
}
