/*
 *  Copyright 2016, Yahoo Inc.
 *  Licensed under the terms of the Apache License, Version 2.0.
 *  See the LICENSE file associated with the project for terms.
 */
package com.yahoo.bullet.record;

import org.apache.commons.lang3.tuple.Pair;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;

@SuppressWarnings("unchecked")
public class SimpleBulletRecordTest {

    @Test
    public void testSetBoolean() {
        SimpleBulletRecord record = new SimpleBulletRecord();
        record.setBoolean("foo", false).setBoolean("bar", true).setBoolean("baz", null);

        Assert.assertTrue((Boolean) record.get("bar"));
        Assert.assertFalse((Boolean) record.get("foo"));
        Assert.assertNull(record.get("baz"));
        Assert.assertNull(record.get("dne"));
    }

    @Test
    public void testSetString() {
        SimpleBulletRecord record = new SimpleBulletRecord();
        record.setString("foo", "bar").setString("bar", "baz");

        Assert.assertEquals(record.get("foo"), "bar");
        Assert.assertEquals(record.get("bar"), "baz");
        Assert.assertNull(record.get("dne"));
    }

    @Test
    public void testSetInteger() {
        SimpleBulletRecord record = new SimpleBulletRecord();
        record.setInteger("foo", 88).setInteger("bar", 51);

        Assert.assertEquals(record.get("foo"), 88);
        Assert.assertEquals(record.get("bar"), 51);
        Assert.assertNull(record.get("dne"));
    }

    @Test
    public void testSetLong() {
        SimpleBulletRecord record = new SimpleBulletRecord();
        record.setLong("foo", 0L).setLong("bar", -1231231231231231231L).setLong("baz", null);

        Assert.assertEquals(record.get("foo"), 0L);
        Assert.assertEquals(record.get("bar"), -1231231231231231231L);
        Assert.assertNull(record.get("baz"));
        Assert.assertNull(record.get("dne"));
    }

    @Test
    public void testSetFloat() {
        SimpleBulletRecord record = new SimpleBulletRecord();
        record.setFloat("foo", 42.1f).setFloat("bar", -1.13f).setFloat("baz", null);

        Assert.assertEquals(record.get("foo"), 42.1f);
        Assert.assertEquals(record.get("bar"), -1.13f);
        Assert.assertNull(record.get("baz"));
        Assert.assertNull(record.get("dne"));
    }

    @Test
    public void testSetDouble() {
        SimpleBulletRecord record = new SimpleBulletRecord();
        record.setDouble("foo", 42.1).setDouble("bar", -1.13).setDouble("baz", null);

        Assert.assertEquals(record.get("foo"), 42.1);
        Assert.assertEquals(record.get("bar"), -1.13);
        Assert.assertNull(record.get("baz"));
        Assert.assertNull(record.get("dne"));
    }

