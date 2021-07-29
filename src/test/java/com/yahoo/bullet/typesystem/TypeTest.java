/*
 *  Copyright 2020, Yahoo Inc.
 *  Licensed under the terms of the Apache License, Version 2.0.
 *  See the LICENSE file associated with the project for terms.
 */
package com.yahoo.bullet.typesystem;

import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiPredicate;

import static com.yahoo.bullet.TestHelpers.assertException;
import static com.yahoo.bullet.TestHelpers.list;
import static com.yahoo.bullet.TestHelpers.map;
import static com.yahoo.bullet.TestHelpers.nestedList;
import static com.yahoo.bullet.TestHelpers.nestedMap;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

public class TypeTest {
    private static final ArrayList<Boolean> SIMPLE_BOOLEAN_LIST = list(singletonList(true));
    private static final ArrayList<Integer> SIMPLE_INTEGER_LIST = list(singletonList(1));
    private static final ArrayList<Long> SIMPLE_LONG_LIST = list(singletonList(1L));
    private static final ArrayList<Float> SIMPLE_FLOAT_LIST = list(singletonList(1.0f));
    private static final ArrayList<Double> SIMPLE_DOUBLE_LIST = list(singletonList(1.0));
    private static final ArrayList<String> SIMPLE_STRING_LIST = list(singletonList("1"));
    private static final HashMap<String, Boolean> SIMPLE_BOOLEAN_MAP = map(singletonMap("a", true));
    private static final HashMap<String, Integer> SIMPLE_INTEGER_MAP = map(singletonMap("a", 1));
    private static final HashMap<String, Long> SIMPLE_LONG_MAP = map(singletonMap("a", 1L));
    private static final HashMap<String, Float> SIMPLE_FLOAT_MAP = map(singletonMap("a", 1.0f));
    private static final HashMap<String, Double> SIMPLE_DOUBLE_MAP = map(singletonMap("a", 1.0));
    private static final HashMap<String, String> SIMPLE_STRING_MAP = map(singletonMap("a", "1"));
    private static final HashMap<String, HashMap<String, Boolean>> SIMPLE_BOOLEAN_MAP_MAP = nestedMap(singletonMap("a", SIMPLE_BOOLEAN_MAP));
    private static final HashMap<String, HashMap<String, Integer>> SIMPLE_INTEGER_MAP_MAP = nestedMap(singletonMap("a", SIMPLE_INTEGER_MAP));
    private static final HashMap<String, HashMap<String, Long>> SIMPLE_LONG_MAP_MAP = nestedMap(singletonMap("a", SIMPLE_LONG_MAP));
    private static final HashMap<String, HashMap<String, Float>> SIMPLE_FLOAT_MAP_MAP = nestedMap(singletonMap("a", SIMPLE_FLOAT_MAP));
    private static final HashMap<String, HashMap<String, Double>> SIMPLE_DOUBLE_MAP_MAP = nestedMap(singletonMap("a", SIMPLE_DOUBLE_MAP));
    private static final HashMap<String, HashMap<String, String>> SIMPLE_STRING_MAP_MAP = nestedMap(singletonMap("a", SIMPLE_STRING_MAP));
    private static final ArrayList<HashMap<String, Boolean>> SIMPLE_BOOLEAN_MAP_LIST = nestedList(singletonList(SIMPLE_BOOLEAN_MAP));
    private static final ArrayList<HashMap<String, Integer>> SIMPLE_INTEGER_MAP_LIST = nestedList(singletonList(SIMPLE_INTEGER_MAP));
    private static final ArrayList<HashMap<String, Long>> SIMPLE_LONG_MAP_LIST = nestedList(singletonList(SIMPLE_LONG_MAP));
    private static final ArrayList<HashMap<String, Float>> SIMPLE_FLOAT_MAP_LIST = nestedList(singletonList(SIMPLE_FLOAT_MAP));
    private static final ArrayList<HashMap<String, Double>> SIMPLE_DOUBLE_MAP_LIST = nestedList(singletonList(SIMPLE_DOUBLE_MAP));
    private static final ArrayList<HashMap<String, String>> SIMPLE_STRING_MAP_LIST = nestedList(singletonList(SIMPLE_STRING_MAP));

    private static void assertAllSatisfy(Collection<Type> collection, BiPredicate<Type, Type> check) {
        for (Type typeA : collection) {
            for (Type typeB : collection) {
                assertTrue(check.test(typeA, typeB));
            }
        }
    }

    private static void assertAllFail(Collection<Type> collectionA, Collection<Type> collectionB, BiPredicate<Type, Type> check) {
        for (Type typeA : collectionA) {
            for (Type typeB : collectionB) {
                assertFalse(check.test(typeA, typeB));
            }
        }
    }

    @Test
    public void testTypeFindingAtomic() {
        assertEquals(Type.getType(null), Type.NULL);
        assertEquals(Type.getType(true), Type.BOOLEAN);
        assertEquals(Type.getType("foo"), Type.STRING);
        assertEquals(Type.getType(1), Type.INTEGER);
        assertEquals(Type.getType(1L), Type.LONG);
        assertEquals(Type.getType(3.14F), Type.FLOAT);
        assertEquals(Type.getType(1.2), Type.DOUBLE);
        assertEquals(Type.getType('8'), Type.UNKNOWN);
        assertEquals(Type.getType(Type.LONG), Type.UNKNOWN);
    }

    @Test
    public void testTypeFindingPrimitiveLists() {
        assertEquals(Type.getType(singletonList(false)), Type.BOOLEAN_LIST);
        assertEquals(Type.getType(singletonList("foo")), Type.STRING_LIST);
        assertEquals(Type.getType(asList(1, null, 5)), Type.INTEGER_LIST);
        assertEquals(Type.getType(asList(1L, 24L)), Type.LONG_LIST);
        assertEquals(Type.getType(singletonList(1.4f)), Type.FLOAT_LIST);
        assertEquals(Type.getType(singletonList(1.4)), Type.DOUBLE_LIST);
        assertEquals(Type.getType(singletonList(null)), Type.UNKNOWN_LIST);
        assertEquals(Type.getType(singletonList(new HashSet<>())), Type.UNKNOWN_LIST);
        assertEquals(Type.getType(emptyList()), Type.UNKNOWN_LIST);
    }

    @Test
    public void testTypeFindingListOfMaps() {
        assertEquals(Type.getType(singletonList(singletonMap("a", false))), Type.BOOLEAN_MAP_LIST);
        assertEquals(Type.getType(singletonList(singletonMap("a", "foo"))), Type.STRING_MAP_LIST);
        assertEquals(Type.getType(asList(singletonMap("a", null), singletonMap("b", 1))), Type.UNKNOWN_MAP_LIST);
        assertEquals(Type.getType(asList(singletonMap("a", null), singletonMap("b", 1L))), Type.UNKNOWN_MAP_LIST);
        assertEquals(Type.getType(asList(singletonMap("a", 1.4f), singletonMap("b", 4.2f))), Type.FLOAT_MAP_LIST);
        assertEquals(Type.getType(singletonList(singletonMap("a", 1.4))), Type.DOUBLE_MAP_LIST);
        assertEquals(Type.getType(singletonList(singletonMap("a", null))), Type.UNKNOWN_MAP_LIST);
        assertEquals(Type.getType(singletonList(singletonMap("a", new HashSet<>()))), Type.UNKNOWN_MAP_LIST);
        assertEquals(Type.getType(singletonList(singletonMap(1, 0))), Type.UNKNOWN_MAP_LIST);
        assertEquals(Type.getType(asList(singletonMap("a", null), singletonMap(1, 0))), Type.UNKNOWN_MAP_LIST);
        assertEquals(Type.getType(singletonList(emptyMap())), Type.UNKNOWN_MAP_LIST);
    }

