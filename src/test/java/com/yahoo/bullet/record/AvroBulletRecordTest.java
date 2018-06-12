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
public class AvroBulletRecordTest {
    private AvroBulletRecord record;

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

    public static byte[] getRecordBytes(AvroBulletRecord record) {
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

    public static AvroBulletRecord fromRecordBytes(byte[] data) {
        try {
            ObjectInputStream stream = new ObjectInputStream(new ByteArrayInputStream(data));
            return (AvroBulletRecord) stream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @BeforeMethod
    public void setup() {
        record = new AvroBulletRecord();
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
    public void testSetInteger() {
        record.setInteger("foo", 88).setInteger("bar", 51);

        Assert.assertEquals(record.get("foo"), 88);
        Assert.assertEquals(record.get("bar"), 51);
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
    public void testSetFloat() {
        record.setFloat("foo", 42.1f).setFloat("bar", -1.13f).setFloat("baz", null);

        Assert.assertEquals(record.get("foo"), 42.1f);
        Assert.assertEquals(record.get("bar"), -1.13f);
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
        Map<String, Integer> dataA = new HashMap<>();
        dataA.put("bar", 1);
        dataA.put("baz", 2);
        record.setIntegerMap("fooA", dataA);
        Assert.assertTrue(record.get("fooA") instanceof Map);
        Assert.assertEquals(record.get("fooA", "bar"), 1);
        Assert.assertEquals(record.get("fooA", "baz"), 2);

        Map<String, Long> dataB = new HashMap<>();
        dataB.put("bar", 1L);
        dataB.put("baz", 2L);
        record.setLongMap("fooA", dataB);
        Assert.assertTrue(record.get("fooA") instanceof Map);
        Assert.assertEquals(record.get("fooA", "bar"), 1L);
        Assert.assertEquals(record.get("fooA", "baz"), 2L);

        Map<String, Float> dataC = new HashMap<>();
        dataC.put("bar", 1.1f);
        dataC.put("baz", 2.2f);
        record.setFloatMap("fooA", dataC);
        Assert.assertTrue(record.get("fooA") instanceof Map);
        Assert.assertEquals(record.get("fooA", "bar"), 1.1f);
        Assert.assertEquals(record.get("fooA", "baz"), 2.2f);

        Map<String, Double> dataD = new HashMap<>();
        dataD.put("bar", 1.1);
        dataD.put("baz", 2.2);
        record.setDoubleMap("fooB", dataD);
        Assert.assertTrue(record.get("fooB") instanceof Map);
        Assert.assertEquals(record.get("fooB", "bar"), 1.1);
        Assert.assertEquals(record.get("fooB", "baz"), 2.2);

        Map<String, Boolean> dataE = new HashMap<>();
        dataE.put("bar", false);
        dataE.put("baz", true);
        record.setBooleanMap("fooC", dataE);
        Assert.assertTrue(record.get("fooC") instanceof Map);
        Assert.assertEquals(record.get("fooC", "bar"), false);
        Assert.assertEquals(record.get("fooC", "baz"), true);

        Map<String, String> dataF = new HashMap<>();
        dataF.put("bar", "foo");
        dataF.put("baz", "norf");
        record.setStringMap("fooD", dataF);
        Assert.assertTrue(record.get("fooD") instanceof Map);
        Assert.assertEquals(record.get("fooD", "bar"), "foo");
        Assert.assertEquals(record.get("fooD", "baz"), "norf");

        Assert.assertNull(record.get("dne"));
        Assert.assertNull(record.get("dne", "bar"));
        Assert.assertNull(record.get("dne", "baz"));
    }

    @Test
    public void testSetMapOfMaps() {
        Map<String, Map<String, Integer>> dataA = new HashMap<>();
        dataA.put("bar", singletonMap("a", 1));
        dataA.put("baz", singletonMap("b", 2));
        record.setMapOfIntegerMap("fooA", dataA);
        Assert.assertTrue(record.get("fooA") instanceof Map);
        Assert.assertTrue(record.get("fooA", "bar") instanceof Map);
        Assert.assertTrue(record.get("fooA", "baz") instanceof Map);
        Assert.assertEquals(record.get("fooA", "bar"), singletonMap("a", 1));
        Assert.assertEquals(record.get("fooA", "baz"), singletonMap("b", 2));

        Map<String, Map<String, Long>> dataB = new HashMap<>();
        dataB.put("bar", singletonMap("a", 1L));
        dataB.put("baz", singletonMap("b", 2L));
        record.setMapOfLongMap("fooB", dataB);
        Assert.assertTrue(record.get("fooB") instanceof Map);
        Assert.assertTrue(record.get("fooB", "bar") instanceof Map);
        Assert.assertTrue(record.get("fooB", "baz") instanceof Map);
        Assert.assertEquals(record.get("fooB", "bar"), singletonMap("a", 1L));
        Assert.assertEquals(record.get("fooB", "baz"), singletonMap("b", 2L));

        Map<String, Map<String, Float>> dataC = new HashMap<>();
        dataC.put("bar", singletonMap("a", 0.2f));
        dataC.put("baz", singletonMap("b", 0.1f));
        record.setMapOfFloatMap("fooC", dataC);
        Assert.assertTrue(record.get("fooC") instanceof Map);
        Assert.assertTrue(record.get("fooC", "bar") instanceof Map);
        Assert.assertTrue(record.get("fooC", "baz") instanceof Map);
        Assert.assertEquals(record.get("fooC", "bar"), singletonMap("a", 0.2f));
        Assert.assertEquals(record.get("fooC", "baz"), singletonMap("b", 0.1f));

        Map<String, Map<String, Double>> dataD = new HashMap<>();
        dataD.put("bar", singletonMap("a", 0.2));
        dataD.put("baz", singletonMap("b", 0.1));
        record.setMapOfDoubleMap("fooD", dataD);
        Assert.assertTrue(record.get("fooD") instanceof Map);
        Assert.assertTrue(record.get("fooD", "bar") instanceof Map);
        Assert.assertTrue(record.get("fooD", "baz") instanceof Map);
        Assert.assertEquals(record.get("fooD", "bar"), singletonMap("a", 0.2));
        Assert.assertEquals(record.get("fooD", "baz"), singletonMap("b", 0.1));

        Map<String, Map<String, Boolean>> dataE = new HashMap<>();
        dataE.put("bar", singletonMap("a", false));
        dataE.put("baz", singletonMap("b", true));
        record.setMapOfBooleanMap("fooE", dataE);
        Assert.assertTrue(record.get("fooE") instanceof Map);
        Assert.assertTrue(record.get("fooE", "bar") instanceof Map);
        Assert.assertTrue(record.get("fooE", "baz") instanceof Map);
        Assert.assertEquals(record.get("fooE", "bar"), singletonMap("a", false));
        Assert.assertEquals(record.get("fooE", "baz"), singletonMap("b", true));

        Map<String, Map<String, String>> dataF = new HashMap<>();
        dataF.put("bar", singletonMap("a", "foo"));
        dataF.put("baz", singletonMap("b", "norf"));
        record.setMapOfStringMap("fooF", dataF);
        Assert.assertTrue(record.get("fooF") instanceof Map);
        Assert.assertTrue(record.get("fooF", "bar") instanceof Map);
        Assert.assertTrue(record.get("fooF", "baz") instanceof Map);
        Assert.assertEquals(record.get("fooF", "bar"), singletonMap("a", "foo"));
        Assert.assertEquals(record.get("fooF", "baz"), singletonMap("b", "norf"));

        Assert.assertNull(record.get("dne"));
        Assert.assertNull(record.get("dne", "bar"));
        Assert.assertNull(record.get("dne", "baz"));
    }

    @Test
    public void testSetListMaps() {
        List<Map<String, Integer>> dataA = new ArrayList<>();
        dataA.add(singletonMap("a", 1));
        dataA.add(singletonMap("b", 2));
        record.setListOfIntegerMap("fooA", dataA);
        Assert.assertTrue(record.get("fooA") instanceof List);
        Assert.assertTrue(record.get("fooA", 0) instanceof Map);
        Assert.assertTrue(record.get("fooA", 1) instanceof Map);
        Assert.assertEquals(record.get("fooA", 0), singletonMap("a", 1));
        Assert.assertEquals(record.get("fooA", 1), singletonMap("b", 2));

        List<Map<String, Long>> dataB = new ArrayList<>();
        dataB.add(singletonMap("a", 1L));
        dataB.add(singletonMap("b", 2L));
        record.setListOfLongMap("fooB", dataB);
        Assert.assertTrue(record.get("fooB") instanceof List);
        Assert.assertTrue(record.get("fooB", 0) instanceof Map);
        Assert.assertTrue(record.get("fooB", 1) instanceof Map);
        Assert.assertEquals(record.get("fooB", 0), singletonMap("a", 1L));
        Assert.assertEquals(record.get("fooB", 1), singletonMap("b", 2L));

        List<Map<String, Float>> dataC = new ArrayList<>();
        dataC.add(singletonMap("a", 0.2f));
        dataC.add(singletonMap("b", 0.1f));
        record.setListOfFloatMap("fooC", dataC);
        Assert.assertTrue(record.get("fooC") instanceof List);
        Assert.assertTrue(record.get("fooC", 0) instanceof Map);
        Assert.assertTrue(record.get("fooC", 1) instanceof Map);
        Assert.assertEquals(record.get("fooC", 0), singletonMap("a", 0.2f));
        Assert.assertEquals(record.get("fooC", 1), singletonMap("b", 0.1f));

        List<Map<String, Double>> dataD = new ArrayList<>();
        dataD.add(singletonMap("a", 0.2));
        dataD.add(singletonMap("b", 0.1));
        record.setListOfDoubleMap("fooD", dataD);
        Assert.assertTrue(record.get("fooD") instanceof List);
        Assert.assertTrue(record.get("fooD", 0) instanceof Map);
        Assert.assertTrue(record.get("fooD", 1) instanceof Map);
        Assert.assertEquals(record.get("fooD", 0), singletonMap("a", 0.2));
        Assert.assertEquals(record.get("fooD", 1), singletonMap("b", 0.1));

        List<Map<String, Boolean>> dataE = new ArrayList<>();
        dataE.add(singletonMap("a", false));
        dataE.add(singletonMap("b", true));
        record.setListOfBooleanMap("fooE", dataE);
        Assert.assertTrue(record.get("fooE") instanceof List);
        Assert.assertTrue(record.get("fooE", 0) instanceof Map);
        Assert.assertTrue(record.get("fooE", 1) instanceof Map);
        Assert.assertEquals(record.get("fooE", 0), singletonMap("a", false));
        Assert.assertEquals(record.get("fooE", 1), singletonMap("b", true));

        List<Map<String, String>> dataF = new ArrayList<>();
        dataF.add(singletonMap("a", "foo"));
        dataF.add(singletonMap("b", "norf"));
        record.setListOfStringMap("fooF", dataF);
        Assert.assertTrue(record.get("fooF") instanceof List);
        Assert.assertTrue(record.get("fooF", 0) instanceof Map);
        Assert.assertTrue(record.get("fooF", 1) instanceof Map);
        Assert.assertEquals(record.get("fooF", 0), singletonMap("a", "foo"));
        Assert.assertEquals(record.get("fooF", 1), singletonMap("b", "norf"));

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

        AvroBulletRecord newRecord = new AvroBulletRecord();

        newRecord.set("newA", record, "a");
        Assert.assertEquals(newRecord.get("newA"), "foo");
        Assert.assertEquals(newRecord.toString(), "{newA:foo}");

        newRecord.set("newB", record, "b", "b.1");
        Assert.assertEquals(newRecord.get("newB"), "bar");

        newRecord.set("newC", record, "c", 0);
        Assert.assertEquals(newRecord.get("newC"), singletonMap("c.1", false));

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
        record.setMap("4", Pair.of("4.1", false))
              .setString("1", "bar").setLong("2", 42L)
              .setBoolean("3", false)
              .setInteger("7", 88)
              .setListOfDoubleMap("5", singletonList(singletonMap("5.1", 3.1)))
              .setListOfFloatMap("6", singletonList(singletonMap("8.8", 8.8f)));
        Map<String, Object> expectedMap = new HashMap<>();
        expectedMap.put("1", "bar");
        expectedMap.put("2", 42L);
        expectedMap.put("3", false);
        expectedMap.put("4", singletonMap("4.1", false));
        expectedMap.put("5", singletonList(singletonMap("5.1", 3.1)));
        expectedMap.put("6", singletonList(singletonMap("8.8", 8.8f)));
        expectedMap.put("7", 88);

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
        record.setString("1", "bar");
        Assert.assertEquals(record.toString(), "{1:bar}");
    }

    @Test
    public void testHashcodeEdgeCases() {
        Assert.assertEquals(record.hashCode(), new AvroBulletRecord().hashCode());

        record.setSerializedData(null);
        record.setData(null);
        AvroBulletRecord another = new AvroBulletRecord();
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
        AvroBulletRecord another = (AvroBulletRecord) new AvroBulletRecord().setString("1", "bar");
        Assert.assertFalse(record.equals(another));
    }

    @Test
    public void testEqualsAndHashcodeByteArrays() {
        record.setMap("4", Pair.of("4.1", false), Pair.of("4.2", true), Pair.of("4.3", null))
              .setString("1", "bar").setLong("2", 42L).setBoolean("3", false)
              .setListOfStringMap("5", singletonList(singletonMap("5.1", "foo")));
        AvroBulletRecord another = new AvroBulletRecord();
        another.setMap("4", Pair.of("4.1", false), Pair.of("4.2", true), Pair.of("4.3", null))
               .setString("1", "bar").setLong("2", 42L).setBoolean("3", false)
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
        record.setMap("4", Pair.of("4.1", false), Pair.of("4.2", true), Pair.of("4.3", null))
              .setString("1", "bar").setLong("2", 42L).setBoolean("3", false)
              .setListOfStringMap("5", singletonList(singletonMap("5.1", "foo")));
        AvroBulletRecord another = new AvroBulletRecord();
        another.setMap("4", Pair.of("4.1", false), Pair.of("4.2", true), Pair.of("4.3", null))
               .setString("1", "bar").setLong("2", 42L).setBoolean("3", false)
               .setListOfStringMap("5", singletonList(singletonMap("5.1", "foo")));

        Assert.assertTrue(record.equals(another));
        Assert.assertEquals(record.hashCode(), another.hashCode());

        // Change order and it should still pass
        another = new AvroBulletRecord();
        another.setMap("4", Pair.of("4.1", false), Pair.of("4.2", true), Pair.of("4.3", null))
               .setString("1", "bar").setLong("2", 42L).setBoolean("3", false)
               .setListOfStringMap("5", singletonList(singletonMap("5.1", "foo")));
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

        AvroBulletRecord another = new AvroBulletRecord();
        another.setSerializedData(null);
        another.setData(null);
        another.setDeserialized(false);

        Assert.assertTrue(record.equals(another));
        Assert.assertEquals(record.hashCode(), another.hashCode());

        another = new AvroBulletRecord();
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
        record = new AvroBulletRecord();
        record.setSerializedData("foo".getBytes());
        record.setDeserialized(false);
        Assert.assertFalse(record.forceReadData());

        // Set the data to a valid byte array and force a read
        record = new AvroBulletRecord();
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
        record.setMap("7", Pair.of("4.1", false), Pair.of("7.2", true), Pair.of("7.3", null))
              .setMap("8", Pair.of("8.1", "foo"), Pair.of("8.2", "bar"), Pair.of("8.3", "baz"))
              .setString("1", "bar").setLong("2", 42L).setBoolean("3", false).setDouble("4", 0.34)
              .setListOfLongMap("9", singletonList(singletonMap("9.1", 3L)))
              .setListOfBooleanMap("10", singletonList(singletonMap("10.1", true)));

        byte[] serialized = getRecordBytes(record);
        Assert.assertNotNull(serialized);

        AvroBulletRecord reified = fromRecordBytes(serialized);
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

        AvroBulletRecord reified = fromRecordBytes(serialized);
        Assert.assertNotNull(reified);

        byte[] serializedAgain = getRecordBytes(record);
        Assert.assertEquals(serializedAgain, serialized);
    }

    @Test
    public void testSerializationWithBadStates() {
        byte[] serialized = getRecordBytes(record);
        AvroBulletRecord reified = fromRecordBytes(serialized);

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
        AvroBulletRecord reified = fromRecordBytes(serialized);

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
        AvroBulletRecord reified = fromRecordBytes(serialized);

        // Read and write without accessing anything
        serialized = getRecordBytes(reified);
        reified = fromRecordBytes(serialized);

        Assert.assertNotNull(reified);
        Assert.assertEquals(reified.get("4"), 0.34);
    }

    @Test
    public void testSerializationDeserializationWithDataInput() {
        record.setMap("7", Pair.of("4.1", false), Pair.of("7.2", true), Pair.of("7.3", null))
              .setMap("8", Pair.of("8.1", "foo"), Pair.of("8.2", "bar"), Pair.of("8.3", "baz"))
              .setString("1", "bar").setLong("2", 42L).setBoolean("3", false).setDouble("4", 0.34)
              .setListOfLongMap("9", singletonList(singletonMap("9.1", 3L)))
              .setListOfBooleanMap("10", singletonList(singletonMap("10.1", true)));

        // SerDe
        byte[] serialized = getRecordBytes(record);
        AvroBulletRecord reified = fromRecordBytes(serialized);

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
    public void testRenaming() {
        record.setMap("7", Pair.of("4.1", false), Pair.of("7.2", true))
              .setString("1", "bar").setLong("2", 42L).setBoolean("3", false).setDouble("4", 0.34)
              .setListOfLongMap("9", singletonList(singletonMap("9.1", 3L)));

        record.rename("1", "new1").rename("3", "new3").rename("7.4.1", "new2");

        BulletRecord expected = new AvroBulletRecord().setMap("7", Pair.of("4.1", false), Pair.of("7.2", true))
                                                  .setString("new1", "bar").setLong("2", 42L).setBoolean("new3", false)
                                                  .setDouble("4", 0.34)
                                                  .setListOfLongMap("9", singletonList(singletonMap("9.1", 3L)));

        Assert.assertTrue(expected.equals(record));
    }

    @Test
    public void testFieldCount() throws IOException {
        Assert.assertEquals(record.fieldCount(), 0);

        record.setString("foo", "bar");
        Assert.assertEquals(record.fieldCount(), 1);

        record.setMap("7", Pair.of("4.1", false), Pair.of("7.2", true));
        Assert.assertEquals(record.fieldCount(), 2);

        record.remove("2");
        record.remove("7");
        Assert.assertEquals(record.fieldCount(), 1);

        AvroBulletRecord another = new AvroBulletRecord(record);
        Assert.assertEquals(another.fieldCount(), 1);
    }

    @Test
    public void testRemoving() {
        record.setMap("7", Pair.of("4.1", false), Pair.of("7.2", true))
              .setString("1", "bar").setLong("2", 42L).setBoolean("3", false).setDouble("4", 0.34)
              .setListOfLongMap("9", singletonList(singletonMap("9.1", 3L)));

        record.remove("1").remove("3").remove("7.4.1").remove("9");

        BulletRecord expected = new AvroBulletRecord().setMap("7", Pair.of("4.1", false), Pair.of("7.2", true))
                                                          .setLong("2", 42L).setDouble("4", 0.34);
        Assert.assertTrue(expected.equals(record));
    }

    @Test
    public void testRemovingField() {
        record.setMap("7", Pair.of("4.1", false), Pair.of("7.2", true))
              .setString("1", "bar").setLong("2", 42L).setBoolean("3", false).setDouble("4", 0.34)
              .setListOfLongMap("9", singletonList(singletonMap("9.1", 3L)));

        Object data;
        data = record.getAndRemove("1");
        Assert.assertTrue(data instanceof String);
        Assert.assertEquals(data, "bar");

        data = record.getAndRemove("3");
        Assert.assertTrue(data instanceof Boolean);
        Assert.assertEquals(data, false);

        data = record.getAndRemove("7.7.2");
        Assert.assertNull(data);
    }

    @Test
    public void testFieldPresence() {
        record.setMap("7", Pair.of("4.1", false), Pair.of("7.2", true))
              .setString("1", "bar").setLong("2", 42L).setBoolean("3", false).setDouble("4", 0.34);

        Assert.assertTrue(record.hasField("1"));
        Assert.assertTrue(record.hasField("7"));
        Assert.assertFalse(record.hasField("7.4.1"));
        Assert.assertFalse(record.hasField("foo"));
    }

    @Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = "Cannot read from record.*")
    public void testFailingWhenCannotRead() {
        record = new AvroBulletRecord();
        record.setSerializedData("foo".getBytes());
        record.setDeserialized(false);
        record.hasField("foo");
    }

    @Test
    public void testNoArgsConstructor() {
        AvroBulletRecord record = new AvroBulletRecord();
        Assert.assertTrue(record.isDeserialized);
        Assert.assertNotNull(record.data);
    }

    @Test
    public void testCopyConstructor() throws Exception {
        AvroBulletRecord record = new AvroBulletRecord();
        record.set("someField", "someValue");
        AvroBulletRecord copy = new AvroBulletRecord(record);
        Assert.assertEquals(copy.get("someField"), "someValue");
        Assert.assertEquals(copy.fieldCount(), 1);
    }

    @Test
    public void testCopyOfCopy() throws Exception {
        AvroBulletRecord record = new AvroBulletRecord();
        record.set("someField", "someValue");
        AvroBulletRecord copy = new AvroBulletRecord(record);
        AvroBulletRecord copyOfCopy = new AvroBulletRecord(copy);
        Assert.assertEquals(copyOfCopy.get("someField"), "someValue");
        Assert.assertEquals(copyOfCopy.fieldCount(), 1);
    }
}
