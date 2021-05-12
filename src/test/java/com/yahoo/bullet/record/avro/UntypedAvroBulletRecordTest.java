/*
 *  Copyright 2016, Yahoo Inc.
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
import java.io.Serializable;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;

@SuppressWarnings("unchecked")
public class UntypedAvroBulletRecordTest extends BulletRecordTest<Serializable> {
    private UntypedAvroBulletRecord avroRecord;
    private UntypedAvroBulletRecord avroAnother;

    @Override
    protected Serializable revert(Serializable data) {
        return data;
    }

    public static byte[] getRecordBytes(UntypedAvroBulletRecord record) {
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

    public static UntypedAvroBulletRecord fromRecordBytes(byte[] data) {
        try {
            ObjectInputStream stream = new ObjectInputStream(new ByteArrayInputStream(data));
            return (UntypedAvroBulletRecord) stream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @BeforeMethod
    public void setup() {
        avroRecord = new UntypedAvroBulletRecord();
        avroAnother = new UntypedAvroBulletRecord();
        record = avroRecord;
        another = avroAnother;
    }

    @Test
    public void testAvroRecordEqualsEdgeCases() {
        Assert.assertTrue(avroRecord.equals(avroAnother));
        avroRecord.data = null;
        Assert.assertFalse(avroRecord.equals(another));
        avroAnother.data = null;
        Assert.assertTrue(avroRecord.equals(avroAnother));
    }

    @Test
    public void testAvroRecordHashcodeEdgeCases() {
        Assert.assertEquals(record.hashCode(), another.hashCode());
        avroRecord.data = null;
        avroAnother.data = null;
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

        UntypedAvroBulletRecord reified = fromRecordBytes(serialized);
        Assert.assertNotNull(reified);
        Assert.assertEquals(reified.get("2"), 42L);
        assertTypedEquals(reified.typedGet("7", "7.2"), Type.BOOLEAN, true);
        assertTypedEquals(reified.typedGet("7", "7.3"), TypedObject.NULL);
        assertTypedEquals(reified.typedGet("9", 0), Type.LONG_MAP, singletonMap("9.1", 3L));
    }

    @Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = "Cannot read from record.*")
    public void testFailingWhenCannotRead() {
        LazyBulletAvro bad = new LazyBulletAvro();
        bad.serializedData = "foo".getBytes();
        bad.isDeserialized = false;
        avroRecord.data = bad;
        avroRecord.hasField("foo");
    }
}