    @Test
    public void testTypeFindingPrimitiveMaps() {
        assertEquals(Type.getType(singletonMap("a", true)), Type.BOOLEAN_MAP);
        assertEquals(Type.getType(singletonMap("a", "foo")), Type.STRING_MAP);
        assertEquals(Type.getType(singletonMap("a", 1)), Type.INTEGER_MAP);
        assertEquals(Type.getType(singletonMap("a", 1L)), Type.LONG_MAP);
        assertEquals(Type.getType(singletonMap("a", 1.0f)), Type.FLOAT_MAP);
        Map<String, Double> data = new LinkedHashMap<>();
        data.put("a", null);
        data.put(null, 1.0);
        assertEquals(Type.getType(data), Type.DOUBLE_MAP);
        assertEquals(Type.getType(singletonMap("a", null)), Type.UNKNOWN_MAP);
        assertEquals(Type.getType(singletonMap(1, 1.0f)), Type.UNKNOWN_MAP);
        assertEquals(Type.getType(singletonMap("a", new HashSet<>())), Type.UNKNOWN_MAP);
        assertEquals(Type.getType(emptyMap()), Type.UNKNOWN_MAP);
    }

    @Test
    public void testTypeFindingMapOfMaps() {
        assertEquals(Type.getType(singletonMap("a", singletonMap("a", true))), Type.BOOLEAN_MAP_MAP);
        assertEquals(Type.getType(singletonMap("a", singletonMap("a", "foo"))), Type.STRING_MAP_MAP);
        assertEquals(Type.getType(singletonMap("a", singletonMap("a", 1))), Type.INTEGER_MAP_MAP);
        assertEquals(Type.getType(singletonMap("a", singletonMap("a", 1L))), Type.LONG_MAP_MAP);
        assertEquals(Type.getType(singletonMap("a", singletonMap("a", 1.0f))), Type.FLOAT_MAP_MAP);
        assertEquals(Type.getType(emptyMap()), Type.UNKNOWN_MAP);
        assertEquals(Type.getType(singletonMap("a", singletonMap(1, 1.0f))), Type.UNKNOWN_MAP_MAP);
        assertEquals(Type.getType(singletonMap("a", singletonMap("b", new HashSet<>()))), Type.UNKNOWN_MAP_MAP);
        // Note we can only go as far as the first non-string map
        assertEquals(Type.getType(singletonMap(1, singletonMap(1, 1.0))), Type.UNKNOWN_MAP);
        assertEquals(Type.getType(singletonMap("a", emptyMap())), Type.UNKNOWN_MAP_MAP);

        Map<String, Map<String, Double>> data = new LinkedHashMap<>();
        data.put("a", emptyMap());
        data.put("b", singletonMap("a", null));
        Map<String, Double> nestedData = new LinkedHashMap<>();
        nestedData.put("a", null);
        nestedData.put(null, 1.0);
        data.put("c", nestedData);
        assertEquals(Type.getType(data), Type.UNKNOWN_MAP_MAP);
    }

    @Test
    public void testFromNullSafeCasting() {
        assertNull(Type.NULL.cast(null));
        assertNull(Type.UNKNOWN.cast(null));
        assertNull(Type.BOOLEAN.cast(null));
        assertNull(Type.INTEGER.cast(null));
        assertNull(Type.LONG.cast(null));
        assertNull(Type.FLOAT.cast(null));
        assertNull(Type.DOUBLE.cast(null));
        assertNull(Type.STRING.cast(null));
        assertNull(Type.BOOLEAN_MAP.cast(null));
        assertNull(Type.INTEGER_MAP.cast(null));
        assertNull(Type.LONG_MAP.cast(null));
        assertNull(Type.FLOAT_MAP.cast(null));
        assertNull(Type.DOUBLE_MAP.cast(null));
        assertNull(Type.STRING_MAP.cast(null));
        assertNull(Type.BOOLEAN_MAP_MAP.cast(null));
        assertNull(Type.INTEGER_MAP_MAP.cast(null));
        assertNull(Type.LONG_MAP_MAP.cast(null));
        assertNull(Type.FLOAT_MAP_MAP.cast(null));
        assertNull(Type.DOUBLE_MAP_MAP.cast(null));
        assertNull(Type.STRING_MAP_MAP.cast(null));
        assertNull(Type.BOOLEAN_LIST.cast(null));
        assertNull(Type.INTEGER_LIST.cast(null));
        assertNull(Type.LONG_LIST.cast(null));
        assertNull(Type.FLOAT_LIST.cast(null));
        assertNull(Type.DOUBLE_LIST.cast(null));
        assertNull(Type.STRING_LIST.cast(null));
        assertNull(Type.BOOLEAN_MAP_LIST.cast(null));
        assertNull(Type.INTEGER_MAP_LIST.cast(null));
        assertNull(Type.LONG_MAP_LIST.cast(null));
        assertNull(Type.FLOAT_MAP_LIST.cast(null));
        assertNull(Type.DOUBLE_MAP_LIST.cast(null));
        assertNull(Type.STRING_MAP_LIST.cast(null));
    }

    @Test
    public void testSafeCastingSameTypes() {
        assertEquals(Type.BOOLEAN.cast(false), false);
        assertEquals(Type.INTEGER.cast(1), 1);
        assertEquals(Type.LONG.cast(1L), 1L);
        assertEquals(Type.FLOAT.cast(1.0f), 1.0f);
        assertEquals(Type.DOUBLE.cast(1.0), 1.0);
        assertEquals(Type.STRING.cast("1"), "1");
        assertEquals(Type.BOOLEAN_MAP.cast(SIMPLE_BOOLEAN_MAP), SIMPLE_BOOLEAN_MAP);
        assertEquals(Type.INTEGER_MAP.cast(SIMPLE_INTEGER_MAP), SIMPLE_INTEGER_MAP);
        assertEquals(Type.LONG_MAP.cast(SIMPLE_LONG_MAP), SIMPLE_LONG_MAP);
        assertEquals(Type.FLOAT_MAP.cast(SIMPLE_FLOAT_MAP), SIMPLE_FLOAT_MAP);
        assertEquals(Type.DOUBLE_MAP.cast(SIMPLE_DOUBLE_MAP), SIMPLE_DOUBLE_MAP);
        assertEquals(Type.STRING_MAP.cast(SIMPLE_STRING_MAP), SIMPLE_STRING_MAP);
        assertEquals(Type.BOOLEAN_LIST.cast(SIMPLE_BOOLEAN_LIST), SIMPLE_BOOLEAN_LIST);
        assertEquals(Type.INTEGER_LIST.cast(SIMPLE_INTEGER_LIST), SIMPLE_INTEGER_LIST);
        assertEquals(Type.LONG_LIST.cast(SIMPLE_LONG_LIST), SIMPLE_LONG_LIST);
        assertEquals(Type.FLOAT_LIST.cast(SIMPLE_FLOAT_LIST), SIMPLE_FLOAT_LIST);
        assertEquals(Type.DOUBLE_LIST.cast(SIMPLE_DOUBLE_LIST), SIMPLE_DOUBLE_LIST);
        assertEquals(Type.STRING_LIST.cast(SIMPLE_STRING_LIST), SIMPLE_STRING_LIST);
        assertEquals(Type.BOOLEAN_MAP_MAP.cast(SIMPLE_BOOLEAN_MAP_MAP), SIMPLE_BOOLEAN_MAP_MAP);
        assertEquals(Type.INTEGER_MAP_MAP.cast(SIMPLE_INTEGER_MAP_MAP), SIMPLE_INTEGER_MAP_MAP);
        assertEquals(Type.LONG_MAP_MAP.cast(SIMPLE_LONG_MAP_MAP), SIMPLE_LONG_MAP_MAP);
        assertEquals(Type.FLOAT_MAP_MAP.cast(SIMPLE_FLOAT_MAP_MAP), SIMPLE_FLOAT_MAP_MAP);
        assertEquals(Type.DOUBLE_MAP_MAP.cast(SIMPLE_DOUBLE_MAP_MAP), SIMPLE_DOUBLE_MAP_MAP);
        assertEquals(Type.STRING_MAP_MAP.cast(SIMPLE_STRING_MAP_MAP), SIMPLE_STRING_MAP_MAP);
        assertEquals(Type.BOOLEAN_MAP_LIST.cast(SIMPLE_BOOLEAN_MAP_LIST), SIMPLE_BOOLEAN_MAP_LIST);
        assertEquals(Type.INTEGER_MAP_LIST.cast(SIMPLE_INTEGER_MAP_LIST), SIMPLE_INTEGER_MAP_LIST);
        assertEquals(Type.LONG_MAP_LIST.cast(SIMPLE_LONG_MAP_LIST), SIMPLE_LONG_MAP_LIST);
        assertEquals(Type.FLOAT_MAP_LIST.cast(SIMPLE_FLOAT_MAP_LIST), SIMPLE_FLOAT_MAP_LIST);
        assertEquals(Type.DOUBLE_MAP_LIST.cast(SIMPLE_DOUBLE_MAP_LIST), SIMPLE_DOUBLE_MAP_LIST);
        assertEquals(Type.STRING_MAP_LIST.cast(SIMPLE_STRING_MAP_LIST), SIMPLE_STRING_MAP_LIST);
    }