    @Test
    public void testSetMaps() {
        SimpleBulletRecord record = new SimpleBulletRecord();
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
    public void testSetLists() {
        SimpleBulletRecord record = new SimpleBulletRecord();
        List<Integer> dataA = new ArrayList<>();
        dataA.add(1);
        dataA.add(2);
        record.setIntegerList("fooA", dataA);
        Assert.assertTrue(record.get("fooA") instanceof List);
        Assert.assertEquals(record.get("fooA", 0), 1);
        Assert.assertEquals(record.get("fooA", 1), 2);

        List<Long> dataB = new ArrayList<>();
        dataB.add(1L);
        dataB.add(2L);
        record.setLongList("fooA", dataB);
        Assert.assertTrue(record.get("fooA") instanceof List);
        Assert.assertEquals(record.get("fooA", 0), 1L);
        Assert.assertEquals(record.get("fooA", 1), 2L);

        List<Float> dataC = new ArrayList<>();
        dataC.add(1.1f);
        dataC.add(2.2f);
        record.setFloatList("fooA", dataC);
        Assert.assertTrue(record.get("fooA") instanceof List);
        Assert.assertEquals(record.get("fooA", 0), 1.1f);
        Assert.assertEquals(record.get("fooA", 1), 2.2f);

        List<Double> dataD = new ArrayList<>();
        dataD.add(1.1);
        dataD.add(2.2);
        record.setDoubleList("fooB", dataD);
        Assert.assertTrue(record.get("fooB") instanceof List);
        Assert.assertEquals(record.get("fooB", 0), 1.1);
        Assert.assertEquals(record.get("fooB", 1), 2.2);

        List<Boolean> dataE = new ArrayList<>();
        dataE.add(false);
        dataE.add(true);
        record.setBooleanList("fooC", dataE);
        Assert.assertTrue(record.get("fooC") instanceof List);
        Assert.assertEquals(record.get("fooC", 0), false);
        Assert.assertEquals(record.get("fooC", 1), true);

        List<String> dataF = new ArrayList<>();
        dataF.add("foo");
        dataF.add("norf");
        record.setStringList("fooD", dataF);
        Assert.assertTrue(record.get("fooD") instanceof List);
        Assert.assertEquals(record.get("fooD", 0), "foo");
        Assert.assertEquals(record.get("fooD", 1), "norf");

        Assert.assertNull(record.get("dne"));
        Assert.assertNull(record.get("dne", 0));
        Assert.assertNull(record.get("dne", 1));
    }

    @Test
    public void testSetMapOfMaps() {
        SimpleBulletRecord record = new SimpleBulletRecord();
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
        SimpleBulletRecord record = new SimpleBulletRecord();
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
        SimpleBulletRecord record = new SimpleBulletRecord();
        record.setString("a", "foo");
        record.setStringMap("b", singletonMap("b.1", "bar"));
        Map<String, Boolean> data = new HashMap<>();
        data.put("c.1", false);
        record.setListOfBooleanMap("c", singletonList(data));
        record.setStringList("d", singletonList("norf"));

        SimpleBulletRecord newRecord = new SimpleBulletRecord();

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

        newRecord.set("newD", record, "d", 0);
        Assert.assertEquals(newRecord.get("newD"), "norf");
    }

    @Test
    public void testGetTopLevelField() {
        SimpleBulletRecord record = new SimpleBulletRecord();
        record.setListOfStringMap("foo", singletonList(singletonMap("qux", "norf")));
        record.setString("bar", null);

        Assert.assertNull(record.get("bar"));
        Assert.assertTrue(record.get("foo") instanceof List);
        Assert.assertEquals(record.get("foo"), singletonList(singletonMap("qux", "norf")));
        Assert.assertNull(record.get("dne"));
    }

    @Test
    public void testGetMapField() {
        SimpleBulletRecord record = new SimpleBulletRecord();
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
        SimpleBulletRecord record = new SimpleBulletRecord();
        record.setListMap("foo", singletonMap("a", 1L), singletonMap("b", 2L));
        List<String> data = new ArrayList<>();
        data.add("qux");
        data.add("norf");
        record.setStringList("bar", data);
        Assert.assertTrue(record.get("foo") instanceof List);
        Assert.assertEquals(record.get("foo", 0), singletonMap("a", 1L));
        Assert.assertEquals(record.get("foo", 1), singletonMap("b", 2L));
        Assert.assertTrue(record.get("bar") instanceof List);
        Assert.assertEquals(record.get("bar", 0), "qux");
        Assert.assertEquals(record.get("bar", 1), "norf");

        Assert.assertNull(record.get("dne", -1));
        Assert.assertNull(record.get("dne", 0));
        Assert.assertNull(record.get("dne", 123123));
    }

    @Test(expectedExceptions = IndexOutOfBoundsException.class)
    public void testGetListBadIndex() {
        SimpleBulletRecord record = new SimpleBulletRecord();
        record.setListMap("foo", singletonMap("a", 1L));
        record.get("foo", 1);
    }

    @Test
    public void testIterator() {
        SimpleBulletRecord record = new SimpleBulletRecord();
        record.setMap("4", Pair.of("4.1", false))
              .setString("1", "bar").setLong("2", 42L)
              .setBoolean("3", false)
              .setInteger("7", 88)
              .setListOfDoubleMap("5", singletonList(singletonMap("5.1", 3.1)))
              .setListOfFloatMap("6", singletonList(singletonMap("8.8", 8.8f)))
              .setStringList("8", singletonList("foo"));
        Map<String, Object> expectedMap = new HashMap<>();
        expectedMap.put("1", "bar");
        expectedMap.put("2", 42L);
        expectedMap.put("3", false);
        expectedMap.put("4", singletonMap("4.1", false));
        expectedMap.put("5", singletonList(singletonMap("5.1", 3.1)));
        expectedMap.put("6", singletonList(singletonMap("8.8", 8.8f)));
        expectedMap.put("7", 88);
        expectedMap.put("8", singletonList("foo"));

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
        SimpleBulletRecord record = new SimpleBulletRecord();
        Assert.assertEquals(record.toString(), "{}");
        record.setString("1", "bar");
        Assert.assertEquals(record.toString(), "{1:bar}");
    }

    @Test
    public void testHashcodeEdgeCases() {
        SimpleBulletRecord record = new SimpleBulletRecord();
        Assert.assertEquals(record.hashCode(), new SimpleBulletRecord().hashCode());

        record.setData(null);
        SimpleBulletRecord another = new SimpleBulletRecord();
        another.setData(null);
        Assert.assertEquals(record.hashCode(), another.hashCode());
    }

    @Test
    public void testEqualsEdgeCases() {
        SimpleBulletRecord record = new SimpleBulletRecord();
        record.setString("1", "bar").setLong("2", 42L).setBoolean("3", false);
        Assert.assertFalse(record.equals(null));
        Assert.assertFalse(record.equals("foo"));
    }

    @Test
    public void testEqualsDifferentRecord() {
        SimpleBulletRecord record = new SimpleBulletRecord();
        record.setString("1", "bar").setLong("2", 42L).setBoolean("3", false);
        SimpleBulletRecord another = (SimpleBulletRecord) new SimpleBulletRecord().setString("1", "bar");
        Assert.assertFalse(record.equals(another));
    }

    @Test
    public void testEqualsAndHashcodeByteArrays() {
        SimpleBulletRecord record = new SimpleBulletRecord();
        record.setMap("4", Pair.of("4.1", false), Pair.of("4.2", true), Pair.of("4.3", null))
              .setString("1", "bar").setLong("2", 42L).setBoolean("3", false)
              .setListOfStringMap("5", singletonList(singletonMap("5.1", "foo")));
        SimpleBulletRecord another = new SimpleBulletRecord();
        another.setMap("4", Pair.of("4.1", false), Pair.of("4.2", true), Pair.of("4.3", null))
               .setString("1", "bar").setLong("2", 42L).setBoolean("3", false)
               .setListOfStringMap("5", singletonList(singletonMap("5.1", "foo")));
    }

    @Test
    public void testEqualsAndHashcodeSameRecord() {
        SimpleBulletRecord record = new SimpleBulletRecord();
        record.setMap("4", Pair.of("4.1", false), Pair.of("4.2", true), Pair.of("4.3", null))
              .setString("1", "bar").setLong("2", 42L).setBoolean("3", false)
              .setListOfStringMap("5", singletonList(singletonMap("5.1", "foo")))
              .setStringList("6", singletonList("baz"));
        SimpleBulletRecord another = new SimpleBulletRecord();
        another.setMap("4", Pair.of("4.1", false), Pair.of("4.2", true), Pair.of("4.3", null))
               .setString("1", "bar").setLong("2", 42L).setBoolean("3", false)
               .setListOfStringMap("5", singletonList(singletonMap("5.1", "foo")))
               .setStringList("6", singletonList("baz"));

        Assert.assertTrue(record.equals(another));
        Assert.assertEquals(record.hashCode(), another.hashCode());

        // Change order and it should still pass
        another = new SimpleBulletRecord();
        another.setMap("4", Pair.of("4.1", false), Pair.of("4.2", true), Pair.of("4.3", null))
               .setString("1", "bar").setLong("2", 42L).setBoolean("3", false)
               .setStringList("6", singletonList("baz"))
               .setListOfStringMap("5", singletonList(singletonMap("5.1", "foo")));
        Assert.assertTrue(record.equals(another));
        Assert.assertEquals(record.hashCode(), another.hashCode());
    }

    @Test
    public void testEqualsAndHashcodeCorruptedRecord() {
        SimpleBulletRecord record = new SimpleBulletRecord();
        record.setData(null);

        SimpleBulletRecord another = new SimpleBulletRecord();
        another.setData(null);

        Assert.assertTrue(record.equals(another));
        Assert.assertEquals(record.hashCode(), another.hashCode());

        another = new SimpleBulletRecord();
        another.setString("foo", "bar");

        Assert.assertFalse(record.equals(another));
        // This may or may not be true. Much more likely to be true, so leaving it in
        Assert.assertNotEquals(record.hashCode(), another.hashCode());
    }

    @Test
    public void testRenaming() {
        SimpleBulletRecord record = new SimpleBulletRecord();
        record.setMap("7", Pair.of("4.1", false), Pair.of("7.2", true))
              .setString("1", "bar").setLong("2", 42L).setBoolean("3", false).setDouble("4", 0.34)
              .setListOfLongMap("9", singletonList(singletonMap("9.1", 3L)))
              .setLongList("11", singletonList(4L));

        record.rename("1", "new1").rename("3", "new3").rename("7.4.1", "new2");

        BulletRecord expected = new SimpleBulletRecord().setMap("7", Pair.of("4.1", false), Pair.of("7.2", true))
                                                        .setString("new1", "bar").setLong("2", 42L).setBoolean("new3", false)
                                                        .setDouble("4", 0.34)
                                                        .setListOfLongMap("9", singletonList(singletonMap("9.1", 3L)))
                                                        .setLongList("11", singletonList(4L));

        Assert.assertTrue(expected.equals(record));
    }

    @Test
    public void testFieldCount() throws IOException {
        SimpleBulletRecord record = new SimpleBulletRecord();
        Assert.assertEquals(record.fieldCount(), 0);

        record.setString("foo", "bar");
        Assert.assertEquals(record.fieldCount(), 1);

        record.setMap("7", Pair.of("4.1", false), Pair.of("7.2", true));
        Assert.assertEquals(record.fieldCount(), 2);

        record.remove("2");
        record.remove("7");
        Assert.assertEquals(record.fieldCount(), 1);
    }

    @Test
    public void testRemoving() {
        SimpleBulletRecord record = new SimpleBulletRecord();
        record.setMap("7", Pair.of("4.1", false), Pair.of("7.2", true))
              .setString("1", "bar").setLong("2", 42L).setBoolean("3", false).setDouble("4", 0.34)
              .setListOfLongMap("9", singletonList(singletonMap("9.1", 3L)))
              .setLongList("11", singletonList(4L));

        record.remove("1").remove("3").remove("7.4.1").remove("9").remove("11");

        BulletRecord expected = new SimpleBulletRecord().setMap("7", Pair.of("4.1", false), Pair.of("7.2", true))
                                                        .setLong("2", 42L).setDouble("4", 0.34);
        Assert.assertTrue(expected.equals(record));
    }

    @Test
    public void testRemovingField() {
        SimpleBulletRecord record = new SimpleBulletRecord();
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
        SimpleBulletRecord record = new SimpleBulletRecord();
        record.setMap("7", Pair.of("4.1", false), Pair.of("7.2", true))
              .setString("1", "bar").setLong("2", 42L).setBoolean("3", false).setDouble("4", 0.34);

        Assert.assertTrue(record.hasField("1"));
        Assert.assertTrue(record.hasField("7"));
        Assert.assertFalse(record.hasField("7.4.1"));
        Assert.assertFalse(record.hasField("foo"));
    }
}
