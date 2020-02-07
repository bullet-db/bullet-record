/*
 *  Copyright 2018, Oath Inc.
 *  Licensed under the terms of the Apache License, Version 2.0.
 *  See the LICENSE file associated with the project for terms.
 */
package com.yahoo.bullet.record;

import com.yahoo.bullet.typesystem.Type;
import com.yahoo.bullet.typesystem.TypedObject;
import org.apache.commons.lang3.tuple.Pair;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;

/**
 * Each instance of {@link BulletRecord} can extend this test to get a bunch of common tests for the various operations
 * in a record. Implement {@link #revert(Object)} to convert the data from the particular record to a regular Object.
 *
 * @param <T> The type of the object stored in the record.
 */
@SuppressWarnings("unchecked")
public abstract class BulletRecordTest<T> {
    private static final String KEY_DELIMITER = "\\.";

    protected BulletRecord<T> record;
    protected BulletRecord<T> another;

    /**
     * A helper to get a field from the record by a custom identifier format.
     * <br>
     * For example, suppose a record has a map of boolean maps called "aaa". Then <br>
     * - "aaa" identifies that map of maps <br>
     * - "aaa.bbb" identifies the inner map that "aaa" maps "bbb" to (if it exists) <br>
     * - "aaa.bbb.ccc" identifies the boolean that "aaa.bbb" (if it exists) maps "ccc" to (if it exists) <br>
     * <br>
     * For a list element, the index is the key, e.g. "my_list.0" or "my_list.0.some_key"
     *
     * @param record The record to extract the field from.
     * @param identifier The non-null identifier of the field to get.
     * @return The value of the field or null if it does not exist.
     */
    @SuppressWarnings("unchecked")
    public static <T> T extractField(BulletRecord<T> record, String identifier) {
        try {
            String[] keys = identifier.split(KEY_DELIMITER, 3);
            T first = record.get(keys[0]);
            if (keys.length == 1) {
                return first;
            }
            T second;
            if (first instanceof Map) {
                second = ((Map<String, T>) first).get(keys[1]);
            } else if (first instanceof List) {
                second = ((List<T>) first).get(Integer.parseInt(keys[1]));
            } else {
                return null;
            }
            if (keys.length == 2) {
                return second;
            }
            return ((Map<String, T>) second).get(keys[2]);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Insert a primitive map field with values as Pairs or Map.Entry. The value of the entries must be in
     * {@link Type#PRIMITIVES}.
     *
     * @param <T> The type of the object in the record.
     * @param record The record to add the field to.
     * @param field The non-null name of the field.
     * @param entries The non-null entries to insert.
     * @return This object for chaining.
     */
    public static <T> BulletRecord<T> setMap(BulletRecord<T> record, String field, Map.Entry<String, Object>... entries) {
        Objects.requireNonNull(entries);
        Map<String, Object> newMap = new HashMap<>(entries.length);
        for (Map.Entry<String, Object> entry : entries) {
            newMap.put(entry.getKey(), entry.getValue());
        }
        return record.set(field, newMap);
    }

    /**
     * Insert a complex list field with values as Pairs or Map.Entry of maps. The value of the maps must be in
     * {@link Type#PRIMITIVES}.
     *
     * @param <T> The type of the object in the record.
     * @param record The record to add the field to.
     * @param field The non-null name of the field.
     * @param entries The non-null entries to insert.
     * @return This object for chaining.
     */
    public static <T> BulletRecord<T> setListMap(BulletRecord<T> record, String field, Map<String, Object>... entries) {
        Objects.requireNonNull(entries);
        List<Map<String, Object>> data = new ArrayList<>();
        for (Map<String, Object> entry : entries) {
            data.add(entry);
        }
        return record.set(field, data);
    }

    public static void assertTypedEquals(TypedObject actual, TypedObject object) {
        assertTypedEquals(actual, object.getType(), object.getValue());
    }

    public static void assertTypedEquals(TypedObject actual, Type expectedType, Object expectedValue) {
        Assert.assertNotNull(actual, "Actual typed data from the record was null");
        Type actualType = actual.getType();
        Object actualValue = actual.getValue();
        Assert.assertEquals(actualType, expectedType,
                            "The type did not match. Actual: " + actualType + " Expected: " + expectedType);
        Assert.assertEquals(actualValue, expectedValue,
                            "The value did not match. Actual: " + actualValue + " Expected: " + expectedValue);
    }

    /**
     * Override this to convert from the type of the object in the record to an Object.
     *
     * @param data The data in the record.
     * @return The data converted to an Object.
     */
    protected abstract Object revert(T data);

    @Test
    public void testSetBoolean() {
        record.setBoolean("foo", false).setBoolean("bar", true).setBoolean("baz", null);

        Assert.assertTrue((Boolean) revert(record.get("bar")));
        Assert.assertFalse((Boolean) revert(record.get("foo")));
        Assert.assertNull(record.get("baz"));
        Assert.assertNull(record.get("dne"));
    }

    @Test
    public void testSetString() {
        record.setString("foo", "bar").setString("bar", "baz");

        Assert.assertEquals(revert(record.get("foo")), "bar");
        Assert.assertEquals(revert(record.get("bar")), "baz");
        Assert.assertNull(record.get("dne"));
    }

    @Test
    public void testSetInteger() {
        record.setInteger("foo", 88).setInteger("bar", 51);

        Assert.assertEquals(revert(record.get("foo")), 88);
        Assert.assertEquals(revert(record.get("bar")), 51);
        Assert.assertNull(record.get("dne"));
    }

    @Test
    public void testSetLong() {
        record.setLong("foo", 0L).setLong("bar", -1231231231231231231L).setLong("baz", null);

        Assert.assertEquals(revert(record.get("foo")), 0L);
        Assert.assertEquals(revert(record.get("bar")), -1231231231231231231L);
        Assert.assertNull(record.get("baz"));
        Assert.assertNull(record.get("dne"));
    }

    @Test
    public void testSetFloat() {
        record.setFloat("foo", 42.1f).setFloat("bar", -1.13f).setFloat("baz", null);

        Assert.assertEquals(revert(record.get("foo")), 42.1f);
        Assert.assertEquals(revert(record.get("bar")), -1.13f);
        Assert.assertNull(record.get("baz"));
        Assert.assertNull(record.get("dne"));
    }

    @Test
    public void testSetDouble() {
        record.setDouble("foo", 42.1).setDouble("bar", -1.13).setDouble("baz", null);

        Assert.assertEquals(revert(record.get("foo")), 42.1);
        Assert.assertEquals(revert(record.get("bar")), -1.13);
        Assert.assertNull(record.get("baz"));
        Assert.assertNull(record.get("dne"));
    }

    @Test
    public void testSetAndGettingIntegerMaps() {
        Map<String, Integer> data = new HashMap<>();
        data.put("bar", 1);
        data.put("baz", 2);
        record.setIntegerMap("foo", data);

        Object object = revert(record.get("foo"));
        Assert.assertTrue(object instanceof Map);
        Map<String, Integer> actual = (Map<String, Integer>) object;
        Assert.assertEquals(actual.size(), 2);
        Assert.assertEquals(actual.get("bar"), (Integer) 1);
        Assert.assertEquals(actual.get("baz"), (Integer) 2);
        assertTypedEquals(record.typedGet("foo"), Type.INTEGER_MAP, data);
        assertTypedEquals(record.typedGet("foo", "bar"), Type.INTEGER, 1);
        assertTypedEquals(record.typedGet("foo", "baz"), Type.INTEGER, 2);
    }

    @Test
    public void testSetAndGettingLongMaps() {
        Map<String, Long> data = new HashMap<>();
        data.put("bar", 1L);
        data.put("baz", 2L);
        record.setLongMap("foo", data);

        Object object = revert(record.get("foo"));
        Assert.assertTrue(object instanceof Map);
        Map<String, Long> actual = (Map<String, Long>) object;
        Assert.assertEquals(actual.size(), 2);
        Assert.assertEquals(actual.get("bar"), (Long) 1L);
        Assert.assertEquals(actual.get("baz"), (Long) 2L);
        assertTypedEquals(record.typedGet("foo"), Type.LONG_MAP, data);
        assertTypedEquals(record.typedGet("foo", "bar"), Type.LONG, 1L);
        assertTypedEquals(record.typedGet("foo", "baz"), Type.LONG, 2L);
    }

    @Test
    public void testSetAndGettingFloatMaps() {
        Map<String, Float> data = new HashMap<>();
        data.put("bar", 1.1f);
        data.put("baz", 2.2f);
        record.setFloatMap("foo", data);

        Object object = revert(record.get("foo"));
        Assert.assertTrue(object instanceof Map);
        Map<String, Float> actual = (Map<String, Float>) object;
        Assert.assertEquals(actual.size(), 2);
        Assert.assertEquals(actual.get("bar"), 1.1f);
        Assert.assertEquals(actual.get("baz"), 2.2f);
        assertTypedEquals(record.typedGet("foo"), Type.FLOAT_MAP, data);
        assertTypedEquals(record.typedGet("foo", "bar"), Type.FLOAT, 1.1f);
        assertTypedEquals(record.typedGet("foo", "baz"), Type.FLOAT, 2.2f);
    }

    @Test
    public void testSetAndGettingDoubleMaps() {
        Map<String, Double> data = new HashMap<>();
        data.put("bar", 1.1);
        data.put("baz", 2.2);
        record.setDoubleMap("foo", data);

        Object object = revert(record.get("foo"));
        Assert.assertTrue(object instanceof Map);
        Map<String, Double> actual = (Map<String, Double>) object;
        Assert.assertEquals(actual.size(), 2);
        Assert.assertEquals(actual.get("bar"), 1.1);
        Assert.assertEquals(actual.get("baz"), 2.2);
        assertTypedEquals(record.typedGet("foo"), Type.DOUBLE_MAP, data);
        assertTypedEquals(record.typedGet("foo", "bar"), Type.DOUBLE, 1.1);
        assertTypedEquals(record.typedGet("foo", "baz"), Type.DOUBLE, 2.2);
    }

    @Test
    public void testSetAndGettingBooleanMaps() {
        Map<String, Boolean> data = new HashMap<>();
        data.put("bar", false);
        data.put("baz", true);
        record.setBooleanMap("foo", data);

        Object object = revert(record.get("foo"));
        Assert.assertTrue(object instanceof Map);
        Map<String, Boolean> actual = (Map<String, Boolean>) object;
        Assert.assertEquals(actual.size(), 2);
        Assert.assertFalse(actual.get("bar"));
        Assert.assertTrue(actual.get("baz"));
        assertTypedEquals(record.typedGet("foo"), Type.BOOLEAN_MAP, data);
        assertTypedEquals(record.typedGet("foo", "bar"), Type.BOOLEAN, false);
        assertTypedEquals(record.typedGet("foo", "baz"), Type.BOOLEAN, true);
    }

    @Test
    public void testSetAndGettingStringMaps() {
        Map<String, String> data = new HashMap<>();
        data.put("bar", "qux");
        data.put("baz", "norf");
        record.setStringMap("foo", data);

        Object object = revert(record.get("foo"));
        Assert.assertTrue(object instanceof Map);
        Map<String, String> actual = (Map<String, String>) object;
        Assert.assertEquals(actual.size(), 2);
        Assert.assertEquals(actual.get("bar"), "qux");
        Assert.assertEquals(actual.get("baz"), "norf");
        assertTypedEquals(record.typedGet("foo"), Type.STRING_MAP, data);
        assertTypedEquals(record.typedGet("foo", "bar"), Type.STRING, "qux");
        assertTypedEquals(record.typedGet("foo", "baz"), Type.STRING, "norf");
    }

    @Test
    public void testGettingMissingPrimitiveMapFields() {
        Object object = revert(record.get("dne"));
        Assert.assertNull(object);
        assertTypedEquals(record.typedGet("dne"), TypedObject.NULL);
        assertTypedEquals(record.typedGet("dne", "baz"), TypedObject.NULL);

        record.setStringMap("foo", singletonMap("bar", null));
        assertTypedEquals(record.typedGet("foo", "bar"), TypedObject.NULL);
        assertTypedEquals(record.typedGet("foo", "dne"), TypedObject.NULL);
    }

    @Test(expectedExceptions = ClassCastException.class)
    public void testGettingWrongTypePrimitiveMaps() {
        record.setString("foo", "bar");
        record.typedGet("foo", "baz");
    }

    @Test
    public void testSettingAndGettingIntegerLists() {
        List<Integer> data = new ArrayList<>();
        data.add(1);
        data.add(2);
        record.setIntegerList("foo", data);

        Object object = revert(record.get("foo"));
        Assert.assertTrue(object instanceof List);
        List<Integer> actual = (List<Integer>) object;
        Assert.assertEquals(actual.size(), 2);
        Assert.assertEquals(actual.get(0), (Integer) 1);
        Assert.assertEquals(actual.get(1), (Integer) 2);
        assertTypedEquals(record.typedGet("foo"), Type.INTEGER_LIST, data);
        assertTypedEquals(record.typedGet("foo", 0), Type.INTEGER, 1);
        assertTypedEquals(record.typedGet("foo", 1), Type.INTEGER, 2);
    }

    @Test
    public void testSettingAndGettingLongLists() {
        List<Long> data = new ArrayList<>();
        data.add(1L);
        data.add(2L);
        record.setLongList("foo", data);

        Object object = revert(record.get("foo"));
        Assert.assertTrue(object instanceof List);
        List<Long> actual = (List<Long>) object;
        Assert.assertEquals(actual.size(), 2);
        Assert.assertEquals(actual.get(0), (Long) 1L);
        Assert.assertEquals(actual.get(1), (Long) 2L);
        assertTypedEquals(record.typedGet("foo"), Type.LONG_LIST, data);
        assertTypedEquals(record.typedGet("foo", 0), Type.LONG, 1L);
        assertTypedEquals(record.typedGet("foo", 1), Type.LONG, 2L);
    }

    @Test
    public void testSettingAndGettingFloatLists() {
        List<Float> data = new ArrayList<>();
        data.add(1.1f);
        data.add(2.2f);
        record.setFloatList("foo", data);

        Object object = revert(record.get("foo"));
        Assert.assertTrue(object instanceof List);
        List<Float> actual = (List<Float>) object;
        Assert.assertEquals(actual.size(), 2);
        Assert.assertEquals(actual.get(0), 1.1f);
        Assert.assertEquals(actual.get(1), 2.2f);
        assertTypedEquals(record.typedGet("foo"), Type.FLOAT_LIST, data);
        assertTypedEquals(record.typedGet("foo", 0), Type.FLOAT, 1.1f);
        assertTypedEquals(record.typedGet("foo", 1), Type.FLOAT, 2.2f);
    }

    @Test
    public void testSettingAndGettingDoubleLists() {
        List<Double> data = new ArrayList<>();
        data.add(1.1);
        data.add(2.2);
        record.setDoubleList("foo", data);

        Object object = revert(record.get("foo"));
        Assert.assertTrue(object instanceof List);
        List<Double> actual = (List<Double>) object;
        Assert.assertEquals(actual.size(), 2);
        Assert.assertEquals(actual.get(0), 1.1);
        Assert.assertEquals(actual.get(1), 2.2);
        assertTypedEquals(record.typedGet("foo"), Type.DOUBLE_LIST, data);
        assertTypedEquals(record.typedGet("foo", 0), Type.DOUBLE, 1.1);
        assertTypedEquals(record.typedGet("foo", 1), Type.DOUBLE, 2.2);
    }

    @Test
    public void testSettingAndGettingBooleanLists() {
        List<Boolean> data = new ArrayList<>();
        data.add(false);
        data.add(true);
        record.setBooleanList("foo", data);

        Object object = revert(record.get("foo"));
        Assert.assertTrue(object instanceof List);
        List<Boolean> actual = (List<Boolean>) object;
        Assert.assertEquals(actual.size(), 2);
        Assert.assertFalse(actual.get(0));
        Assert.assertTrue(actual.get(1));
        assertTypedEquals(record.typedGet("foo"), Type.BOOLEAN_LIST, data);
        assertTypedEquals(record.typedGet("foo", 0), Type.BOOLEAN, false);
        assertTypedEquals(record.typedGet("foo", 1), Type.BOOLEAN, true);
    }

    @Test
    public void testSettingAndGettingStringLists() {
        List<String> data = new ArrayList<>();
        data.add("bar");
        data.add("baz");
        record.setStringList("foo", data);

        Object object = revert(record.get("foo"));
        Assert.assertTrue(object instanceof List);
        List<String> actual = (List<String>) object;
        Assert.assertEquals(actual.size(), 2);
        Assert.assertEquals(actual.get(0), "bar");
        Assert.assertEquals(actual.get(1), "baz");
        assertTypedEquals(record.typedGet("foo"), Type.STRING_LIST, data);
        assertTypedEquals(record.typedGet("foo", 0), Type.STRING, "bar");
        assertTypedEquals(record.typedGet("foo", 1), Type.STRING, "baz");
    }

    @Test
    public void testGettingMissingPrimitiveListEntries() {
        Object object = revert(record.get("dne"));
        Assert.assertNull(object);
        assertTypedEquals(record.typedGet("dne", 0), TypedObject.NULL);
        assertTypedEquals(record.typedGet("dne", 1), TypedObject.NULL);

        record.setStringList("foo", singletonList(null));
        assertTypedEquals(record.typedGet("foo", 0), TypedObject.NULL);
    }

    @Test(expectedExceptions = ClassCastException.class)
    public void testGettingWrongTypePrimitiveLists() {
        record.setString("foo", "bar");
        record.typedGet("foo", 0);
    }

    @Test(expectedExceptions = IndexOutOfBoundsException.class)
    public void testGetListBadIndex() {
        setListMap(record, "foo", singletonMap("a", 1L));
        record.typedGet("foo", 1);
    }

    @Test
    public void testSettingAndGettingIntegerMapOfMaps() {
        Map<String, Map<String, Integer>> data = new HashMap<>();
        data.put("bar", singletonMap("a", 1));
        data.put("baz", singletonMap("b", 2));
        record.setMapOfIntegerMap("fooA", data);

        Object object = revert(record.get("foo"));
        Assert.assertTrue(object instanceof Map);
        Map<String, Map<String, Integer>> actual = (Map<String, Map<String, Integer>>) object;
        Assert.assertEquals(actual.size(), 2);
        Map<String, Integer> actualA =  actual.get("bar");
        Map<String, Integer> actualB =  actual.get("baz");
        Assert.assertEquals(actualA.size(), 1);
        Assert.assertEquals(actualB.size(), 1);
        Assert.assertEquals(actualA.get("a"), (Integer) 1);
        Assert.assertEquals(actualB.get("b"), (Integer) 2);
        assertTypedEquals(record.typedGet("foo"), Type.INTEGER_MAP_MAP, data);
        assertTypedEquals(record.typedGet("foo", "bar"), Type.INTEGER_MAP, singletonMap("a", 1));
        assertTypedEquals(record.typedGet("foo", "baz"), Type.INTEGER_MAP, singletonMap("b", 2));
        assertTypedEquals(record.typedGet("foo", "bar", "a"), Type.INTEGER, 1);
        assertTypedEquals(record.typedGet("foo", "baz", "b"), Type.INTEGER, 2);
    }

    @Test
    public void testSettingAndGettingLongMapOfMaps() {
        Map<String, Map<String, Long>> data = new HashMap<>();
        data.put("bar", singletonMap("a", 1L));
        data.put("baz", singletonMap("b", 2L));
        record.setMapOfLongMap("fooA", data);

        Object object = revert(record.get("foo"));
        Assert.assertTrue(object instanceof Map);
        Map<String, Map<String, Long>> actual = (Map<String, Map<String, Long>>) object;
        Assert.assertEquals(actual.size(), 2);
        Map<String, Long> actualA =  actual.get("bar");
        Map<String, Long> actualB =  actual.get("baz");
        Assert.assertEquals(actualA.size(), 1);
        Assert.assertEquals(actualB.size(), 1);
        Assert.assertEquals(actualA.get("a"), (Long) 1L);
        Assert.assertEquals(actualB.get("b"), (Long) 2L);
        assertTypedEquals(record.typedGet("foo"), Type.LONG_MAP_MAP, data);
        assertTypedEquals(record.typedGet("foo", "bar"), Type.LONG_MAP, singletonMap("a", 1L));
        assertTypedEquals(record.typedGet("foo", "baz"), Type.LONG_MAP, singletonMap("b", 2L));
        assertTypedEquals(record.typedGet("foo", "bar", "a"), Type.LONG, 1L);
        assertTypedEquals(record.typedGet("foo", "baz", "b"), Type.LONG, 2L);
    }

    @Test
    public void testSettingAndGettingFloatMapOfMaps() {
        Map<String, Map<String, Float>> data = new HashMap<>();
        data.put("bar", singletonMap("a", 1.1f));
        data.put("baz", singletonMap("b", 2.2f));
        record.setMapOfFloatMap("fooA", data);

        Object object = revert(record.get("foo"));
        Assert.assertTrue(object instanceof Map);
        Map<String, Map<String, Float>> actual = (Map<String, Map<String, Float>>) object;
        Assert.assertEquals(actual.size(), 2);
        Map<String, Float> actualA =  actual.get("bar");
        Map<String, Float> actualB =  actual.get("baz");
        Assert.assertEquals(actualA.size(), 1);
        Assert.assertEquals(actualB.size(), 1);
        Assert.assertEquals(actualA.get("a"), 1.1f);
        Assert.assertEquals(actualB.get("b"), 2.2f);
        assertTypedEquals(record.typedGet("foo"), Type.FLOAT_MAP_MAP, data);
        assertTypedEquals(record.typedGet("foo", "bar"), Type.FLOAT_MAP, singletonMap("a", 1.1f));
        assertTypedEquals(record.typedGet("foo", "baz"), Type.FLOAT_MAP, singletonMap("b", 2.2f));
        assertTypedEquals(record.typedGet("foo", "bar", "a"), Type.FLOAT, 1.1f);
        assertTypedEquals(record.typedGet("foo", "baz", "b"), Type.FLOAT, 2.2f);
    }

    @Test
    public void testSettingAndGettingDoubleMapOfMaps() {
        Map<String, Map<String, Double>> data = new HashMap<>();
        data.put("bar", singletonMap("a", 1.1));
        data.put("baz", singletonMap("b", 2.2));
        record.setMapOfDoubleMap("fooA", data);

        Object object = revert(record.get("foo"));
        Assert.assertTrue(object instanceof Map);
        Map<String, Map<String, Double>> actual = (Map<String, Map<String, Double>>) object;
        Assert.assertEquals(actual.size(), 2);
        Map<String, Double> actualA =  actual.get("bar");
        Map<String, Double> actualB =  actual.get("baz");
        Assert.assertEquals(actualA.size(), 1);
        Assert.assertEquals(actualB.size(), 1);
        Assert.assertEquals(actualA.get("a"), 1.1);
        Assert.assertEquals(actualB.get("b"), 2.2);
        assertTypedEquals(record.typedGet("foo"), Type.DOUBLE_MAP_MAP, data);
        assertTypedEquals(record.typedGet("foo", "bar"), Type.DOUBLE_MAP, singletonMap("a", 1.1));
        assertTypedEquals(record.typedGet("foo", "baz"), Type.DOUBLE_MAP, singletonMap("b", 2.2));
        assertTypedEquals(record.typedGet("foo", "bar", "a"), Type.DOUBLE, 1.1);
        assertTypedEquals(record.typedGet("foo", "baz", "b"), Type.DOUBLE, 2.2);
    }

    @Test
    public void testSettingAndGettingBooleanMapOfMaps() {
        Map<String, Map<String, Boolean>> data = new HashMap<>();
        data.put("bar", singletonMap("a", false));
        data.put("baz", singletonMap("b", true));
        record.setMapOfBooleanMap("fooA", data);

        Object object = revert(record.get("foo"));
        Assert.assertTrue(object instanceof Map);
        Map<String, Map<String, Boolean>> actual = (Map<String, Map<String, Boolean>>) object;
        Assert.assertEquals(actual.size(), 2);
        Map<String, Boolean> actualA =  actual.get("bar");
        Map<String, Boolean> actualB =  actual.get("baz");
        Assert.assertEquals(actualA.size(), 1);
        Assert.assertEquals(actualB.size(), 1);
        Assert.assertFalse(actualA.get("a"));
        Assert.assertTrue(actualB.get("b"));
        assertTypedEquals(record.typedGet("foo"), Type.BOOLEAN_MAP_MAP, data);
        assertTypedEquals(record.typedGet("foo", "bar"), Type.BOOLEAN_MAP, singletonMap("a", false));
        assertTypedEquals(record.typedGet("foo", "baz"), Type.BOOLEAN_MAP, singletonMap("b", true));
        assertTypedEquals(record.typedGet("foo", "bar", "a"), Type.BOOLEAN, false);
        assertTypedEquals(record.typedGet("foo", "baz", "b"), Type.BOOLEAN, true);
    }

    @Test
    public void testSettingAndGettingStringMapOfMaps() {
        Map<String, Map<String, String>> data = new HashMap<>();
        data.put("bar", singletonMap("a", "qux"));
        data.put("baz", singletonMap("b", "norf"));
        record.setMapOfStringMap("fooA", data);

        Object object = revert(record.get("foo"));
        Assert.assertTrue(object instanceof Map);
        Map<String, Map<String, String>> actual = (Map<String, Map<String, String>>) object;
        Assert.assertEquals(actual.size(), 2);
        Map<String, String> actualA =  actual.get("bar");
        Map<String, String> actualB =  actual.get("baz");
        Assert.assertEquals(actualA.size(), 1);
        Assert.assertEquals(actualB.size(), 1);
        Assert.assertEquals(actualA.get("a"), "qux");
        Assert.assertEquals(actualB.get("b"), "norf");
        assertTypedEquals(record.typedGet("foo"), Type.STRING_MAP_MAP, data);
        assertTypedEquals(record.typedGet("foo", "bar"), Type.STRING_MAP, singletonMap("a", "qux"));
        assertTypedEquals(record.typedGet("foo", "baz"), Type.STRING_MAP, singletonMap("b", "norf"));
        assertTypedEquals(record.typedGet("foo", "bar", "a"), Type.STRING, "qux");
        assertTypedEquals(record.typedGet("foo", "baz", "b"), Type.STRING, "baz");
    }

    @Test
    public void testGettingMissingMapOfMaps() {
        Object object = revert(record.get("dne"));
        Assert.assertNull(object);
        assertTypedEquals(record.typedGet("dne"), TypedObject.NULL);
        assertTypedEquals(record.typedGet("dne", "bar"), TypedObject.NULL);
        assertTypedEquals(record.typedGet("dne", "bar", "baz"), TypedObject.NULL);

        record.setMapOfStringMap("foo", singletonMap("bar", null));
        assertTypedEquals(record.typedGet("foo", "bar"), TypedObject.NULL);
        record.setMapOfStringMap("foo", singletonMap("bar", singletonMap("baz", "qux")));
        assertTypedEquals(record.typedGet("foo", "dne"), TypedObject.NULL);
        assertTypedEquals(record.typedGet("foo", "bar", "dne"), TypedObject.NULL);
    }

    @Test(expectedExceptions = ClassCastException.class)
    public void testGettingWrongTypeMapOfMaps() {
        Map<String, String> data = new HashMap<>();
        data.put("bar", "qux");
        record.setStringMap("foo", data);
        record.typedGet("foo", "bar", "baz");
    }

    @Test
    public void testSettingAndGettingListOfIntegerMaps() {
        List<Map<String, Integer>> data = new ArrayList<>();
        data.add(singletonMap("a", 1));
        data.add(singletonMap("b", 2));
        record.setListOfIntegerMap("foo", data);

        Object object = revert(record.get("foo"));
        Assert.assertTrue(object instanceof List);
        List<Map<String, Integer>> actual = (List<Map<String, Integer>>) object;
        Assert.assertEquals(actual.size(), 2);
        Map<String, Integer> actualA = actual.get(0);
        Map<String, Integer> actualB = actual.get(1);
        Assert.assertEquals(actualA.size(), 1);
        Assert.assertEquals(actualB.size(), 1);
        Assert.assertEquals(actualA.get("a"), (Integer) 1);
        Assert.assertEquals(actualB.get("b"), (Integer) 2);
        assertTypedEquals(record.typedGet("foo"), Type.INTEGER_MAP_LIST, data);
        assertTypedEquals(record.typedGet("foo", "bar"), Type.INTEGER_MAP, singletonMap("a", 1));
        assertTypedEquals(record.typedGet("foo", "baz"), Type.INTEGER_MAP, singletonMap("b", 2));
        assertTypedEquals(record.typedGet("foo", 0, "a"), Type.INTEGER, 1);
        assertTypedEquals(record.typedGet("foo", 1, "b"), Type.INTEGER, 2);
    }

    @Test
    public void testSettingAndGettingListOfLongMaps() {
        List<Map<String, Long>> data = new ArrayList<>();
        data.add(singletonMap("a", 1L));
        data.add(singletonMap("b", 2L));
        record.setListOfLongMap("foo", data);

        Object object = revert(record.get("foo"));
        Assert.assertTrue(object instanceof List);
        List<Map<String, Long>> actual = (List<Map<String, Long>>) object;
        Assert.assertEquals(actual.size(), 2);
        Map<String, Long> actualA = actual.get(0);
        Map<String, Long> actualB = actual.get(1);
        Assert.assertEquals(actualA.size(), 1);
        Assert.assertEquals(actualB.size(), 1);
        Assert.assertEquals(actualA.get("a"), (Long) 1L);
        Assert.assertEquals(actualB.get("b"), (Long) 2L);
        assertTypedEquals(record.typedGet("foo"), Type.LONG_MAP_LIST, data);
        assertTypedEquals(record.typedGet("foo", "bar"), Type.LONG_MAP, singletonMap("a", 1L));
        assertTypedEquals(record.typedGet("foo", "baz"), Type.LONG_MAP, singletonMap("b", 2L));
        assertTypedEquals(record.typedGet("foo", 0, "a"), Type.LONG, 1L);
        assertTypedEquals(record.typedGet("foo", 1, "b"), Type.LONG, 2L);
    }

    @Test
    public void testSettingAndGettingListOfFloatMaps() {
        List<Map<String, Float>> data = new ArrayList<>();
        data.add(singletonMap("a", 1.1f));
        data.add(singletonMap("b", 2.2f));
        record.setListOfFloatMap("foo", data);

        Object object = revert(record.get("foo"));
        Assert.assertTrue(object instanceof List);
        List<Map<String, Float>> actual = (List<Map<String, Float>>) object;
        Assert.assertEquals(actual.size(), 2);
        Map<String, Float> actualA = actual.get(0);
        Map<String, Float> actualB = actual.get(1);
        Assert.assertEquals(actualA.size(), 1);
        Assert.assertEquals(actualB.size(), 1);
        Assert.assertEquals(actualA.get("a"), 1.1f);
        Assert.assertEquals(actualB.get("b"), 2.2f);
        assertTypedEquals(record.typedGet("foo"), Type.FLOAT_MAP_LIST, data);
        assertTypedEquals(record.typedGet("foo", "bar"), Type.FLOAT_MAP, singletonMap("a", 1.1f));
        assertTypedEquals(record.typedGet("foo", "baz"), Type.FLOAT_MAP, singletonMap("b", 2.2f));
        assertTypedEquals(record.typedGet("foo", 0, "a"), Type.FLOAT, 1.1f);
        assertTypedEquals(record.typedGet("foo", 1, "b"), Type.FLOAT, 2.2f);
    }

    @Test
    public void testSettingAndGettingListOfDoubleMaps() {
        List<Map<String, Double>> data = new ArrayList<>();
        data.add(singletonMap("a", 1.1));
        data.add(singletonMap("b", 2.2));
        record.setListOfDoubleMap("foo", data);

        Object object = revert(record.get("foo"));
        Assert.assertTrue(object instanceof List);
        List<Map<String, Double>> actual = (List<Map<String, Double>>) object;
        Assert.assertEquals(actual.size(), 2);
        Map<String, Double> actualA = actual.get(0);
        Map<String, Double> actualB = actual.get(1);
        Assert.assertEquals(actualA.size(), 1);
        Assert.assertEquals(actualB.size(), 1);
        Assert.assertEquals(actualA.get("a"), 1.1);
        Assert.assertEquals(actualB.get("b"), 2.2);
        assertTypedEquals(record.typedGet("foo"), Type.DOUBLE_MAP_LIST, data);
        assertTypedEquals(record.typedGet("foo", "bar"), Type.DOUBLE_MAP, singletonMap("a", 1.1));
        assertTypedEquals(record.typedGet("foo", "baz"), Type.DOUBLE_MAP, singletonMap("b", 2.2));
        assertTypedEquals(record.typedGet("foo", 0, "a"), Type.DOUBLE, 1.1);
        assertTypedEquals(record.typedGet("foo", 1, "b"), Type.DOUBLE, 2.2);
    }

    @Test
    public void testSettingAndGettingListOfBooleanMaps() {
        List<Map<String, Boolean>> data = new ArrayList<>();
        data.add(singletonMap("a", false));
        data.add(singletonMap("b", true));
        record.setListOfBooleanMap("foo", data);

        Object object = revert(record.get("foo"));
        Assert.assertTrue(object instanceof List);
        List<Map<String, Boolean>> actual = (List<Map<String, Boolean>>) object;
        Assert.assertEquals(actual.size(), 2);
        Map<String, Boolean> actualA = actual.get(0);
        Map<String, Boolean> actualB = actual.get(1);
        Assert.assertEquals(actualA.size(), 1);
        Assert.assertEquals(actualB.size(), 1);
        Assert.assertFalse(actualA.get("a"));
        Assert.assertTrue(actualB.get("b"));
        assertTypedEquals(record.typedGet("foo"), Type.BOOLEAN_MAP_LIST, data);
        assertTypedEquals(record.typedGet("foo", "bar"), Type.BOOLEAN_MAP, singletonMap("a", false));
        assertTypedEquals(record.typedGet("foo", "baz"), Type.BOOLEAN_MAP, singletonMap("b", true));
        assertTypedEquals(record.typedGet("foo", 0, "a"), Type.BOOLEAN, false);
        assertTypedEquals(record.typedGet("foo", 1, "b"), Type.BOOLEAN, true);
    }

    @Test
    public void testSettingAndGettingListOfStringMaps() {
        List<Map<String, String>> data = new ArrayList<>();
        data.add(singletonMap("a", "qux"));
        data.add(singletonMap("b", "norf"));
        record.setListOfStringMap("foo", data);

        Object object = revert(record.get("foo"));
        Assert.assertTrue(object instanceof List);
        List<Map<String, String>> actual = (List<Map<String, String>>) object;
        Assert.assertEquals(actual.size(), 2);
        Map<String, String> actualA = actual.get(0);
        Map<String, String> actualB = actual.get(1);
        Assert.assertEquals(actualA.size(), 1);
        Assert.assertEquals(actualB.size(), 1);
        Assert.assertEquals(actualA.get("a"), "qux");
        Assert.assertEquals(actualB.get("b"), "norf");
        assertTypedEquals(record.typedGet("foo"), Type.STRING_MAP_LIST, data);
        assertTypedEquals(record.typedGet("foo", "bar"), Type.STRING_MAP, singletonMap("a", "qux"));
        assertTypedEquals(record.typedGet("foo", "baz"), Type.STRING_MAP, singletonMap("b", "norf"));
        assertTypedEquals(record.typedGet("foo", 0, "a"), Type.STRING, "qux");
        assertTypedEquals(record.typedGet("foo", 1, "b"), Type.STRING, "norf");
    }

    @Test
    public void testGettingMissingListOfMaps() {
        Object object = revert(record.get("dne"));
        Assert.assertNull(object);
        assertTypedEquals(record.typedGet("dne"), TypedObject.NULL);
        assertTypedEquals(record.typedGet("dne", 0), TypedObject.NULL);

        record.setListOfStringMap("foo", singletonList(null));
        assertTypedEquals(record.typedGet("foo", 0, "bar"), TypedObject.NULL);
        record.setListOfStringMap("foo", singletonList(singletonMap("bar", null)));
        assertTypedEquals(record.typedGet("foo", 0, "bar"), TypedObject.NULL);
        record.setListOfStringMap("foo", singletonList(singletonMap("bar", "baz")));
        assertTypedEquals(record.typedGet("foo", 0, "dne"), TypedObject.NULL);
    }

    @Test(expectedExceptions = ClassCastException.class)
    public void testGettingWrongTypeListOfMaps() {
        record.setStringList("foo", singletonList("bar"));
        record.typedGet("foo", 0, "baz");
    }

    @Test(expectedExceptions = IndexOutOfBoundsException.class)
    public void testGetListOfMapsBadIndex() {
        setListMap(record, "foo", singletonMap("a", 1L));
        record.typedGet("foo", 1, "c");
    }

    @Test
    public void testInsertingFromAnotherRecord() {
        record.setString("a", "foo");
        record.setStringMap("b", singletonMap("b.1", "bar"));
        Map<String, Boolean> data = new HashMap<>();
        data.put("c.1", false);
        record.setListOfBooleanMap("c", singletonList(data));
        record.setStringList("d", singletonList("norf"));
        record.setMapOfStringMap("e", singletonMap("e.f", singletonMap("e.f.g", "baz")));

        another.set("newA", record, "a");
        Assert.assertEquals(revert(another.get("newA")), "foo");

        another.set("newB", record, "b", "b.1");
        Assert.assertEquals(revert(another.get("newB")), "bar");

        another.set("newC", record, "c", 0);
        Assert.assertEquals(revert(another.get("newC")), singletonMap("c.1", false));

        // The Map isn't copied
        Map<String, Boolean> anotherC = (Map<String, Boolean>) ((List) revert(record.get("c"))).get(0);
        anotherC.put("c.1", true);
        Assert.assertEquals(revert(another.get("newC")), singletonMap("c.1", true));

        // And not in the first record either
        data.put("c.2", false);
        Map<String, Boolean> originalC = (Map<String, Boolean>) ((List) revert(record.get("c"))).get(0);
        Assert.assertFalse(originalC.get("c.2"));
        // Reget from another
        anotherC = (Map<String, Boolean>) ((List) revert(another.get("c"))).get(0);
        Assert.assertFalse(anotherC.get("c.2"));

        another.set("newD", record, "d", 0);
        Assert.assertEquals(revert(another.get("newD")), "norf");

        another.set("newE", record, "e", "e.f", "e.f.g");
        Assert.assertEquals(revert(another.get("newE")), "baz");

        another.set("newF", record, "c", 0, "c.1");
        Assert.assertEquals(revert(another.get("newF")), true);
    }

    @Test
    public void testExtractTopLevelField() {
        record.setListOfStringMap("foo", singletonList(singletonMap("qux", "norf")));
        record.setString("bar", "baz");

        Assert.assertEquals(revert(extractField(record, "bar")), "baz");
        Assert.assertNull(revert(extractField(record, "bar.baz")));
        Assert.assertEquals(revert(extractField(record, "foo")), singletonList(singletonMap("qux", "norf")));
        Assert.assertNull(revert(extractField(record, "foo.bar")));
        Assert.assertNull(revert(extractField(record, "dne")));
    }

    @Test
    public void testExtractMapField() {
        setMap(record, "foo", Pair.of("bar", singletonMap("baz", 1L)), Pair.of("baz", singletonMap("qux", 2L)));

        Assert.assertTrue(revert(extractField(record, "foo")) instanceof Map);
        Assert.assertEquals(revert(extractField(record, "foo.bar")), singletonMap("baz", 1L));
        Assert.assertEquals(revert(extractField(record, "foo.baz")), singletonMap("qux", 2L));
        Assert.assertEquals(revert(extractField(record, "foo.bar.baz")), 1L);
        Assert.assertEquals(revert(extractField(record, "foo.baz.qux")), 2L);
        Assert.assertNull(revert(extractField(record, "foo.bar.dne")));

        Assert.assertNull(revert(extractField(record, "dne")));
        Assert.assertNull(revert(extractField(record, "foo.")));
        Assert.assertNull(revert(extractField(record, "foo.dne")));
    }

    @Test
    public void testExtractListIndex() {
        setListMap(record, "foo", singletonMap("a", 1L), singletonMap("b", 2L));
        List<String> data = new ArrayList<>();
        data.add("qux");
        data.add("norf");
        record.setStringList("bar", data);
        Assert.assertTrue(revert(extractField(record, "foo")) instanceof List);
        Assert.assertEquals(revert(extractField(record, "foo.0")), singletonMap("a", 1L));
        Assert.assertEquals(revert(extractField(record, "foo.1")), singletonMap("b", 2L));
        Assert.assertEquals(revert(extractField(record, "foo.0.a")), 1L);
        Assert.assertEquals(revert(extractField(record, "foo.1.b")), 2L);
        Assert.assertNull(revert(extractField(record, "foo.0.c")));
        Assert.assertTrue(revert(extractField(record, "bar")) instanceof List);
        Assert.assertEquals(revert(extractField(record, "bar.0")), "qux");
        Assert.assertEquals(revert(extractField(record, "bar.1")), "norf");

        Assert.assertNull(revert(extractField(record, "dne.-1")));
        Assert.assertNull(revert(extractField(record, "dne.0")));
        Assert.assertNull(revert(extractField(record, "dne.123123")));
    }

    @Test
    public void testExtractListBadIndex() {
        setListMap(record, "foo", singletonMap("a", 1L));
        Assert.assertNull(revert(extractField(record, "foo.1")));
    }

    @Test
    public void testIterator() {
        setMap(record, "4", Pair.of("4.1", false));
        record.setString("1", "bar").setLong("2", 42L)
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

        int size = 0;
        for (Map.Entry<String, T> entry : record) {
            size++;
            Assert.assertEquals(entry.getValue(), expectedMap.get(entry.getKey()));
        }
        Assert.assertEquals(size, 8);
        Assert.assertEquals(record.fieldCount(), 8);
    }

    @Test
    public void testToString() {
        Assert.assertEquals(record.toString(), "{}");
        record.setString("1", "bar");
        Assert.assertEquals(record.toString(), "{1:bar}");
    }

    @Test
    public void testEqualsEdgeCases() {
        record.setString("1", "bar").setLong("2", 42L).setBoolean("3", false);
        Assert.assertNotEquals(record, null);
        Assert.assertNotEquals(record, "foo");
    }

    @Test
    public void testEqualsDifferentRecord() {
        record.setString("1", "bar").setLong("2", 42L).setBoolean("3", false);
        another.setString("1", "bar");
        Assert.assertNotEquals(another, record);
    }

    @Test
    public void testEqualsAndHashcodeSameRecord() {
        setMap(record, "4", Pair.of("4.1", false), Pair.of("4.2", true), Pair.of("4.3", null));
        record.setString("1", "bar").setLong("2", 42L).setBoolean("3", false)
              .setListOfStringMap("5", singletonList(singletonMap("5.1", "foo")))
              .setStringList("6", singletonList("baz"));
        setMap(another, "4", Pair.of("4.1", false), Pair.of("4.2", true), Pair.of("4.3", null));
        another.setString("1", "bar").setLong("2", 42L).setBoolean("3", false)
               .setStringList("6", singletonList("baz"))
               .setListOfStringMap("5", singletonList(singletonMap("5.1", "foo")));
        Assert.assertEquals(another, record);
        Assert.assertEquals(record.hashCode(), another.hashCode());
    }

    @Test
    public void testRenaming() {
        setMap(record, "7", Pair.of("4.1", false), Pair.of("7.2", true));
        record.setString("1", "bar").setLong("2", 42L).setBoolean("3", false).setDouble("4", 0.34)
              .setListOfLongMap("9", singletonList(singletonMap("9.1", 3L)))
              .setLongList("11", singletonList(4L));

        record.rename("1", "new1").rename("3", "new3").rename("7.4.1", "new2");

        setMap(another, "7", Pair.of("4.1", false), Pair.of("7.2", true));
        another.setString("new1", "bar").setLong("2", 42L).setBoolean("new3", false).setDouble("4", 0.34)
               .setListOfLongMap("9", singletonList(singletonMap("9.1", 3L)))
               .setLongList("11", singletonList(4L));

        Assert.assertEquals(record, another);
    }

    @Test
    public void testFieldCount() {
        Assert.assertEquals(record.fieldCount(), 0);

        record.setString("foo", "bar");
        Assert.assertEquals(record.fieldCount(), 1);

        setMap(record, "7", Pair.of("4.1", false), Pair.of("7.2", true));
        Assert.assertEquals(record.fieldCount(), 2);

        record.remove("2");
        record.remove("7");
        Assert.assertEquals(record.fieldCount(), 1);
    }

    @Test
    public void testRemoving() {
        setMap(record, "7", Pair.of("4.1", false), Pair.of("7.2", true));
        record.setString("1", "bar").setLong("2", 42L).setBoolean("3", false).setDouble("4", 0.34)
              .setListOfLongMap("9", singletonList(singletonMap("9.1", 3L)))
              .setLongList("11", singletonList(4L));

        record.remove("1").remove("3").remove("7.4.1").remove("9").remove("11");

        setMap(another, "7", Pair.of("4.1", false), Pair.of("7.2", true));
        another.setLong("2", 42L).setDouble("4", 0.34);
        Assert.assertEquals(record, another);
    }

    @Test
    public void testRemovingField() {
        setMap(record, "7", Pair.of("4.1", false), Pair.of("7.2", true));
        record.setString("1", "bar").setLong("2", 42L).setBoolean("3", false).setDouble("4", 0.34)
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
        setMap(record, "7", Pair.of("4.1", false), Pair.of("7.2", true));
        record.setString("1", "bar").setLong("2", 42L).setBoolean("3", false).setDouble("4", 0.34);

        Assert.assertTrue(record.hasField("1"));
        Assert.assertTrue(record.hasField("7"));
        Assert.assertFalse(record.hasField("7.4.1"));
        Assert.assertFalse(record.hasField("foo"));
    }
}