    @Test
    public void testPrimitivesToStringSafeCasting() {
        assertEquals(Type.STRING.cast(true), "true");
        assertEquals(Type.STRING.cast(1), "1");
        assertEquals(Type.STRING.cast(1L), "1");
        assertEquals(Type.STRING.cast(1.0f), "1.0");
        assertEquals(Type.STRING.cast(1.0), "1.0");
        assertEquals(Type.STRING.cast("foo"), "foo");
    }

    @Test
    public void testIntegerTypesToLongTypesSafeCasting() {
        assertEquals(Type.LONG.cast(1), 1L);
        assertEquals(Type.LONG_MAP.cast(SIMPLE_INTEGER_MAP), SIMPLE_LONG_MAP);
        assertEquals(Type.LONG_MAP_MAP.cast(SIMPLE_INTEGER_MAP_MAP), SIMPLE_LONG_MAP_MAP);
    }

    @Test
    public void testNumericTypesToDoubleTypesSafeCasting() {
        assertEquals(Type.DOUBLE.cast(1), 1.0);
        assertEquals(Type.DOUBLE.cast(1L), 1.0);
        assertEquals(Type.DOUBLE.cast(1.0f), 1.0);
        assertEquals(Type.DOUBLE.cast(1.0), 1.0);
        assertEquals(Type.DOUBLE_MAP.cast(SIMPLE_INTEGER_MAP), SIMPLE_DOUBLE_MAP);
        assertEquals(Type.DOUBLE_MAP.cast(SIMPLE_LONG_MAP), SIMPLE_DOUBLE_MAP);
        assertEquals(Type.DOUBLE_MAP.cast(SIMPLE_FLOAT_MAP), SIMPLE_DOUBLE_MAP);
        assertEquals(Type.DOUBLE_MAP_MAP.cast(SIMPLE_INTEGER_MAP_MAP), SIMPLE_DOUBLE_MAP_MAP);
        assertEquals(Type.DOUBLE_MAP_MAP.cast(SIMPLE_LONG_MAP_MAP), SIMPLE_DOUBLE_MAP_MAP);
        assertEquals(Type.DOUBLE_MAP_MAP.cast(SIMPLE_FLOAT_MAP_MAP), SIMPLE_DOUBLE_MAP_MAP);
        assertEquals(Type.DOUBLE_LIST.cast(SIMPLE_INTEGER_LIST), SIMPLE_DOUBLE_LIST);
        assertEquals(Type.DOUBLE_LIST.cast(SIMPLE_LONG_LIST), SIMPLE_DOUBLE_LIST);
        assertEquals(Type.DOUBLE_LIST.cast(SIMPLE_FLOAT_LIST), SIMPLE_DOUBLE_LIST);
        assertEquals(Type.DOUBLE_MAP_LIST.cast(SIMPLE_INTEGER_MAP_LIST), SIMPLE_DOUBLE_MAP_LIST);
        assertEquals(Type.DOUBLE_MAP_LIST.cast(SIMPLE_LONG_MAP_LIST), SIMPLE_DOUBLE_MAP_LIST);
        assertEquals(Type.DOUBLE_MAP_LIST.cast(SIMPLE_FLOAT_MAP_LIST), SIMPLE_DOUBLE_MAP_LIST);
    }

    @Test(expectedExceptions = ClassCastException.class, expectedExceptionsMessageRegExp = ".*Cannot cast non-null object.*")
    public void testToNullSafeCastingNonNullObject() {
        Type.NULL.cast("foo");
    }

    @Test(expectedExceptions = ClassCastException.class, expectedExceptionsMessageRegExp = ".*Cannot cast non-null object.*")
    public void testToUnknownSafeCasting() {
        Type.UNKNOWN.cast("foo");
    }

    @Test
    public void testFailSafeCasting() {
        assertException(() -> Type.INTEGER.cast(true), ".*Cannot safely cast.*");
        assertException(() -> Type.INTEGER.cast(1L), ".*Cannot safely cast.*");
        assertException(() -> Type.INTEGER.cast(1.0f), ".*Cannot safely cast.*");
        assertException(() -> Type.INTEGER.cast(1.0), ".*Cannot safely cast.*");
        assertException(() -> Type.FLOAT.cast(false), ".*Cannot safely cast.*");
        assertException(() -> Type.FLOAT.cast(1L), ".*Cannot safely cast.*");
        assertException(() -> Type.FLOAT.cast(1.0), ".*Cannot safely cast.*");
        assertException(() -> Type.LONG.cast(true), ".*Cannot safely cast.*");
        assertException(() -> Type.LONG.cast(1.0), ".*Cannot safely cast.*");
        assertException(() -> Type.LONG.cast(1.0f), ".*Cannot safely cast.*");
        assertException(() -> Type.STRING.cast(SIMPLE_STRING_LIST), ".*Cannot safely cast.*");
        assertException(() -> Type.STRING.cast(SIMPLE_BOOLEAN_MAP), ".*Cannot safely cast.*");
        assertException(() -> Type.STRING_MAP.cast("foo"), ".*Cannot safely cast.*");
        assertException(() -> Type.INTEGER_LIST.cast(list(singletonList(1L))), ".*Cannot safely cast.*");
        assertException(() -> Type.BOOLEAN_MAP_LIST.cast(nestedList(singletonList(singletonMap("a", 1L)))), ".*Cannot safely cast.*");
    }

    @Test
    public void testFromNullForceCasting() {
        for (Type type : Type.values()) {
            assertNull(type.forceCast(type, null));
        }
    }

