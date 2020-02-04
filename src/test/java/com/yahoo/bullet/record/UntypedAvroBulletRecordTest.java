/*
 *  Copyright 2016, Yahoo Inc.
 *  Licensed under the terms of the Apache License, Version 2.0.
 *  See the LICENSE file associated with the project for terms.
 */
package com.yahoo.bullet.record;

/*
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.commons.lang3.tuple.Pair;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
*/

@SuppressWarnings("unchecked")
public class UntypedAvroBulletRecordTest extends BulletRecordTest<Object> {
    private UntypedAvroBulletRecord avroRecord;
    private UntypedAvroBulletRecord avroAnother;

    /* TODO: Fix tests
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
    public void testHashcodeEdgeCases() {
        Assert.assertEquals(record.hashCode(), another.hashCode());

        avroRecord.setSerializedData(null);
        avroRecord.setData(null);
        avroAnother.setSerializedData(null);
        avroAnother.setData(null);
        Assert.assertEquals(avroRecord.hashCode(), avroAnother.hashCode());
    }

    @Test
    public void testEqualsAndHashcodeByteArrays() {
        avroRecord.setMap("4", Pair.of("4.1", false), Pair.of("4.2", true), Pair.of("4.3", null))
                  .setString("1", "bar").setLong("2", 42L).setBoolean("3", false)
                  .setListOfStringMap("5", singletonList(singletonMap("5.1", "foo")))
                  .setStringList("6", singletonList("baz"));
        avroAnother.setMap("4", Pair.of("4.1", false), Pair.of("4.2", true), Pair.of("4.3", null))
                   .setString("1", "bar").setLong("2", 42L).setBoolean("3", false)
                   .setListOfStringMap("5", singletonList(singletonMap("5.1", "foo")))
                   .setStringList("6", singletonList("baz"));

        avroRecord.setSerializedData(getRecordBytes(avroRecord));
        avroRecord.setDeserialized(false);
        avroAnother.setSerializedData(getRecordBytes(avroAnother));
        avroAnother.setDeserialized(false);

        Assert.assertTrue(avroRecord.equals(avroAnother));
        Assert.assertEquals(avroRecord.hashCode(), avroAnother.hashCode());
    }

    @Test
    @Override
    public void testEqualsAndHashcodeSameRecord() {
        super.testEqualsAndHashcodeSameRecord();

        // If you reify the changed order and compare, it still should be equal
        avroRecord.setSerializedData(getRecordBytes(avroRecord));
        avroRecord.setDeserialized(false);
        avroAnother.setSerializedData(getRecordBytes(avroAnother));
        avroAnother.setDeserialized(false);
        Assert.assertTrue(avroRecord.equals(avroAnother));
        Assert.assertEquals(avroRecord.hashCode(), avroAnother.hashCode());
    }

    @Test
    public void testEqualsAndHashcodeCorruptedRecord() {
        avroRecord.setSerializedData(null);
        avroRecord.setData(null);
        avroRecord.setDeserialized(false);
        avroAnother.setSerializedData(null);
        avroAnother.setData(null);
        avroAnother.setDeserialized(false);
        Assert.assertTrue(avroRecord.equals(avroAnother));
        Assert.assertEquals(avroRecord.hashCode(), avroAnother.hashCode());

        another = new UntypedAvroBulletRecord();
        another.setString("foo", "bar");

        Assert.assertFalse(record.equals(another));
        // This may or may not be true. Much more likely to be true, so leaving it in
        Assert.assertNotEquals(record.hashCode(), another.hashCode());
    }

    @Test
    public void testForceReadData() {
        // If already deserialized, should succeed
        Assert.assertTrue(avroRecord.forceReadData());

        // Force a bad read with no data and not deserialized.
        avroRecord.setDeserialized(false);
        Assert.assertFalse(avroRecord.forceReadData());

        // Set the data to an invalid byte array and force a read
        avroRecord = new UntypedAvroBulletRecord();
        avroRecord.setSerializedData("foo".getBytes());
        avroRecord.setDeserialized(false);
        Assert.assertFalse(avroRecord.forceReadData());

        // Set the data to a valid byte array and force a read
        avroRecord = new UntypedAvroBulletRecord();
        Map<String, Object> data = new HashMap<>();
        data.put("foo", singletonMap("bar", "baz"));
        data.put("qux", singletonList(singletonMap("bar", "baz")));
        avroRecord.setSerializedData(getAvroBytes(data));
        avroRecord.setDeserialized(false);
        Assert.assertTrue(avroRecord.forceReadData());
        Assert.assertEquals(avroRecord.get("foo"), singletonMap("bar", "baz"));
        Assert.assertEquals(avroRecord.get("foo", "bar"), "baz");
        Assert.assertEquals(avroRecord.get("qux"), singletonList(singletonMap("bar", "baz")));
        Assert.assertEquals(avroRecord.get("qux", 0), singletonMap("bar", "baz"));

        // Further force reads should return true
        Assert.assertTrue(avroRecord.forceReadData());
        Assert.assertTrue(avroRecord.forceReadData());
    }

    @Test
    public void testSerializationDeserialization() {
        avroRecord.setMap("7", Pair.of("4.1", false), Pair.of("7.2", true), Pair.of("7.3", null))
                  .setMap("8", Pair.of("8.1", "foo"), Pair.of("8.2", "bar"), Pair.of("8.3", "baz"))
                  .setString("1", "bar").setLong("2", 42L).setBoolean("3", false).setDouble("4", 0.34)
                  .setListOfLongMap("9", singletonList(singletonMap("9.1", 3L)))
                  .setListOfBooleanMap("10", singletonList(singletonMap("10.1", true)));

        byte[] serialized = getRecordBytes(avroRecord);
        Assert.assertNotNull(serialized);

        UntypedAvroBulletRecord reified = fromRecordBytes(serialized);
        Assert.assertNotNull(reified);
        Assert.assertEquals(reified.get("2"), 42L);
        Assert.assertTrue((Boolean) reified.get("7", "7.2"));
        Assert.assertNull(reified.get("7", "7.3"));
        Assert.assertEquals(reified.get("9", 0), singletonMap("9.1", 3L));
    }

    @Test
    public void testSerializationDeserializationOfEmptyRecord() {
        byte[] serialized = getRecordBytes(avroRecord);
        Assert.assertNotNull(serialized);

        UntypedAvroBulletRecord reified = fromRecordBytes(serialized);
        Assert.assertNotNull(reified);

        byte[] serializedAgain = getRecordBytes(avroRecord);
        Assert.assertEquals(serializedAgain, serialized);
    }

    @Test
    public void testSerializationWithBadStates() {
        byte[] serialized = getRecordBytes(avroRecord);
        UntypedAvroBulletRecord reified = fromRecordBytes(serialized);

        // This will destroy the byte array and force the record to think it has data in its map (it doesn't)
        reified.setSerializedData(null);
        reified.setDeserialized(true);

        // The record should handle this case by pretending to be an empty record
        byte[] serializedAgain = getRecordBytes(reified);
        Assert.assertEquals(serializedAgain, serialized);
    }

    @Test
    public void testDeserializationWithBadStates() {
        byte[] serialized = getRecordBytes(avroRecord);
        UntypedAvroBulletRecord reified = fromRecordBytes(serialized);

        // This will destroy the byte array, force the record to think it doesn't have data in its map and then
        // force a read
        reified.setSerializedData(null);
        reified.setDeserialized(false);
        Assert.assertFalse(reified.forceReadData());

        // But the record is still iterable
        Assert.assertNotNull(reified.iterator());
        Assert.assertNull(reified.get("some field"));
    }

    @Test
    public void testSerializationDeserializationWithoutReading() {
        record.setString("1", "bar").setLong("2", 42L).setBoolean("3", false).setDouble("4", 0.34);

        byte[] serialized = getRecordBytes(avroRecord);
        Assert.assertNotNull(serialized);
        UntypedAvroBulletRecord reified = fromRecordBytes(serialized);

        // Read and write without accessing anything
        serialized = getRecordBytes(reified);
        reified = fromRecordBytes(serialized);

        Assert.assertNotNull(reified);
        Assert.assertEquals(reified.get("4"), 0.34);
    }

    @Test
    public void testSerializationDeserializationWithDataInput() {
        avroRecord.setMap("7", Pair.of("4.1", false), Pair.of("7.2", true), Pair.of("7.3", null))
                  .setMap("8", Pair.of("8.1", "foo"), Pair.of("8.2", "bar"), Pair.of("8.3", "baz"))
                  .setString("1", "bar").setLong("2", 42L).setBoolean("3", false).setDouble("4", 0.34)
                  .setListOfLongMap("9", singletonList(singletonMap("9.1", 3L)))
                  .setListOfBooleanMap("10", singletonList(singletonMap("10.1", true)));

        // SerDe
        byte[] serialized = getRecordBytes(avroRecord);
        UntypedAvroBulletRecord reified = fromRecordBytes(serialized);

        Assert.assertNotNull(serialized);
        Assert.assertNotNull(reified);
        Assert.assertEquals(reified.get("2"), 42L);
        Assert.assertTrue((Boolean) reified.get("7", "7.2"));
        Assert.assertNull(reified.get("7", "4.3"));
        Assert.assertEquals(reified.get("9", 0), singletonMap("9.1", 3L));

        // Add new fields and modify fields in the record
        reified.setMap("2", Pair.of("2.1", 42L));
        reified.setLong("11", 4L);
        reified.setMap("12", Pair.of("12.1", "foo"));

        // SerDe again
        serialized = getRecordBytes(reified);
        reified = fromRecordBytes(serialized);

        // The old ones that went through the first cycle
        Assert.assertNotNull(reified);
        Assert.assertEquals(reified.get("1"), "bar");
        Assert.assertEquals(reified.get("2", "2.1"), 42L);
        Assert.assertFalse((Boolean) reified.get("3"));
        Assert.assertEquals(reified.get("4"), 0.34);
        Assert.assertNull(reified.get("7", "7.3"));
        Assert.assertEquals(reified.get("8", "8.3"), "baz");
        Assert.assertEquals(reified.get("10", 0), singletonMap("10.1", true));

        // New ones added at the last cycle
        Assert.assertEquals(reified.get("11"), 4L);
        Assert.assertEquals(reified.get("12", "12.1"), "foo");
    }

    @Test
    public void testFieldCount() throws Exception {
        super.testFieldCount();

        avroAnother = new UntypedAvroBulletRecord(avroRecord);
        Assert.assertEquals(avroAnother.fieldCount(), 1);
    }

    @Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = "Cannot read from record.*")
    public void testFailingWhenCannotRead() {
        avroRecord.setSerializedData("foo".getBytes());
        avroRecord.setDeserialized(false);
        avroRecord.hasField("foo");
    }

    @Test
    public void testNoArgsConstructor() {
        Assert.assertTrue(avroRecord.isDeserialized());
        Assert.assertNotNull(avroRecord.getData());
    }

    @Test
    public void testCopyConstructor() throws Exception {
        avroRecord.setString("someField", "someValue");
        UntypedAvroBulletRecord copy = new UntypedAvroBulletRecord(avroRecord);
        Assert.assertEquals(copy.get("someField"), "someValue");
        Assert.assertEquals(copy.fieldCount(), 1);
    }

    @Test
    public void testCopyOfCopy() throws Exception {
        avroRecord.setString("someField", "someValue");
        UntypedAvroBulletRecord copy = new UntypedAvroBulletRecord(avroRecord);
        UntypedAvroBulletRecord copyOfCopy = new UntypedAvroBulletRecord(copy);
        Assert.assertEquals(copyOfCopy.get("someField"), "someValue");
        Assert.assertEquals(copyOfCopy.fieldCount(), 1);
    }
    */
}
