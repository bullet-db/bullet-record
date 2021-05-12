/*
 *  Copyright 2020, Yahoo Inc.
 *  Licensed under the terms of the Apache License, Version 2.0.
 *  See the LICENSE file associated with the project for terms.
 */
package com.yahoo.bullet.record.avro;

import com.yahoo.bullet.typesystem.Type;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.yahoo.bullet.TestHelpers.list;
import static com.yahoo.bullet.TestHelpers.map;
import static com.yahoo.bullet.TestHelpers.nestedList;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;

public class LazyBulletAvroTest {
    private LazyBulletAvro avro;
    private LazyBulletAvro another;

    public static byte[] getAvroBytes(Map<String, Object> data) {
        try {
            // Keep this independent from the code
            BulletAvro record = new BulletAvro(data);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            SpecificDatumWriter<BulletAvro> writer = new SpecificDatumWriter<>(BulletAvro.class);
            Encoder encoder = new EncoderFactory().directBinaryEncoder(stream, null);
            stream.reset();
            writer.write(record, encoder);
            encoder.flush();
            return stream.toByteArray();
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    public static byte[] getAvroBytes(LazyBulletAvro data) {
        try {
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            ObjectOutputStream outputStream = new ObjectOutputStream(byteStream);
            outputStream.writeObject(data);
            outputStream.close();
            return byteStream.toByteArray();
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    public static LazyBulletAvro fromRecordBytes(byte[] data) {
        try {
            ObjectInputStream stream = new ObjectInputStream(new ByteArrayInputStream(data));
            return (LazyBulletAvro) stream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @BeforeMethod
    public void setup() {
        avro = new LazyBulletAvro();
        another = new LazyBulletAvro();
    }
    
    @Test
    public void testNoArgsConstructor() {
        Assert.assertTrue(avro.isDeserialized);
        Assert.assertNotNull(avro.data);
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testFailCopying() {
        avro.set("foo", Type.NULL);
        new LazyBulletAvro(avro);
    }

    @Test
    public void testCopyOfCopy() {
        avro.set("someField", "someValue");
        LazyBulletAvro copy = new LazyBulletAvro(avro);
        LazyBulletAvro copyOfCopy = new LazyBulletAvro(copy);
        Assert.assertEquals(copyOfCopy.get("someField"), "someValue");
        Assert.assertEquals(copyOfCopy.fieldCount(), 1);
    }

    @Test
    public void testEqualsAndHashcodeByteArrays() {
        HashMap<String, Boolean> data = new HashMap<>();
        data.put("4.1", false);
        data.put("4.2", true);
        data.put("4.3", null);
        avro.set("1", "bar").set("2", 42L).set("3", false)
            .set("4", data)
            .set("5", nestedList(singletonList(singletonMap("5.1", "foo"))))
            .set("6", list(singletonList("baz")));
        another.set("1", "bar").set("2", 42L).set("3", false)
               .set("4", data)
               .set("5", nestedList(singletonList(singletonMap("5.1", "foo"))))
               .set("6", list(singletonList("baz")));

        avro.serializedData = getAvroBytes(avro);
        avro.isDeserialized = false;
        another.serializedData = getAvroBytes(another);
        another.isDeserialized = false;

        Assert.assertFalse(avro.equals(data));
        Assert.assertTrue(avro.equals(another));
        Assert.assertEquals(avro.hashCode(), another.hashCode());
    }

    @Test
    public void testEqualsAndHashcodeSameRecord() {
        // If you reify the changed order and compare, it still should be equal
        avro.serializedData = getAvroBytes(avro);
        avro.isDeserialized = false;
        another.serializedData = getAvroBytes(another);
        another.isDeserialized = false;
        Assert.assertTrue(avro.equals(another));
        Assert.assertEquals(avro.hashCode(), another.hashCode());
    }

    @Test
    public void testEqualsAndHashcodeCorruptedRecord() {
        avro.serializedData = null;
        avro.data = null;
        avro.isDeserialized = false;
        // For coverage
        Assert.assertNull(avro.serializedData);
        another.serializedData = null;
        another.data = null;
        another.isDeserialized = false;
        Assert.assertTrue(avro.equals(another));
        Assert.assertEquals(avro.hashCode(), another.hashCode());

        another = new LazyBulletAvro();
        another.set("foo", "bar");

        Assert.assertFalse(avro.equals(another));
        // This may or may not be true. Much more likely to be true, so leaving it in
        Assert.assertNotEquals(avro.hashCode(), another.hashCode());
    }

    @Test
    public void testForceReadData() {
        // If already deserialized, should succeed
        Assert.assertTrue(avro.forceReadData());

        // Force a bad read with no data and not deserialized.
        avro.isDeserialized = false;
        Assert.assertFalse(avro.forceReadData());

        // Set the data to an invalid byte array and force a read
        avro = new LazyBulletAvro();
        avro.serializedData = "foo".getBytes();
        avro.isDeserialized = false;
        Assert.assertFalse(avro.forceReadData());

        // Set the data to a valid byte array and force a read
        avro = new LazyBulletAvro();
        Map<String, Object> data = new HashMap<>();
        data.put("foo", singletonMap("bar", "baz"));
        data.put("qux", singletonList(singletonMap("bar", "baz")));
        avro.serializedData = getAvroBytes(data);
        avro.isDeserialized = false;
        Assert.assertTrue(avro.forceReadData());
        Assert.assertEquals(avro.get("foo"), singletonMap("bar", "baz"));

        // Further force reads should return true
        Assert.assertTrue(avro.forceReadData());
        Assert.assertTrue(avro.forceReadData());
    }

    @Test
    public void testSerializationDeserializationOfEmptyRecord() {
        byte[] serialized = getAvroBytes(avro);
        Assert.assertNotNull(serialized);

        LazyBulletAvro reified = fromRecordBytes(serialized);
        Assert.assertNotNull(reified);

        byte[] serializedAgain = getAvroBytes(avro);
        Assert.assertEquals(serializedAgain, serialized);
    }

    @Test
    public void testSerializationWithBadStates() {
        byte[] serialized = getAvroBytes(avro);
        LazyBulletAvro reified = fromRecordBytes(serialized);

        // This will destroy the byte array and force the record to think it has data in its map (it doesn't)
        reified.serializedData = null;
        reified.isDeserialized = true;

        // The record should handle this case by pretending to be an empty record
        byte[] serializedAgain = getAvroBytes(reified);
        Assert.assertEquals(serializedAgain, serialized);
    }

    @Test
    public void testDeserializationWithBadStates() {
        byte[] serialized = getAvroBytes(avro);
        LazyBulletAvro reified = fromRecordBytes(serialized);

        // This will destroy the byte array, force the record to think it doesn't have data in its map and then
        // force a read
        reified.serializedData = null;
        reified.isDeserialized = false;
        Assert.assertFalse(reified.forceReadData());

        // But the record is still iterable
        Assert.assertNotNull(reified.iterator());
        Assert.assertNull(reified.get("some field"));
    }

    @Test
    public void testSerializationDeserializationWithoutReading() {
        avro.set("1", "bar").set("2", 42L).set("3", false).set("4", 0.34);

        byte[] serialized = getAvroBytes(avro);
        Assert.assertNotNull(serialized);
        LazyBulletAvro reified = fromRecordBytes(serialized);

        // Read and write without accessing anything
        serialized = getAvroBytes(reified);
        reified = fromRecordBytes(serialized);

        Assert.assertNotNull(reified);
        Assert.assertEquals(reified.get("4"), 0.34);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSerializationDeserializationWithDataInput() {
        HashMap<String, Boolean> dataA = new HashMap<>();
        dataA.put("5.1", false);
        dataA.put("5.2", true);
        dataA.put("5.3", null);
        HashMap<String, String> dataB = new HashMap<>();
        dataB.put("6.1", "foo");
        dataB.put("6.2", "bar");
        dataB.put("6.3", "baz");
        avro.set("1", "bar").set("2", 42L).set("3", false).set("4", 0.34)
            .set("5", dataA).set("6", dataB)
            .set("7", nestedList(singletonList(singletonMap("7.1", 3L))))
            .set("8", nestedList(singletonList(singletonMap("8.1", true))));

        // SerDe
        byte[] serialized = getAvroBytes(avro);
        LazyBulletAvro reified = fromRecordBytes(serialized);

        Assert.assertNotNull(serialized);
        Assert.assertNotNull(reified);
        Assert.assertEquals(reified.get("2"), 42L);
        Assert.assertTrue(((Map<String, Boolean>) reified.get("5")).get("5.2"));
        Assert.assertNull(((Map<String, Boolean>) reified.get("5")).get("5.3"));
        Assert.assertEquals(((List<Map<String, Long>>) reified.get("7")).get(0), singletonMap("7.1", 3L));

        // Add new fields and modify fields in the record
        reified.set("2", map(singletonMap("2.1", 42L)));
        reified.set("9", 4L);
        reified.set("10", map(singletonMap("10.1", "foo")));

        // SerDe again
        serialized = getAvroBytes(reified);
        reified = fromRecordBytes(serialized);

        // The old ones that went through the first cycle
        Assert.assertNotNull(reified);
        Assert.assertEquals(reified.get("1"), "bar");
        Assert.assertEquals(reified.get("2"), singletonMap("2.1", 42L));
        Assert.assertFalse((Boolean) reified.get("3"));
        Assert.assertEquals(reified.get("4"), 0.34);
        Assert.assertNull(((Map<String, Boolean>) reified.get("5")).get("5.3"));
        Assert.assertEquals(((Map<String, String>) reified.get("6")).get("6.3"), "baz");
        Assert.assertEquals(((List<Map<String, Boolean>>) reified.get("8")).get(0), singletonMap("8.1", true));

        // New ones added at the last cycle
        Assert.assertEquals(reified.get("9"), 4L);
        Assert.assertEquals(((Map<String, String>) reified.get("10")).get("10.1"), "foo");
    }

    @Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = "Cannot read from record.*")
    public void testFailingWhenCannotRead() {
        avro.serializedData = "foo".getBytes();
        avro.isDeserialized = false;
        avro.hasField("foo");
    }
}