    @Test
    public void testForceCastingSameTypes() {
        assertEquals(Type.BOOLEAN.forceCast(Type.BOOLEAN, false), false);
        assertEquals(Type.INTEGER.forceCast(Type.INTEGER, 1), 1);
        assertEquals(Type.LONG.forceCast(Type.LONG, 1L), 1L);
        assertEquals(Type.FLOAT.forceCast(Type.FLOAT, 1.0f), 1.0f);
        assertEquals(Type.DOUBLE.forceCast(Type.DOUBLE, 1.0), 1.0);
        assertEquals(Type.STRING.forceCast(Type.STRING, "1"), "1");
        assertEquals(Type.BOOLEAN_MAP.forceCast(Type.BOOLEAN_MAP, SIMPLE_BOOLEAN_MAP), SIMPLE_BOOLEAN_MAP);
        assertEquals(Type.INTEGER_MAP.forceCast(Type.INTEGER_MAP, SIMPLE_INTEGER_MAP), SIMPLE_INTEGER_MAP);
        assertEquals(Type.LONG_MAP.forceCast(Type.LONG_MAP, SIMPLE_LONG_MAP), SIMPLE_LONG_MAP);
        assertEquals(Type.FLOAT_MAP.forceCast(Type.FLOAT_MAP, SIMPLE_FLOAT_MAP), SIMPLE_FLOAT_MAP);
        assertEquals(Type.DOUBLE_MAP.forceCast(Type.DOUBLE_MAP, SIMPLE_DOUBLE_MAP), SIMPLE_DOUBLE_MAP);
        assertEquals(Type.STRING_MAP.forceCast(Type.STRING_MAP, SIMPLE_STRING_MAP), SIMPLE_STRING_MAP);
        assertEquals(Type.BOOLEAN_LIST.forceCast(Type.BOOLEAN_LIST, SIMPLE_BOOLEAN_LIST), SIMPLE_BOOLEAN_LIST);
        assertEquals(Type.INTEGER_LIST.forceCast(Type.INTEGER_LIST, SIMPLE_INTEGER_LIST), SIMPLE_INTEGER_LIST);
        assertEquals(Type.LONG_LIST.forceCast(Type.LONG_LIST, SIMPLE_LONG_LIST), SIMPLE_LONG_LIST);
        assertEquals(Type.FLOAT_LIST.forceCast(Type.FLOAT_LIST, SIMPLE_FLOAT_LIST), SIMPLE_FLOAT_LIST);
        assertEquals(Type.DOUBLE_LIST.forceCast(Type.DOUBLE_LIST, SIMPLE_DOUBLE_LIST), SIMPLE_DOUBLE_LIST);
        assertEquals(Type.STRING_LIST.forceCast(Type.STRING_LIST, SIMPLE_STRING_LIST), SIMPLE_STRING_LIST);
        assertEquals(Type.BOOLEAN_MAP_MAP.forceCast(Type.BOOLEAN_MAP_MAP, SIMPLE_BOOLEAN_MAP_MAP), SIMPLE_BOOLEAN_MAP_MAP);
        assertEquals(Type.INTEGER_MAP_MAP.forceCast(Type.INTEGER_MAP_MAP, SIMPLE_INTEGER_MAP_MAP), SIMPLE_INTEGER_MAP_MAP);
        assertEquals(Type.LONG_MAP_MAP.forceCast(Type.LONG_MAP_MAP, SIMPLE_LONG_MAP_MAP), SIMPLE_LONG_MAP_MAP);
        assertEquals(Type.FLOAT_MAP_MAP.forceCast(Type.FLOAT_MAP_MAP, SIMPLE_FLOAT_MAP_MAP), SIMPLE_FLOAT_MAP_MAP);
        assertEquals(Type.DOUBLE_MAP_MAP.forceCast(Type.DOUBLE_MAP_MAP, SIMPLE_DOUBLE_MAP_MAP), SIMPLE_DOUBLE_MAP_MAP);
        assertEquals(Type.STRING_MAP_MAP.forceCast(Type.STRING_MAP_MAP, SIMPLE_STRING_MAP_MAP), SIMPLE_STRING_MAP_MAP);
        assertEquals(Type.BOOLEAN_MAP_LIST.forceCast(Type.BOOLEAN_MAP_LIST, SIMPLE_BOOLEAN_MAP_LIST), SIMPLE_BOOLEAN_MAP_LIST);
        assertEquals(Type.INTEGER_MAP_LIST.forceCast(Type.INTEGER_MAP_LIST, SIMPLE_INTEGER_MAP_LIST), SIMPLE_INTEGER_MAP_LIST);
        assertEquals(Type.LONG_MAP_LIST.forceCast(Type.LONG_MAP_LIST, SIMPLE_LONG_MAP_LIST), SIMPLE_LONG_MAP_LIST);
        assertEquals(Type.FLOAT_MAP_LIST.forceCast(Type.FLOAT_MAP_LIST, SIMPLE_FLOAT_MAP_LIST), SIMPLE_FLOAT_MAP_LIST);
        assertEquals(Type.DOUBLE_MAP_LIST.forceCast(Type.DOUBLE_MAP_LIST, SIMPLE_DOUBLE_MAP_LIST), SIMPLE_DOUBLE_MAP_LIST);
        assertEquals(Type.STRING_MAP_LIST.forceCast(Type.STRING_MAP_LIST, SIMPLE_STRING_MAP_LIST), SIMPLE_STRING_MAP_LIST);
    }

    @Test
    public void testForceCastFromIntegerTypes() {
        assertEquals(Type.INTEGER.forceCast(Type.LONG, 1), 1L);
        assertEquals(Type.INTEGER.forceCast(Type.FLOAT, 3), 3.0f);
        assertEquals(Type.INTEGER.forceCast(Type.DOUBLE, 3), 3.0);
        assertEquals(Type.INTEGER.forceCast(Type.STRING, 3), "3");
        assertEquals(Type.INTEGER.forceCast(Type.BOOLEAN, 3), true);
        assertEquals(Type.INTEGER.forceCast(Type.BOOLEAN, 0), false);
        assertEquals(Type.INTEGER_MAP.forceCast(Type.LONG_MAP, SIMPLE_INTEGER_MAP), SIMPLE_LONG_MAP);
        assertEquals(Type.INTEGER_MAP.forceCast(Type.FLOAT_MAP, SIMPLE_INTEGER_MAP), SIMPLE_FLOAT_MAP);
        assertEquals(Type.INTEGER_MAP.forceCast(Type.DOUBLE_MAP, SIMPLE_INTEGER_MAP), SIMPLE_DOUBLE_MAP);
        assertEquals(Type.INTEGER_MAP.forceCast(Type.STRING_MAP, SIMPLE_INTEGER_MAP), SIMPLE_STRING_MAP);
        assertEquals(Type.INTEGER_MAP.forceCast(Type.BOOLEAN_MAP, SIMPLE_INTEGER_MAP), SIMPLE_BOOLEAN_MAP);
        assertEquals(Type.INTEGER_MAP_MAP.forceCast(Type.LONG_MAP_MAP, SIMPLE_INTEGER_MAP_MAP), SIMPLE_LONG_MAP_MAP);
        assertEquals(Type.INTEGER_MAP_MAP.forceCast(Type.FLOAT_MAP_MAP, SIMPLE_INTEGER_MAP_MAP), SIMPLE_FLOAT_MAP_MAP);
        assertEquals(Type.INTEGER_MAP_MAP.forceCast(Type.DOUBLE_MAP_MAP, SIMPLE_INTEGER_MAP_MAP), SIMPLE_DOUBLE_MAP_MAP);
        assertEquals(Type.INTEGER_MAP_MAP.forceCast(Type.STRING_MAP_MAP, SIMPLE_INTEGER_MAP_MAP), SIMPLE_STRING_MAP_MAP);
        assertEquals(Type.INTEGER_MAP_MAP.forceCast(Type.BOOLEAN_MAP_MAP, SIMPLE_INTEGER_MAP_MAP), SIMPLE_BOOLEAN_MAP_MAP);
        assertEquals(Type.INTEGER_LIST.forceCast(Type.LONG_LIST, SIMPLE_INTEGER_LIST), SIMPLE_LONG_LIST);
        assertEquals(Type.INTEGER_LIST.forceCast(Type.FLOAT_LIST, SIMPLE_INTEGER_LIST), SIMPLE_FLOAT_LIST);
        assertEquals(Type.INTEGER_LIST.forceCast(Type.DOUBLE_LIST, SIMPLE_INTEGER_LIST), SIMPLE_DOUBLE_LIST);
        assertEquals(Type.INTEGER_LIST.forceCast(Type.STRING_LIST, SIMPLE_INTEGER_LIST), SIMPLE_STRING_LIST);
        assertEquals(Type.INTEGER_LIST.forceCast(Type.BOOLEAN_LIST, SIMPLE_INTEGER_LIST), SIMPLE_BOOLEAN_LIST);
        assertEquals(Type.INTEGER_MAP_LIST.forceCast(Type.LONG_MAP_LIST, SIMPLE_INTEGER_MAP_LIST), SIMPLE_LONG_MAP_LIST);
        assertEquals(Type.INTEGER_MAP_LIST.forceCast(Type.FLOAT_MAP_LIST, SIMPLE_INTEGER_MAP_LIST), SIMPLE_FLOAT_MAP_LIST);
        assertEquals(Type.INTEGER_MAP_LIST.forceCast(Type.DOUBLE_MAP_LIST, SIMPLE_INTEGER_MAP_LIST), SIMPLE_DOUBLE_MAP_LIST);
        assertEquals(Type.INTEGER_MAP_LIST.forceCast(Type.STRING_MAP_LIST, SIMPLE_INTEGER_MAP_LIST), SIMPLE_STRING_MAP_LIST);
        assertEquals(Type.INTEGER_MAP_LIST.forceCast(Type.BOOLEAN_MAP_LIST, SIMPLE_INTEGER_MAP_LIST), SIMPLE_BOOLEAN_MAP_LIST);
    }

