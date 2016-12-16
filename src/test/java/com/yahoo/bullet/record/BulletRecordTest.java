/*
 *  Copyright 2016, Yahoo Inc.
 *  Licensed under the terms of the Apache License, Version 2.0.
 *  See the LICENSE file associated with the project for terms.
 */
package com.yahoo.bullet.record;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;

@SuppressWarnings("unchecked")
public class BulletRecordTest {
    private BulletRecord record;

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

    public static byte[] getRecordBytes(BulletRecord record) {
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

    public static BulletRecord fromRecordBytes(byte[] data) {
        try {
            ObjectInputStream stream = new ObjectInputStream(new ByteArrayInputStream(data));
            return (BulletRecord) stream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @BeforeMethod
    public void setup() {
        record = new BulletRecord();
    }

    @Test
    public void testSetBoolean() {
        record.setBoolean("foo", false).setBoolean("bar", true).setBoolean("baz", null);

        Assert.assertTrue((Boolean) record.get("bar"));
        Assert.assertFalse((Boolean) record.get("foo"));
        Assert.assertNull(record.get("baz"));
        Assert.assertNull(record.get("dne"));
    }

    @Test
    public void testSetString() {
        record.setString("foo", "bar").setString("bar", "baz");

        Assert.assertEquals(record.get("foo"), "bar");
        Assert.assertEquals(record.get("bar"), "baz");
        Assert.assertNull(record.get("dne"));
    }

    @Test
    public void testSetLong() {
        record.setLong("foo", 0L).setLong("bar", -1231231231231231231L).setLong("baz", null);

        Assert.assertEquals(record.get("foo"), 0L);
        Assert.assertEquals(record.get("bar"), -1231231231231231231L);
        Assert.assertNull(record.get("baz"));
        Assert.assertNull(record.get("dne"));
    }

    @Test
    public void testSetDouble() {
        record.setDouble("foo", 42.1).setDouble("bar", -1.13).setDouble("baz", null);

        Assert.assertEquals(record.get("foo"), 42.1);
        Assert.assertEquals(record.get("bar"), -1.13);
        Assert.assertNull(record.get("baz"));
        Assert.assertNull(record.get("dne"));
    }

    @Test
    public void testSetMaps() {
        Map<String, Long> dataA = new HashMap<>();
        dataA.put("bar", 1L);
        dataA.put("baz", 2L);
        record.setLongMap("fooA", dataA);
        Assert.assertTrue(record.get("fooA") instanceof Map);
        Assert.assertEquals(record.get("fooA", "bar"), 1L);
        Assert.assertEquals(record.get("fooA", "baz"), 2L);

        Map<String, Double> dataB = new HashMap<>();
        dataB.put("bar", 1.1);
        dataB.put("baz", 2.2);
        record.setDoubleMap("fooB", dataB);
        Assert.assertTrue(record.get("fooB") instanceof Map);
        Assert.assertEquals(record.get("fooB", "bar"), 1.1);
        Assert.assertEquals(record.get("fooB", "baz"), 2.2);

        Map<String, Boolean> dataC = new HashMap<>();
        dataC.put("bar", false);
        dataC.put("baz", true);
        record.setBooleanMap("fooC", dataC);
        Assert.assertTrue(record.get("fooC") instanceof Map);
        Assert.assertEquals(record.get("fooC", "bar"), false);
        Assert.assertEquals(record.get("fooC", "baz"), true);

        Map<String, String> dataD = new HashMap<>();
        dataD.put("bar", "foo");
        dataD.put("baz", "norf");
        record.setStringMap("fooD", dataD);
        Assert.assertTrue(record.get("fooD") instanceof Map);
        Assert.assertEquals(record.get("fooD", "bar"), "foo");
        Assert.assertEquals(record.get("fooD", "baz"), "norf");

        Assert.assertNull(record.get("dne"));
        Assert.assertNull(record.get("dne", "bar"));
        Assert.assertNull(record.get("dne", "baz"));
    }

    @Test
    public void testSetMapOfMaps() {
        Map<String, Map<String, Long>> dataA = new HashMap<>();
        dataA.put("bar", singletonMap("a", 1L));
        dataA.put("baz", singletonMap("b", 2L));
        record.setMapOfLongMap("fooA", dataA);
        Assert.assertTrue(record.get("fooA") instanceof Map);
        Assert.assertTrue(record.get("fooA", "bar") instanceof Map);
        Assert.assertTrue(record.get("fooA", "baz") instanceof Map);
        Assert.assertEquals(record.get("fooA", "bar"), singletonMap("a", 1L));
        Assert.assertEquals(record.get("fooA", "baz"), singletonMap("b", 2L));

        Map<String, Map<String, Double>> dataB = new HashMap<>();
        dataB.put("bar", singletonMap("a", 0.2));
        dataB.put("baz", singletonMap("b", 0.1));
        record.setMapOfDoubleMap("fooB", dataB);
        Assert.assertTrue(record.get("fooB") instanceof Map);
        Assert.assertTrue(record.get("fooB", "bar") instanceof Map);
        Assert.assertTrue(record.get("fooB", "baz") instanceof Map);
        Assert.assertEquals(record.get("fooB", "bar"), singletonMap("a", 0.2));
        Assert.assertEquals(record.get("fooB", "baz"), singletonMap("b", 0.1));

        Map<String, Map<String, Boolean>> dataC = new HashMap<>();
        dataC.put("bar", singletonMap("a", false));
        dataC.put("baz", singletonMap("b", true));
        record.setMapOfBooleanMap("fooC", dataC);
        Assert.assertTrue(record.get("fooC") instanceof Map);
        Assert.assertTrue(record.get("fooC", "bar") instanceof Map);
        Assert.assertTrue(record.get("fooC", "baz") instanceof Map);
        Assert.assertEquals(record.get("fooC", "bar"), singletonMap("a", false));
        Assert.assertEquals(record.get("fooC", "baz"), singletonMap("b", true));

        Map<String, Map<String, String>> dataD = new HashMap<>();
        dataD.put("bar", singletonMap("a", "foo"));
        dataD.put("baz", singletonMap("b", "norf"));
        record.setMapOfStringMap("fooD", dataD);
        Assert.assertTrue(record.get("fooD") instanceof Map);
        Assert.assertTrue(record.get("fooD", "bar") instanceof Map);
        Assert.assertTrue(record.get("fooD", "baz") instanceof Map);
        Assert.assertEquals(record.get("fooD", "bar"), singletonMap("a", "foo"));
        Assert.assertEquals(record.get("fooD", "baz"), singletonMap("b", "norf"));

        Assert.assertNull(record.get("dne"));
        Assert.assertNull(record.get("dne", "bar"));
        Assert.assertNull(record.get("dne", "baz"));
    }

    @Test
    public void testSetListMaps() {
        List<Map<String, Long>> dataA = new ArrayList<>();
        dataA.add(singletonMap("a", 1L));
        dataA.add(singletonMap("b", 2L));
        record.setListOfLongMap("fooA", dataA);
        Assert.assertTrue(record.get("fooA") instanceof List);
        Assert.assertTrue(record.get("fooA", 0) instanceof Map);
        Assert.assertTrue(record.get("fooA", 1) instanceof Map);
        Assert.assertEquals(record.get("fooA", 0), singletonMap("a", 1L));
        Assert.assertEquals(record.get("fooA", 1), singletonMap("b", 2L));

        List<Map<String, Double>> dataB = new ArrayList<>();
        dataB.add(singletonMap("a", 0.2));
        dataB.add(singletonMap("b", 0.1));
        record.setListOfDoubleMap("fooB", dataB);
        Assert.assertTrue(record.get("fooB") instanceof List);
        Assert.assertTrue(record.get("fooB", 0) instanceof Map);
        Assert.assertTrue(record.get("fooB", 1) instanceof Map);
        Assert.assertEquals(record.get("fooB", 0), singletonMap("a", 0.2));
        Assert.assertEquals(record.get("fooB", 1), singletonMap("b", 0.1));

        List<Map<String, Boolean>> dataC = new ArrayList<>();
        dataC.add(singletonMap("a", false));
        dataC.add(singletonMap("b", true));
        record.setListOfBooleanMap("fooC", dataC);
        Assert.assertTrue(record.get("fooC") instanceof List);
        Assert.assertTrue(record.get("fooC", 0) instanceof Map);
        Assert.assertTrue(record.get("fooC", 1) instanceof Map);
        Assert.assertEquals(record.get("fooC", 0), singletonMap("a", false));
        Assert.assertEquals(record.get("fooC", 1), singletonMap("b", true));

        List<Map<String, String>> dataD = new ArrayList<>();
        dataD.add(singletonMap("a", "foo"));
        dataD.add(singletonMap("b", "norf"));
        record.setListOfStringMap("fooD", dataD);
        Assert.assertTrue(record.get("fooD") instanceof List);
        Assert.assertTrue(record.get("fooD", 0) instanceof Map);
        Assert.assertTrue(record.get("fooD", 1) instanceof Map);
        Assert.assertEquals(record.get("fooD", 0), singletonMap("a", "foo"));
        Assert.assertEquals(record.get("fooD", 1), singletonMap("b", "norf"));

        Assert.assertNull(record.get("dne", -1));
        Assert.assertNull(record.get("dne", 0));
        Assert.assertNull(record.get("dne", 123123));
    }

    @Test
    public void testInsertingFromAnotherRecord() {
        record.setString("a", "foo");
        record.setStringMap("b", singletonMap("b.1", "bar"));
        Map<String, Boolean> data = new HashMap<>();
        data.put("c.1", false);
        record.setListOfBooleanMap("c", singletonList(data));

        BulletRecord newRecord = new BulletRecord();

        newRecord.set("newA", record, "a");
        Assert.assertEquals(newRecord.get("newA"), "foo");
        Assert.assertEquals(newRecord.toString(), "{newA:foo}");

        newRecord.set("newB", record, "b", "b.1");
        Assert.assertEquals(newRecord.get("newB"), "bar");
        Assert.assertEquals(newRecord.toString(), "{newA:foo, newB:bar}");

        newRecord.set("newC", record, "c", 0);
        Assert.assertEquals(newRecord.get("newC"), singletonMap("c.1", false));
        Assert.assertEquals(newRecord.toString(), "{newA:foo, newB:bar, newC:{c.1=false}}");

        // The Map isn't copied
        ((Map<String, Boolean>) record.get("c", 0)).put("c.1", true);
        Assert.assertEquals(newRecord.get("newC"), singletonMap("c.1", true));

        // And not in the first record either
        data.put("c.2", false);
        Assert.assertEquals(((Map) record.get("c", 0)).get("c.2"), false);
        Assert.assertEquals(newRecord.get("newC", "c.2"), false);
    }

    @Test
    public void testGetTopLevelField() {
        record.setListOfStringMap("foo", singletonList(singletonMap("qux", "norf")));
        record.setString("bar", null);

        Assert.assertNull(record.get("bar"));
        Assert.assertTrue(record.get("foo") instanceof List);
        Assert.assertEquals(record.get("foo"), singletonList(singletonMap("qux", "norf")));
        Assert.assertNull(record.get("dne"));
    }

    @Test
    public void testGetMapField() {
        record.setMap("foo", Pair.of("bar", "baz"), Pair.of("baz", "qux"));

        Assert.assertTrue(record.get("foo") instanceof Map);
        Assert.assertEquals(record.get("foo", "bar"), "baz");
        Assert.assertEquals(record.get("foo", "baz"), "qux");

        Assert.assertNull(record.get("dne"));
        Assert.assertNull(record.get("foo", null));
        Assert.assertNull(record.get("foo", ""));
        Assert.assertNull(record.get("foo", "dne"));
    }

    @Test
    public void testGetListIndex() {
        record.setListMap("foo", singletonMap("a", 1L), singletonMap("b", 2L));
        Assert.assertTrue(record.get("foo") instanceof List);
        Assert.assertEquals(record.get("foo", 0), singletonMap("a", 1L));
        Assert.assertEquals(record.get("foo", 1), singletonMap("b", 2L));

        Assert.assertNull(record.get("dne", -1));
        Assert.assertNull(record.get("dne", 0));
        Assert.assertNull(record.get("dne", 123123));
    }

    @Test(expectedExceptions = IndexOutOfBoundsException.class)
    public void testGetListBadIndex() {
        record.setListMap("foo", singletonMap("a", 1L));
        record.get("foo", 1);
    }

    @Test
    public void testIterator() {
        record.setString("1", "bar").setLong("2", 42L)
              .setBoolean("3", false)
              .setMap("4", Pair.of("4.1", false))
              .setListOfDoubleMap("5", singletonList(singletonMap("5.1", 3.1)));
        Map<String, Object> expectedMap = new HashMap<>();
        expectedMap.put("1", "bar");
        expectedMap.put("2", 42L);
        expectedMap.put("3", false);
        expectedMap.put("4", singletonMap("4.1", false));
        expectedMap.put("5", singletonList(singletonMap("5.1", 3.1)));

        int lastEntry = 0;
        for (Map.Entry<String, Object> entry : record) {
            int currentEntry = Integer.valueOf(entry.getKey());
            Assert.assertTrue(lastEntry + 1 == currentEntry);
            Assert.assertEquals(entry.getValue(), expectedMap.get(entry.getKey()));
            lastEntry = currentEntry;
        }
    }

    @Test
    public void testToString() {
        Assert.assertEquals(record.toString(), "{}");
        record.setString("1", "bar").setLong("2", 42L).setBoolean("3", false)
              .setMap("4", Pair.of("4.1", false), Pair.of("4.2", true), Pair.of("4.3", null))
              .setListOfStringMap("5", singletonList(singletonMap("5.1", "foo")));
        Assert.assertEquals(record.toString(), "{1:bar, 2:42, 3:false, 4:{4.1=false, 4.2=true, 4.3=null}, 5:[{5.1=foo}]}");
    }

    @Test
    public void testHashcodeEdgeCases() {
        Assert.assertEquals(record.hashCode(), new BulletRecord().hashCode());

        record.setSerializedData(null);
        record.setData(null);
        BulletRecord another = new BulletRecord();
        another.setSerializedData(null);
        another.setData(null);
        Assert.assertEquals(record.hashCode(), another.hashCode());
    }

    @Test
    public void testEqualsEdgeCases() {
        record.setString("1", "bar").setLong("2", 42L).setBoolean("3", false);
        Assert.assertFalse(record.equals(null));
        Assert.assertFalse(record.equals("foo"));
    }

    @Test
    public void testEqualsDifferentRecord() {
        record.setString("1", "bar").setLong("2", 42L).setBoolean("3", false);
        BulletRecord another = new BulletRecord().setString("1", "bar");
        Assert.assertFalse(record.equals(another));
    }

    @Test
    public void testEqualsAndHashcodeByteArrays() {
        record.setString("1", "bar").setLong("2", 42L).setBoolean("3", false)
              .setMap("4", Pair.of("4.1", false), Pair.of("4.2", true), Pair.of("4.3", null))
              .setListOfStringMap("5", singletonList(singletonMap("5.1", "foo")));
        BulletRecord another = new BulletRecord();
        another.setString("1", "bar").setLong("2", 42L).setBoolean("3", false)
               .setMap("4", Pair.of("4.1", false), Pair.of("4.2", true), Pair.of("4.3", null))
               .setListOfStringMap("5", singletonList(singletonMap("5.1", "foo")));

        record.setSerializedData(getRecordBytes(record));
        record.setDeserialized(false);
        another.setSerializedData(getRecordBytes(another));
        another.setDeserialized(false);

        Assert.assertTrue(record.equals(another));
        Assert.assertEquals(record.hashCode(), another.hashCode());
    }

    @Test
    public void testEqualsAndHashcodeSameRecord() {
        record.setString("1", "bar").setLong("2", 42L).setBoolean("3", false)
              .setMap("4", Pair.of("4.1", false), Pair.of("4.2", true), Pair.of("4.3", null))
              .setListOfStringMap("5", singletonList(singletonMap("5.1", "foo")));
        BulletRecord another = new BulletRecord();
        another.setString("1", "bar").setLong("2", 42L).setBoolean("3", false)
               .setMap("4", Pair.of("4.1", false), Pair.of("4.2", true), Pair.of("4.3", null))
               .setListOfStringMap("5", singletonList(singletonMap("5.1", "foo")));

        Assert.assertTrue(record.equals(another));
        Assert.assertEquals(record.hashCode(), another.hashCode());

        // Change order and it should still pass
        another = new BulletRecord();
        another.setString("1", "bar").setLong("2", 42L).setBoolean("3", false)
               .setListOfStringMap("5", singletonList(singletonMap("5.1", "foo")))
               .setMap("4", Pair.of("4.1", false), Pair.of("4.2", true), Pair.of("4.3", null));
        Assert.assertTrue(record.equals(another));
        Assert.assertEquals(record.hashCode(), another.hashCode());

        // If you reify the changed order and compare, it still should
        record.setSerializedData(getRecordBytes(record));
        record.setDeserialized(false);
        another.setSerializedData(getRecordBytes(another));
        another.setDeserialized(false);
        Assert.assertTrue(record.equals(another));
        Assert.assertEquals(record.hashCode(), another.hashCode());
    }

    @Test
    public void testEqualsAndHashcodeCorruptedRecord() {
        record.setSerializedData(null);
        record.setData(null);
        record.setDeserialized(false);

        BulletRecord another = new BulletRecord();
        another.setSerializedData(null);
        another.setData(null);
        another.setDeserialized(false);

        Assert.assertTrue(record.equals(another));
        Assert.assertEquals(record.hashCode(), another.hashCode());

        another = new BulletRecord();
        another.setString("foo", "bar");

        Assert.assertFalse(record.equals(another));
        // This may or may not be true. Much more likely to be true, so leaving it in
        Assert.assertNotEquals(record.hashCode(), another.hashCode());
    }

    @Test
    public void testForceReadData() {
        // If already deserialized, should succeed
        Assert.assertTrue(record.forceReadData());

        // Force a bad read with no data and not deserialized.
        record.setDeserialized(false);
        Assert.assertFalse(record.forceReadData());

        // Set the data to an invalid byte array and force a read
        record = new BulletRecord();
        record.setSerializedData("foo".getBytes());
        record.setDeserialized(false);
        Assert.assertFalse(record.forceReadData());

        // Set the data to a valid byte array and force a read
        record = new BulletRecord();
        Map<String, Object> data = new HashMap<>();
        data.put("foo", singletonMap("bar", "baz"));
        data.put("qux", singletonList(singletonMap("bar", "baz")));
        record.setSerializedData(getAvroBytes(data));
        record.setDeserialized(false);
        Assert.assertTrue(record.forceReadData());
        Assert.assertEquals(record.get("foo"), singletonMap("bar", "baz"));
        Assert.assertEquals(record.get("foo", "bar"), "baz");
        Assert.assertEquals(record.get("qux"), singletonList(singletonMap("bar", "baz")));
        Assert.assertEquals(record.get("qux", 0), singletonMap("bar", "baz"));

        // Further force reads should return true
        Assert.assertTrue(record.forceReadData());
        Assert.assertTrue(record.forceReadData());
    }

    @Test
    public void testSerializationDeserialization() {
        record.setString("1", "bar").setLong("2", 42L).setBoolean("3", false).setDouble("4", 0.34)
              .setMap("7", Pair.of("4.1", false), Pair.of("7.2", true), Pair.of("7.3", null))
              .setMap("8", Pair.of("8.1", "foo"), Pair.of("8.2", "bar"), Pair.of("8.3", "baz"))
              .setListOfLongMap("9", singletonList(singletonMap("9.1", 3L)))
              .setListOfBooleanMap("10", singletonList(singletonMap("10.1", true)));

        byte[] serialized = getRecordBytes(record);
        Assert.assertNotNull(serialized);

        BulletRecord reified = fromRecordBytes(serialized);
        Assert.assertNotNull(reified);
        Assert.assertEquals(reified.get("2"), 42L);
        Assert.assertTrue((Boolean) reified.get("7", "7.2"));
        Assert.assertNull(reified.get("7", "7.3"));
        Assert.assertEquals(reified.get("9", 0), singletonMap("9.1", 3L));
    }

    @Test
    public void testSerializationDeserializationOfEmptyRecord() {
        byte[] serialized = getRecordBytes(record);
        Assert.assertNotNull(serialized);

        BulletRecord reified = fromRecordBytes(serialized);
        Assert.assertNotNull(reified);

        byte[] serializedAgain = getRecordBytes(record);
        Assert.assertEquals(serializedAgain, serialized);
    }

    @Test
    public void testSerializationWithBadStates() {
        byte[] serialized = getRecordBytes(record);
        BulletRecord reified = fromRecordBytes(serialized);

        // This will destroy the byte array and force the record to think it has data in its map (it doesn't)
        reified.setSerializedData(null);
        reified.setDeserialized(true);

        // The record should handle this case by pretending to be an empty record
        byte[] serializedAgain = getRecordBytes(reified);
        Assert.assertEquals(serializedAgain, serialized);
    }

    @Test
    public void testDeserializationWithBadStates() {
        byte[] serialized = getRecordBytes(record);
        BulletRecord reified = fromRecordBytes(serialized);

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

        byte[] serialized = getRecordBytes(record);
        Assert.assertNotNull(serialized);
        BulletRecord reified = fromRecordBytes(serialized);

        // Read and write without accessing anything
        serialized = getRecordBytes(reified);
        reified = fromRecordBytes(serialized);

        Assert.assertNotNull(reified);
        Assert.assertEquals(reified.get("4"), 0.34);
    }

    @Test
    public void testSerializationDeserializationWithDataInput() {
        record.setString("1", "bar").setLong("2", 42L).setBoolean("3", false).setDouble("4", 0.34)
              .setMap("7", Pair.of("4.1", false), Pair.of("7.2", true), Pair.of("7.3", null))
              .setMap("8", Pair.of("8.1", "foo"), Pair.of("8.2", "bar"), Pair.of("8.3", "baz"))
              .setListOfLongMap("9", singletonList(singletonMap("9.1", 3L)))
              .setListOfBooleanMap("10", singletonList(singletonMap("10.1", true)));

        // SerDe
        byte[] serialized = getRecordBytes(record);
        BulletRecord reified = fromRecordBytes(serialized);

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
    public void testRawByteArrayIsNotBacking() throws IOException {
        record.setString("1", "bar").setLong("2", 42L).setBoolean("3", false).setDouble("4", 0.34);
        byte[] rawByteArray = record.getAsByteArray();
        record.setString("1", "foo");
        record.setString("5", "bar");
        byte[] after = record.getAsByteArray();
        Assert.assertNotEquals(after, rawByteArray);
    }

    @Test
    public void testSerializationWithMapAndRawByteArray() throws IOException {
        record.setString("1", "bar").setLong("2", 42L).setBoolean("3", false).setDouble("4", 0.34);

        // Get the raw data out
        byte[] rawDataBytes = record.getAsByteArray();
        // Create a new record from the raw data
        BulletRecord reified = new BulletRecord(rawDataBytes);
        Assert.assertTrue(record.equals(reified));
    }

    @Test
    public void testSameByteArrayPostSerialization() throws IOException {
        record.setString("1", "bar").setLong("2", 42L).setBoolean("3", false).setDouble("4", 0.34);
        byte[] originalByteArray = record.getAsByteArray();

        // Now serialize and deserialize the record
        byte[] serialized = getRecordBytes(record);
        BulletRecord reified = fromRecordBytes(serialized);

        // Get the raw data again as bytes
        byte[] rawByteArray = record.getAsByteArray();
        Assert.assertEquals(rawByteArray, originalByteArray);

        // Read something from it to force a map conversion
        Assert.assertEquals(record.get("1"), "bar");
        // Then check if it is still the same byte array
        Assert.assertEquals(record.getAsByteArray(), rawByteArray);
    }

}