    @Test
    public void testForceCastFromLongTypes() {
        assertEquals(Type.LONG.forceCast(Type.INTEGER, Long.MAX_VALUE), -1);
        assertEquals(Type.LONG.forceCast(Type.FLOAT, 3L), 3.0f);
        assertEquals(Type.LONG.forceCast(Type.DOUBLE, 3L), 3.0);
        assertEquals(Type.LONG.forceCast(Type.STRING, 3L), "3");
        assertEquals(Type.LONG.forceCast(Type.BOOLEAN, 3L), true);
        assertEquals(Type.LONG.forceCast(Type.BOOLEAN, 0L), false);
        assertEquals(Type.LONG_MAP.forceCast(Type.INTEGER_MAP, SIMPLE_LONG_MAP), SIMPLE_INTEGER_MAP);
        assertEquals(Type.LONG_MAP.forceCast(Type.FLOAT_MAP, SIMPLE_LONG_MAP), SIMPLE_FLOAT_MAP);
        assertEquals(Type.LONG_MAP.forceCast(Type.DOUBLE_MAP, SIMPLE_LONG_MAP), SIMPLE_DOUBLE_MAP);
        assertEquals(Type.LONG_MAP.forceCast(Type.STRING_MAP, SIMPLE_LONG_MAP), SIMPLE_STRING_MAP);
        assertEquals(Type.LONG_MAP.forceCast(Type.BOOLEAN_MAP, SIMPLE_LONG_MAP), SIMPLE_BOOLEAN_MAP);
        assertEquals(Type.LONG_MAP_MAP.forceCast(Type.INTEGER_MAP_MAP, SIMPLE_LONG_MAP_MAP), SIMPLE_INTEGER_MAP_MAP);
        assertEquals(Type.LONG_MAP_MAP.forceCast(Type.FLOAT_MAP_MAP, SIMPLE_LONG_MAP_MAP), SIMPLE_FLOAT_MAP_MAP);
        assertEquals(Type.LONG_MAP_MAP.forceCast(Type.DOUBLE_MAP_MAP, SIMPLE_LONG_MAP_MAP), SIMPLE_DOUBLE_MAP_MAP);
        assertEquals(Type.LONG_MAP_MAP.forceCast(Type.STRING_MAP_MAP, SIMPLE_LONG_MAP_MAP), SIMPLE_STRING_MAP_MAP);
        assertEquals(Type.LONG_MAP_MAP.forceCast(Type.BOOLEAN_MAP_MAP, SIMPLE_LONG_MAP_MAP), SIMPLE_BOOLEAN_MAP_MAP);
        assertEquals(Type.LONG_LIST.forceCast(Type.INTEGER_LIST, SIMPLE_LONG_LIST), SIMPLE_INTEGER_LIST);
        assertEquals(Type.LONG_LIST.forceCast(Type.FLOAT_LIST, SIMPLE_LONG_LIST), SIMPLE_FLOAT_LIST);
        assertEquals(Type.LONG_LIST.forceCast(Type.DOUBLE_LIST, SIMPLE_LONG_LIST), SIMPLE_DOUBLE_LIST);
        assertEquals(Type.LONG_LIST.forceCast(Type.STRING_LIST, SIMPLE_LONG_LIST), SIMPLE_STRING_LIST);
        assertEquals(Type.LONG_LIST.forceCast(Type.BOOLEAN_LIST, SIMPLE_LONG_LIST), SIMPLE_BOOLEAN_LIST);
        assertEquals(Type.LONG_MAP_LIST.forceCast(Type.INTEGER_MAP_LIST, SIMPLE_LONG_MAP_LIST), SIMPLE_INTEGER_MAP_LIST);
        assertEquals(Type.LONG_MAP_LIST.forceCast(Type.FLOAT_MAP_LIST, SIMPLE_LONG_MAP_LIST), SIMPLE_FLOAT_MAP_LIST);
        assertEquals(Type.LONG_MAP_LIST.forceCast(Type.DOUBLE_MAP_LIST, SIMPLE_LONG_MAP_LIST), SIMPLE_DOUBLE_MAP_LIST);
        assertEquals(Type.LONG_MAP_LIST.forceCast(Type.STRING_MAP_LIST, SIMPLE_LONG_MAP_LIST), SIMPLE_STRING_MAP_LIST);
        assertEquals(Type.LONG_MAP_LIST.forceCast(Type.BOOLEAN_MAP_LIST, SIMPLE_LONG_MAP_LIST), SIMPLE_BOOLEAN_MAP_LIST);
    }

    @Test
    public void testForceCastFromFloatTypes() {
        assertEquals(Type.FLOAT.forceCast(Type.INTEGER, 3.2f), 3);
        assertEquals(Type.FLOAT.forceCast(Type.LONG, 3.2f), 3L);
        Object object = Type.FLOAT.forceCast(Type.DOUBLE, 3.2f);
        assertTrue(Math.abs((Double) object - 3.2) <= 1e-07);
        assertEquals(Type.FLOAT.forceCast(Type.STRING, 3.2f), "3.2");
        assertEquals(Type.FLOAT.forceCast(Type.BOOLEAN, 3.2f), true);
        assertEquals(Type.FLOAT.forceCast(Type.BOOLEAN, 0.0f), false);
        assertEquals(Type.FLOAT_MAP.forceCast(Type.INTEGER_MAP, SIMPLE_FLOAT_MAP), SIMPLE_INTEGER_MAP);
        assertEquals(Type.FLOAT_MAP.forceCast(Type.LONG_MAP, SIMPLE_FLOAT_MAP), SIMPLE_LONG_MAP);
        assertEquals(Type.FLOAT_MAP.forceCast(Type.DOUBLE_MAP, SIMPLE_FLOAT_MAP), SIMPLE_DOUBLE_MAP);
        assertEquals(Type.FLOAT_MAP.forceCast(Type.STRING_MAP, SIMPLE_FLOAT_MAP), singletonMap("a", "1.0"));
        assertEquals(Type.FLOAT_MAP.forceCast(Type.BOOLEAN_MAP, SIMPLE_FLOAT_MAP), SIMPLE_BOOLEAN_MAP);
        assertEquals(Type.FLOAT_MAP_MAP.forceCast(Type.INTEGER_MAP_MAP, SIMPLE_FLOAT_MAP_MAP), SIMPLE_INTEGER_MAP_MAP);
        assertEquals(Type.FLOAT_MAP_MAP.forceCast(Type.LONG_MAP_MAP, SIMPLE_FLOAT_MAP_MAP), SIMPLE_LONG_MAP_MAP);
        assertEquals(Type.FLOAT_MAP_MAP.forceCast(Type.DOUBLE_MAP_MAP, SIMPLE_FLOAT_MAP_MAP), SIMPLE_DOUBLE_MAP_MAP);
        assertEquals(Type.FLOAT_MAP_MAP.forceCast(Type.STRING_MAP_MAP, SIMPLE_FLOAT_MAP_MAP), singletonMap("a", singletonMap("a", "1.0")));
        assertEquals(Type.FLOAT_MAP_MAP.forceCast(Type.BOOLEAN_MAP_MAP, SIMPLE_FLOAT_MAP_MAP), SIMPLE_BOOLEAN_MAP_MAP);
        assertEquals(Type.FLOAT_LIST.forceCast(Type.INTEGER_LIST, SIMPLE_FLOAT_LIST), SIMPLE_INTEGER_LIST);
        assertEquals(Type.FLOAT_LIST.forceCast(Type.LONG_LIST, SIMPLE_FLOAT_LIST), SIMPLE_LONG_LIST);
        assertEquals(Type.FLOAT_LIST.forceCast(Type.DOUBLE_LIST, SIMPLE_FLOAT_LIST), SIMPLE_DOUBLE_LIST);
        assertEquals(Type.FLOAT_LIST.forceCast(Type.STRING_LIST, SIMPLE_FLOAT_LIST), singletonList("1.0"));
        assertEquals(Type.FLOAT_LIST.forceCast(Type.BOOLEAN_LIST, SIMPLE_FLOAT_LIST), SIMPLE_BOOLEAN_LIST);
        assertEquals(Type.FLOAT_MAP_LIST.forceCast(Type.INTEGER_MAP_LIST, SIMPLE_FLOAT_MAP_LIST), SIMPLE_INTEGER_MAP_LIST);
        assertEquals(Type.FLOAT_MAP_LIST.forceCast(Type.LONG_MAP_LIST, SIMPLE_FLOAT_MAP_LIST), SIMPLE_LONG_MAP_LIST);
        assertEquals(Type.FLOAT_MAP_LIST.forceCast(Type.DOUBLE_MAP_LIST, SIMPLE_FLOAT_MAP_LIST), SIMPLE_DOUBLE_MAP_LIST);
        assertEquals(Type.FLOAT_MAP_LIST.forceCast(Type.STRING_MAP_LIST, SIMPLE_FLOAT_MAP_LIST), singletonList(singletonMap("a", "1.0")));
        assertEquals(Type.FLOAT_MAP_LIST.forceCast(Type.BOOLEAN_MAP_LIST, SIMPLE_FLOAT_MAP_LIST), SIMPLE_BOOLEAN_MAP_LIST);
    }

    @Test
    public void testForceCastFromDoubleTypes() {
        assertEquals(Type.DOUBLE.forceCast(Type.INTEGER, 3.2), 3);
        assertEquals(Type.DOUBLE.forceCast(Type.LONG, 3.2), 3L);
        Object object = Type.DOUBLE.forceCast(Type.FLOAT, 3.2);
        assertTrue(Math.abs((Float) object - 3.2) <= 1e-07);
        assertEquals(Type.DOUBLE.forceCast(Type.STRING, 3.2), "3.2");
        assertEquals(Type.DOUBLE.forceCast(Type.BOOLEAN, 3.2), true);
        assertEquals(Type.DOUBLE.forceCast(Type.BOOLEAN, 0.0), false);
        assertEquals(Type.DOUBLE_MAP.forceCast(Type.INTEGER_MAP, SIMPLE_DOUBLE_MAP), SIMPLE_INTEGER_MAP);
        assertEquals(Type.DOUBLE_MAP.forceCast(Type.LONG_MAP, SIMPLE_DOUBLE_MAP), SIMPLE_LONG_MAP);
        assertEquals(Type.DOUBLE_MAP.forceCast(Type.FLOAT_MAP, SIMPLE_DOUBLE_MAP), SIMPLE_FLOAT_MAP);
        assertEquals(Type.DOUBLE_MAP.forceCast(Type.STRING_MAP, SIMPLE_DOUBLE_MAP), singletonMap("a", "1.0"));
        assertEquals(Type.DOUBLE_MAP.forceCast(Type.BOOLEAN_MAP, SIMPLE_DOUBLE_MAP), SIMPLE_BOOLEAN_MAP);
        assertEquals(Type.DOUBLE_MAP_MAP.forceCast(Type.INTEGER_MAP_MAP, SIMPLE_DOUBLE_MAP_MAP), SIMPLE_INTEGER_MAP_MAP);
        assertEquals(Type.DOUBLE_MAP_MAP.forceCast(Type.LONG_MAP_MAP, SIMPLE_DOUBLE_MAP_MAP), SIMPLE_LONG_MAP_MAP);
        assertEquals(Type.DOUBLE_MAP_MAP.forceCast(Type.FLOAT_MAP_MAP, SIMPLE_DOUBLE_MAP_MAP), SIMPLE_FLOAT_MAP_MAP);
        assertEquals(Type.DOUBLE_MAP_MAP.forceCast(Type.STRING_MAP_MAP, SIMPLE_DOUBLE_MAP_MAP), singletonMap("a", singletonMap("a", "1.0")));
        assertEquals(Type.DOUBLE_MAP_MAP.forceCast(Type.BOOLEAN_MAP_MAP, SIMPLE_DOUBLE_MAP_MAP), SIMPLE_BOOLEAN_MAP_MAP);
        assertEquals(Type.DOUBLE_LIST.forceCast(Type.INTEGER_LIST, SIMPLE_DOUBLE_LIST), SIMPLE_INTEGER_LIST);
        assertEquals(Type.DOUBLE_LIST.forceCast(Type.LONG_LIST, SIMPLE_DOUBLE_LIST), SIMPLE_LONG_LIST);
        assertEquals(Type.DOUBLE_LIST.forceCast(Type.FLOAT_LIST, SIMPLE_DOUBLE_LIST), SIMPLE_FLOAT_LIST);
        assertEquals(Type.DOUBLE_LIST.forceCast(Type.STRING_LIST, SIMPLE_DOUBLE_LIST), singletonList("1.0"));
        assertEquals(Type.DOUBLE_LIST.forceCast(Type.BOOLEAN_LIST, SIMPLE_DOUBLE_LIST), SIMPLE_BOOLEAN_LIST);
        assertEquals(Type.DOUBLE_MAP_LIST.forceCast(Type.INTEGER_MAP_LIST, SIMPLE_DOUBLE_MAP_LIST), SIMPLE_INTEGER_MAP_LIST);
        assertEquals(Type.DOUBLE_MAP_LIST.forceCast(Type.LONG_MAP_LIST, SIMPLE_DOUBLE_MAP_LIST), SIMPLE_LONG_MAP_LIST);
        assertEquals(Type.DOUBLE_MAP_LIST.forceCast(Type.FLOAT_MAP_LIST, SIMPLE_DOUBLE_MAP_LIST), SIMPLE_FLOAT_MAP_LIST);
        assertEquals(Type.DOUBLE_MAP_LIST.forceCast(Type.STRING_MAP_LIST, SIMPLE_DOUBLE_MAP_LIST), singletonList(singletonMap("a", "1.0")));
        assertEquals(Type.DOUBLE_MAP_LIST.forceCast(Type.BOOLEAN_MAP_LIST, SIMPLE_DOUBLE_MAP_LIST), SIMPLE_BOOLEAN_MAP_LIST);
    }

    @Test
    public void testForceCastFromBooleanTypes() {
        assertEquals(Type.BOOLEAN.forceCast(Type.INTEGER, true), 1);
        assertEquals(Type.BOOLEAN.forceCast(Type.INTEGER, false), 0);
        assertEquals(Type.BOOLEAN.forceCast(Type.FLOAT, true), 1.0f);
        assertEquals(Type.BOOLEAN.forceCast(Type.FLOAT, false), 0.0f);
        assertEquals(Type.BOOLEAN.forceCast(Type.DOUBLE, true), 1.0);
        assertEquals(Type.BOOLEAN.forceCast(Type.DOUBLE, false), 0.0);
        assertEquals(Type.BOOLEAN.forceCast(Type.STRING, true), "true");
        assertEquals(Type.BOOLEAN.forceCast(Type.STRING, false), "false");
        assertEquals(Type.BOOLEAN_MAP.forceCast(Type.INTEGER_MAP, SIMPLE_BOOLEAN_MAP), SIMPLE_INTEGER_MAP);
        assertEquals(Type.BOOLEAN_MAP.forceCast(Type.LONG_MAP, SIMPLE_BOOLEAN_MAP), SIMPLE_LONG_MAP);
        assertEquals(Type.BOOLEAN_MAP.forceCast(Type.FLOAT_MAP, SIMPLE_BOOLEAN_MAP), SIMPLE_FLOAT_MAP);
        assertEquals(Type.BOOLEAN_MAP.forceCast(Type.STRING_MAP, SIMPLE_BOOLEAN_MAP), singletonMap("a", "true"));
        assertEquals(Type.BOOLEAN_MAP.forceCast(Type.DOUBLE_MAP, SIMPLE_BOOLEAN_MAP), SIMPLE_DOUBLE_MAP);
        assertEquals(Type.BOOLEAN_MAP_MAP.forceCast(Type.INTEGER_MAP_MAP, SIMPLE_BOOLEAN_MAP_MAP), SIMPLE_INTEGER_MAP_MAP);
        assertEquals(Type.BOOLEAN_MAP_MAP.forceCast(Type.LONG_MAP_MAP, SIMPLE_BOOLEAN_MAP_MAP), SIMPLE_LONG_MAP_MAP);
        assertEquals(Type.BOOLEAN_MAP_MAP.forceCast(Type.FLOAT_MAP_MAP, SIMPLE_BOOLEAN_MAP_MAP), SIMPLE_FLOAT_MAP_MAP);
        assertEquals(Type.BOOLEAN_MAP_MAP.forceCast(Type.DOUBLE_MAP_MAP, SIMPLE_BOOLEAN_MAP_MAP), SIMPLE_DOUBLE_MAP_MAP);
        assertEquals(Type.BOOLEAN_MAP_MAP.forceCast(Type.STRING_MAP_MAP, SIMPLE_BOOLEAN_MAP_MAP), singletonMap("a", singletonMap("a", "true")));
        assertEquals(Type.BOOLEAN_LIST.forceCast(Type.INTEGER_LIST, SIMPLE_BOOLEAN_LIST), SIMPLE_INTEGER_LIST);
        assertEquals(Type.BOOLEAN_LIST.forceCast(Type.LONG_LIST, SIMPLE_BOOLEAN_LIST), SIMPLE_LONG_LIST);
        assertEquals(Type.BOOLEAN_LIST.forceCast(Type.FLOAT_LIST, SIMPLE_BOOLEAN_LIST), SIMPLE_FLOAT_LIST);
        assertEquals(Type.BOOLEAN_LIST.forceCast(Type.DOUBLE_LIST, SIMPLE_BOOLEAN_LIST), SIMPLE_DOUBLE_LIST);
        assertEquals(Type.BOOLEAN_LIST.forceCast(Type.STRING_LIST, SIMPLE_BOOLEAN_LIST), singletonList("true"));
        assertEquals(Type.BOOLEAN_MAP_LIST.forceCast(Type.INTEGER_MAP_LIST, SIMPLE_BOOLEAN_MAP_LIST), SIMPLE_INTEGER_MAP_LIST);
        assertEquals(Type.BOOLEAN_MAP_LIST.forceCast(Type.LONG_MAP_LIST, SIMPLE_BOOLEAN_MAP_LIST), SIMPLE_LONG_MAP_LIST);
        assertEquals(Type.BOOLEAN_MAP_LIST.forceCast(Type.FLOAT_MAP_LIST, SIMPLE_BOOLEAN_MAP_LIST), SIMPLE_FLOAT_MAP_LIST);
        assertEquals(Type.BOOLEAN_MAP_LIST.forceCast(Type.DOUBLE_MAP_LIST, SIMPLE_BOOLEAN_MAP_LIST), SIMPLE_DOUBLE_MAP_LIST);
        assertEquals(Type.BOOLEAN_MAP_LIST.forceCast(Type.STRING_MAP_LIST, SIMPLE_BOOLEAN_MAP_LIST), singletonList(singletonMap("a", "true")));
    }

    @Test
    public void testFromStringForceCast() {
        assertEquals(Type.STRING.forceCast(Type.INTEGER, "3"), 3);
        assertEquals(Type.STRING.forceCast(Type.LONG, "3"), 3L);
        assertEquals(Type.STRING.forceCast(Type.FLOAT, "3.2"), 3.2f);
        assertEquals(Type.STRING.forceCast(Type.DOUBLE, "3.2"), 3.2);
        assertEquals(Type.STRING_MAP.forceCast(Type.INTEGER_MAP, SIMPLE_STRING_MAP), SIMPLE_INTEGER_MAP);
        assertEquals(Type.STRING_MAP.forceCast(Type.LONG_MAP, SIMPLE_STRING_MAP), SIMPLE_LONG_MAP);
        assertEquals(Type.STRING_MAP.forceCast(Type.FLOAT_MAP, SIMPLE_STRING_MAP), SIMPLE_FLOAT_MAP);
        assertEquals(Type.STRING_MAP.forceCast(Type.DOUBLE_MAP, SIMPLE_STRING_MAP), SIMPLE_DOUBLE_MAP);
        assertEquals(Type.STRING_MAP.forceCast(Type.DOUBLE_MAP, map(singletonMap(null, "1"))), singletonMap(null, 1.0));
        // Booleans parse only "true" as true. Everything else is false
        assertEquals(Type.STRING_MAP.forceCast(Type.BOOLEAN_MAP, SIMPLE_STRING_MAP), singletonMap("a", false));
        assertEquals(Type.STRING_MAP_MAP.forceCast(Type.INTEGER_MAP_MAP, SIMPLE_STRING_MAP_MAP), SIMPLE_INTEGER_MAP_MAP);
        assertEquals(Type.STRING_MAP_MAP.forceCast(Type.LONG_MAP_MAP, SIMPLE_STRING_MAP_MAP), SIMPLE_LONG_MAP_MAP);
        assertEquals(Type.STRING_MAP_MAP.forceCast(Type.FLOAT_MAP_MAP, SIMPLE_STRING_MAP_MAP), SIMPLE_FLOAT_MAP_MAP);
        assertEquals(Type.STRING_MAP_MAP.forceCast(Type.DOUBLE_MAP_MAP, SIMPLE_STRING_MAP_MAP), SIMPLE_DOUBLE_MAP_MAP);
        assertEquals(Type.STRING_MAP_MAP.forceCast(Type.BOOLEAN_MAP_MAP, SIMPLE_STRING_MAP_MAP), singletonMap("a", singletonMap("a", false)));
        assertEquals(Type.STRING_LIST.forceCast(Type.INTEGER_LIST, SIMPLE_STRING_LIST), SIMPLE_INTEGER_LIST);
        assertEquals(Type.STRING_LIST.forceCast(Type.LONG_LIST, SIMPLE_STRING_LIST), SIMPLE_LONG_LIST);
        assertEquals(Type.STRING_LIST.forceCast(Type.FLOAT_LIST, SIMPLE_STRING_LIST), SIMPLE_FLOAT_LIST);
        assertEquals(Type.STRING_LIST.forceCast(Type.DOUBLE_LIST, SIMPLE_STRING_LIST), SIMPLE_DOUBLE_LIST);
        assertEquals(Type.STRING_LIST.forceCast(Type.BOOLEAN_LIST, SIMPLE_STRING_LIST), singletonList(false));
        assertEquals(Type.STRING_MAP_LIST.forceCast(Type.INTEGER_MAP_LIST, SIMPLE_STRING_MAP_LIST), SIMPLE_INTEGER_MAP_LIST);
        assertEquals(Type.STRING_MAP_LIST.forceCast(Type.LONG_MAP_LIST, SIMPLE_STRING_MAP_LIST), SIMPLE_LONG_MAP_LIST);
        assertEquals(Type.STRING_MAP_LIST.forceCast(Type.FLOAT_MAP_LIST, SIMPLE_STRING_MAP_LIST), SIMPLE_FLOAT_MAP_LIST);
        assertEquals(Type.STRING_MAP_LIST.forceCast(Type.DOUBLE_MAP_LIST, SIMPLE_STRING_MAP_LIST), SIMPLE_DOUBLE_MAP_LIST);
        assertEquals(Type.STRING_MAP_LIST.forceCast(Type.BOOLEAN_MAP_LIST, SIMPLE_STRING_MAP_LIST), singletonList(singletonMap("a", false)));
    }

    @Test
    public void testTypeClassification() {
        assertTrue(Type.isUnknown(Type.UNKNOWN));
        assertFalse(Type.isNull(Type.UNKNOWN));
        assertFalse(Type.isPrimitive(Type.UNKNOWN));
        assertFalse(Type.isNumeric(Type.UNKNOWN));
        assertFalse(Type.isList(Type.UNKNOWN));
        assertFalse(Type.isMap(Type.UNKNOWN));

        assertFalse(Type.isUnknown(Type.NULL));
        assertTrue(Type.isNull(Type.NULL));
        assertFalse(Type.isPrimitive(Type.NULL));
        assertFalse(Type.isNumeric(Type.NULL));
        assertFalse(Type.isList(Type.NULL));
        assertFalse(Type.isMap(Type.NULL));

        assertTrue(Type.isNumeric(Type.LONG));
        assertTrue(Type.isNumeric(Type.INTEGER));
        assertTrue(Type.isNumeric(Type.DOUBLE));
        assertTrue(Type.isNumeric(Type.FLOAT));
        assertFalse(Type.isNumeric(Type.BOOLEAN));

        assertFalse(Type.isUnknown(Type.LONG));
        assertFalse(Type.isNull(Type.LONG));
        assertTrue(Type.isPrimitive(Type.LONG));
        assertFalse(Type.isList(Type.LONG));
        assertFalse(Type.isMap(Type.LONG));

        assertFalse(Type.isUnknown(Type.FLOAT_MAP));
        assertFalse(Type.isNull(Type.FLOAT_MAP));
        assertFalse(Type.isPrimitive(Type.FLOAT_MAP));
        assertFalse(Type.isNumeric(Type.FLOAT_MAP));
        assertFalse(Type.isList(Type.FLOAT_MAP));
        assertTrue(Type.isMap(Type.FLOAT_MAP));
        assertTrue(Type.isPrimitiveMap(Type.FLOAT_MAP));
        assertFalse(Type.isComplexMap(Type.FLOAT_MAP));

        assertFalse(Type.isUnknown(Type.DOUBLE_LIST));
        assertFalse(Type.isNull(Type.DOUBLE_LIST));
        assertFalse(Type.isPrimitive(Type.DOUBLE_LIST));
        assertFalse(Type.isNumeric(Type.DOUBLE_LIST));
        assertFalse(Type.isMap(Type.DOUBLE_LIST));
        assertTrue(Type.isList(Type.DOUBLE_LIST));
        assertTrue(Type.isPrimitiveList(Type.DOUBLE_LIST));
        assertFalse(Type.isComplexList(Type.DOUBLE_LIST));

        assertFalse(Type.isUnknown(Type.STRING_MAP_MAP));
        assertFalse(Type.isNull(Type.STRING_MAP_MAP));
        assertFalse(Type.isPrimitive(Type.STRING_MAP_MAP));
        assertFalse(Type.isNumeric(Type.STRING_MAP_MAP));
        assertFalse(Type.isList(Type.STRING_MAP_MAP));
        assertTrue(Type.isMap(Type.STRING_MAP_MAP));
        assertFalse(Type.isPrimitiveMap(Type.STRING_MAP_MAP));
        assertTrue(Type.isComplexMap(Type.STRING_MAP_MAP));

        assertFalse(Type.isUnknown(Type.BOOLEAN_MAP_LIST));
        assertFalse(Type.isNull(Type.BOOLEAN_MAP_LIST));
        assertFalse(Type.isPrimitive(Type.BOOLEAN_MAP_LIST));
        assertFalse(Type.isNumeric(Type.BOOLEAN_MAP_LIST));
        assertFalse(Type.isMap(Type.BOOLEAN_MAP_LIST));
        assertTrue(Type.isList(Type.BOOLEAN_MAP_LIST));
        assertFalse(Type.isPrimitiveList(Type.BOOLEAN_MAP_LIST));
        assertTrue(Type.isComplexList(Type.BOOLEAN_MAP_LIST));
    }

    @Test
    public void testCanCompare() {
        assertFalse(Type.canCompare(Type.NULL, Type.NULL));
        assertFalse(Type.canCompare(Type.UNKNOWN, Type.NULL));
        assertFalse(Type.canCompare(Type.NULL, Type.UNKNOWN));
        assertFalse(Type.canCompare(Type.UNKNOWN, Type.UNKNOWN));
        assertFalse(Type.canCompare(Type.STRING_MAP, Type.FLOAT_MAP));
        assertFalse(Type.canCompare(Type.STRING_LIST, Type.INTEGER_LIST));
        assertFalse(Type.canCompare(Type.STRING_MAP_MAP, Type.DOUBLE_MAP_MAP));
        assertFalse(Type.canCompare(Type.STRING_MAP_LIST, Type.BOOLEAN_MAP_LIST));
        for (Type type : Type.PRIMITIVES) {
            assertTrue(Type.canCompare(type, type));
        }
        assertAllSatisfy(Type.NUMERICS, Type::canCompare);
    }

    @Test
    public void testCanForceCast() {
        for (Type type : Type.values()) {
            assertTrue(Type.canForceCast(type, type));
        }
        assertAllSatisfy(Type.PRIMITIVES, Type::canForceCast);
        assertAllSatisfy(Type.PRIMITIVE_MAPS, Type::canForceCast);
        assertAllSatisfy(Type.PRIMITIVE_LISTS, Type::canForceCast);
        assertAllSatisfy(Type.COMPLEX_MAPS, Type::canForceCast);
        assertAllSatisfy(Type.COMPLEX_LISTS, Type::canForceCast);
    }

    @Test
    public void testCanSafeCast() {
        for (Type type : Type.values()) {
            assertTrue(Type.canSafeCast(type, type));
        }
        for (Type type : Type.PRIMITIVES) {
            assertTrue(Type.canSafeCast(Type.STRING, type));
        }
        assertTrue(Type.canSafeCast(Type.LONG, Type.INTEGER));
        assertTrue(Type.canSafeCast(Type.LONG_MAP, Type.INTEGER_MAP));
        assertTrue(Type.canSafeCast(Type.LONG_LIST, Type.INTEGER_LIST));
        assertTrue(Type.canSafeCast(Type.LONG_MAP_MAP, Type.INTEGER_MAP_MAP));
        assertTrue(Type.canSafeCast(Type.LONG_MAP_LIST, Type.INTEGER_MAP_LIST));
        assertFalse(Type.canSafeCast(Type.LONG, Type.FLOAT));
        assertFalse(Type.canSafeCast(Type.LONG_MAP, Type.FLOAT_MAP));
        assertFalse(Type.canSafeCast(Type.LONG_LIST, Type.FLOAT_LIST));
        assertFalse(Type.canSafeCast(Type.LONG_MAP_MAP, Type.FLOAT_MAP_MAP));
        assertFalse(Type.canSafeCast(Type.LONG_MAP_LIST, Type.FLOAT_MAP_LIST));
        assertFalse(Type.canSafeCast(Type.LONG, Type.DOUBLE));
        assertFalse(Type.canSafeCast(Type.LONG_MAP, Type.DOUBLE_MAP));
        assertFalse(Type.canSafeCast(Type.LONG_LIST, Type.DOUBLE_LIST));
        assertFalse(Type.canSafeCast(Type.LONG_MAP_MAP, Type.DOUBLE_MAP_MAP));
        assertFalse(Type.canSafeCast(Type.LONG_MAP_LIST, Type.DOUBLE_MAP_LIST));

        assertTrue(Type.canSafeCast(Type.DOUBLE, Type.INTEGER));
        assertTrue(Type.canSafeCast(Type.DOUBLE_MAP, Type.INTEGER_MAP));
        assertTrue(Type.canSafeCast(Type.DOUBLE_LIST, Type.INTEGER_LIST));
        assertTrue(Type.canSafeCast(Type.DOUBLE_MAP_MAP, Type.INTEGER_MAP_MAP));
        assertTrue(Type.canSafeCast(Type.DOUBLE_MAP_LIST, Type.INTEGER_MAP_LIST));
        assertTrue(Type.canSafeCast(Type.DOUBLE, Type.LONG));
        assertTrue(Type.canSafeCast(Type.DOUBLE_MAP, Type.LONG_MAP));
        assertTrue(Type.canSafeCast(Type.DOUBLE_LIST, Type.LONG_LIST));
        assertTrue(Type.canSafeCast(Type.DOUBLE_MAP_MAP, Type.LONG_MAP_MAP));
        assertTrue(Type.canSafeCast(Type.DOUBLE_MAP_LIST, Type.LONG_MAP_LIST));
        assertTrue(Type.canSafeCast(Type.DOUBLE, Type.FLOAT));
        assertTrue(Type.canSafeCast(Type.DOUBLE_MAP, Type.FLOAT_MAP));
        assertTrue(Type.canSafeCast(Type.DOUBLE_LIST, Type.FLOAT_LIST));
        assertTrue(Type.canSafeCast(Type.DOUBLE_MAP_MAP, Type.FLOAT_MAP_MAP));
        assertTrue(Type.canSafeCast(Type.DOUBLE_MAP_LIST, Type.FLOAT_MAP_LIST));

        assertAllFail(Type.PRIMITIVES, Type.LISTS, Type::canSafeCast);
        assertAllFail(Type.PRIMITIVES, Type.MAPS, Type::canSafeCast);
        assertAllFail(Type.MAPS, Type.LISTS, Type::canSafeCast);
        assertAllFail(Type.PRIMITIVE_MAPS, Type.COMPLEX_MAPS, Type::canSafeCast);
        assertAllFail(Type.PRIMITIVE_LISTS, Type.COMPLEX_LISTS, Type::canSafeCast);
    }
}
