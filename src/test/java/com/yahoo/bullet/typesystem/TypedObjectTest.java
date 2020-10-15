/*
 *  Copyright 2020, Yahoo Inc.
 *  Licensed under the terms of the Apache License, Version 2.0.
 *  See the LICENSE file associated with the project for terms.
 */
package com.yahoo.bullet.typesystem;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.yahoo.bullet.TestHelpers.list;
import static com.yahoo.bullet.TestHelpers.map;
import static com.yahoo.bullet.TestHelpers.nestedList;
import static com.yahoo.bullet.TestHelpers.nestedMap;
import static com.yahoo.bullet.typesystem.Type.BOOLEAN;
import static com.yahoo.bullet.typesystem.Type.BOOLEAN_LIST;
import static com.yahoo.bullet.typesystem.Type.BOOLEAN_MAP_LIST;
import static com.yahoo.bullet.typesystem.Type.DOUBLE;
import static com.yahoo.bullet.typesystem.Type.DOUBLE_LIST;
import static com.yahoo.bullet.typesystem.Type.DOUBLE_MAP;
import static com.yahoo.bullet.typesystem.Type.DOUBLE_MAP_MAP;
import static com.yahoo.bullet.typesystem.Type.FLOAT;
import static com.yahoo.bullet.typesystem.Type.FLOAT_LIST;
import static com.yahoo.bullet.typesystem.Type.INTEGER;
import static com.yahoo.bullet.typesystem.Type.INTEGER_LIST;
import static com.yahoo.bullet.typesystem.Type.INTEGER_MAP;
import static com.yahoo.bullet.typesystem.Type.INTEGER_MAP_LIST;
import static com.yahoo.bullet.typesystem.Type.INTEGER_MAP_MAP;
import static com.yahoo.bullet.typesystem.Type.LONG;
import static com.yahoo.bullet.typesystem.Type.LONG_LIST;
import static com.yahoo.bullet.typesystem.Type.LONG_MAP;
import static com.yahoo.bullet.typesystem.Type.LONG_MAP_LIST;
import static com.yahoo.bullet.typesystem.Type.LONG_MAP_MAP;
import static com.yahoo.bullet.typesystem.Type.NULL;
import static com.yahoo.bullet.typesystem.Type.STRING;
import static com.yahoo.bullet.typesystem.Type.STRING_LIST;
import static com.yahoo.bullet.typesystem.Type.STRING_MAP;
import static com.yahoo.bullet.typesystem.Type.STRING_MAP_LIST;
import static com.yahoo.bullet.typesystem.Type.STRING_MAP_MAP;
import static com.yahoo.bullet.typesystem.Type.UNKNOWN;
import static com.yahoo.bullet.typesystem.Type.UNKNOWN_LIST;
import static com.yahoo.bullet.typesystem.Type.UNKNOWN_MAP;
import static com.yahoo.bullet.typesystem.Type.UNKNOWN_MAP_LIST;
import static com.yahoo.bullet.typesystem.Type.UNKNOWN_MAP_MAP;
import static com.yahoo.bullet.typesystem.TypedObject.safeCastFromObject;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

public class TypedObjectTest {
    @Test
    public void testTypedObjectWithUnsupportedType() {
        TypedObject object = new TypedObject(UNKNOWN, new HashSet<>());
        assertEquals(object.getType(), UNKNOWN);
        assertEquals(object.getValue(), emptySet());
    }

    @Test
    public void testTypedObjectWithUnknownContainerTypes() {
        TypedObject objectA = new TypedObject(UNKNOWN_LIST, new ArrayList<>());
        assertEquals(objectA.getType(), UNKNOWN_LIST);
        assertEquals(objectA.getValue(), emptyList());
        TypedObject objectB = new TypedObject(UNKNOWN_MAP, new HashMap<>());
        assertEquals(objectB.getType(), UNKNOWN_MAP);
        assertEquals(objectB.getValue(), emptyMap());
        TypedObject objectC = new TypedObject(UNKNOWN_MAP_MAP, nestedMap(singletonMap("foo", new HashMap<>())));
        assertEquals(objectC.getType(), UNKNOWN_MAP_MAP);
        assertEquals(objectC.getValue(), singletonMap("foo", emptyMap()));
        TypedObject objectD = new TypedObject(UNKNOWN_LIST, nestedList(singletonList(emptyMap())));
        assertEquals(objectD.getType(), UNKNOWN_LIST);
        assertEquals(objectD.getValue(), singletonList(emptyMap()));
    }

    @Test
    public void testTypeClassification() {
        TypedObject object;

        object = new TypedObject(UNKNOWN, null);
        assertTrue(object.isUnknown());
        assertFalse(object.isNull());
        assertFalse(object.isList());
        assertFalse(object.isMap());

        object = new TypedObject(UNKNOWN_LIST, new ArrayList<>());
        assertFalse(object.isUnknown());
        assertFalse(object.isNull());
        assertFalse(object.isPrimitive());
        assertTrue(object.isList());
        assertTrue(object.isPrimitiveList());
        assertFalse(object.isComplexList());
        assertFalse(object.isMap());

        object = new TypedObject(UNKNOWN_MAP_LIST, nestedList(singletonList(emptyMap())));
        assertFalse(object.isUnknown());
        assertFalse(object.isNull());
        assertFalse(object.isPrimitive());
        assertTrue(object.isList());
        assertFalse(object.isPrimitiveList());
        assertTrue(object.isComplexList());
        assertFalse(object.isMap());

        object = new TypedObject(UNKNOWN_MAP, new HashMap<>());
        assertFalse(object.isUnknown());
        assertFalse(object.isNull());
        assertFalse(object.isPrimitive());
        assertTrue(object.isMap());
        assertTrue(object.isPrimitiveMap());
        assertFalse(object.isComplexMap());
        assertFalse(object.isList());

        object = new TypedObject(UNKNOWN_MAP_MAP, nestedMap(singletonMap("foo", emptyMap())));
        assertFalse(object.isUnknown());
        assertFalse(object.isNull());
        assertFalse(object.isPrimitive());
        assertTrue(object.isMap());
        assertFalse(object.isPrimitiveMap());
        assertTrue(object.isComplexMap());
        assertFalse(object.isList());

        object = new TypedObject(NULL, null);
        assertTrue(object.isNull());
        assertFalse(object.isUnknown());
        assertFalse(object.isPrimitive());
        assertFalse(object.isList());
        assertFalse(object.isMap());

        object = new TypedObject(LONG, 1L);
        assertFalse(object.isUnknown());
        assertFalse(object.isNull());
        assertTrue(object.isPrimitive());
        assertFalse(object.isList());
        assertFalse(object.isMap());

        object = new TypedObject(Type.FLOAT_MAP, map(singletonMap("a", 1.0f)));
        assertFalse(object.isUnknown());
        assertFalse(object.isNull());
        assertFalse(object.isPrimitive());
        assertFalse(object.isList());
        assertTrue(object.isMap());
        assertTrue(object.isPrimitiveMap());
        assertFalse(object.isComplexMap());

        object = new TypedObject(DOUBLE_LIST, list(singletonList(1.0)));
        assertFalse(object.isUnknown());
        assertFalse(object.isNull());
        assertFalse(object.isPrimitive());
        assertFalse(object.isMap());
        assertTrue(object.isList());
        assertTrue(object.isPrimitiveList());
        assertFalse(object.isComplexList());

        object = new TypedObject(Type.STRING_MAP_MAP, null);
        assertFalse(object.isUnknown());
        assertFalse(object.isNull());
        assertFalse(object.isPrimitive());
        assertFalse(object.isList());
        assertTrue(object.isMap());
        assertFalse(object.isPrimitiveMap());
        assertTrue(object.isComplexMap());

        object = new TypedObject(Type.BOOLEAN_MAP_LIST, null);
        assertFalse(object.isUnknown());
        assertFalse(object.isNull());
        assertFalse(object.isPrimitive());
        assertFalse(object.isMap());
        assertTrue(object.isList());
        assertFalse(object.isPrimitiveList());
        assertTrue(object.isComplexList());
    }

    @Test
    public void testSize() {
        TypedObject objectA = new TypedObject(STRING_LIST, list(asList("1", "2")));
        TypedObject objectB = new TypedObject(UNKNOWN_LIST, new ArrayList<>());
        TypedObject objectC = new TypedObject(STRING, "");
        TypedObject objectD = new TypedObject(STRING, "11");
        TypedObject objectE = new TypedObject(UNKNOWN_MAP, new HashMap<>());
        TypedObject objectF = new TypedObject(LONG_MAP, map(singletonMap("foo", 1L)));
        TypedObject objectG = new TypedObject(UNKNOWN_MAP_LIST, nestedList(singletonList(new HashMap<>())));
        TypedObject objectH = new TypedObject(UNKNOWN_MAP_MAP, nestedMap(singletonMap("foo", new HashMap<>())));
        assertEquals(objectA.size(), 2);
        assertEquals(objectB.size(), 0);
        assertEquals(objectC.size(), 0);
        assertEquals(objectD.size(), 2);
        assertEquals(objectE.size(), 0);
        assertEquals(objectF.size(), 1);
        assertEquals(objectG.size(), 1);
        assertEquals(objectH.size(), 1);
    }

    @Test(expectedExceptions = UnsupportedOperationException.class, expectedExceptionsMessageRegExp = ".*This type does not support getting a size.*")
    public void testUnsupportedTypeSize() {
        TypedObject object = new TypedObject(1);
        object.size();
    }

    @Test
    public void testContainsKey() {
        TypedObject objectA = new TypedObject(UNKNOWN_MAP_LIST, nestedList(singletonList(emptyMap())));
        TypedObject objectB = new TypedObject(STRING_MAP_LIST, nestedList(singletonList(Collections.emptyMap())));
        TypedObject objectC = new TypedObject(STRING_MAP_LIST, nestedList(singletonList(singletonMap("1", "2"))));
        TypedObject objectD = new TypedObject(UNKNOWN_MAP, new HashMap<>());
        TypedObject objectE = new TypedObject(STRING_MAP, map(singletonMap("1", "2")));
        TypedObject objectF = new TypedObject(STRING_MAP_MAP, nestedMap(singletonMap("11", singletonMap("1", "2"))));
        TypedObject objectG = new TypedObject(UNKNOWN_MAP_MAP, nestedMap(singletonMap("11", emptyMap())));
        assertFalse(objectA.containsKey("1"));
        assertFalse(objectB.containsKey("1"));
        assertTrue(objectC.containsKey("1"));
        assertFalse(objectC.containsKey("2"));
        assertFalse(objectD.containsKey("1"));
        assertTrue(objectE.containsKey("1"));
        assertFalse(objectE.containsKey("2"));
        assertTrue(objectF.containsKey("1"));
        assertTrue(objectF.containsKey("11"));
        assertFalse(objectF.containsKey("2"));
        assertFalse(objectG.containsKey("1"));
        assertTrue(objectG.containsKey("11"));
    }

    @Test(expectedExceptions = UnsupportedOperationException.class, expectedExceptionsMessageRegExp = ".*This type does not support mappings.*")
    public void testUnsupportedTypeContainsKey() {
        TypedObject object = new TypedObject(1);
        object.containsKey("1");
    }

    @Test
    public void testContainsValue() {
        TypedObject objectA = new TypedObject(INTEGER_LIST, list(asList(1, 2)));
        TypedObject objectB = new TypedObject(UNKNOWN_LIST, new ArrayList<>());
        TypedObject objectC = new TypedObject(INTEGER_MAP_LIST, nestedList(singletonList(emptyMap())));
        TypedObject objectD = new TypedObject(INTEGER_MAP_LIST, nestedList(singletonList(singletonMap("1", 2))));
        TypedObject objectE = new TypedObject(UNKNOWN_MAP, new HashMap<>());
        TypedObject objectF = new TypedObject(INTEGER_MAP, map(singletonMap("1", 2)));
        TypedObject objectG = new TypedObject(INTEGER_MAP_MAP, nestedMap(singletonMap("1", singletonMap("1", 2))));
        TypedObject objectH = new TypedObject(UNKNOWN_MAP_MAP, nestedMap(singletonMap("11", emptyMap())));
        TypedObject objectI = new TypedObject(UNKNOWN_MAP_LIST, nestedList(singletonList(emptyMap())));
        assertFalse(objectA.containsValue(new TypedObject(INTEGER, 3)));
        assertTrue(objectA.containsValue(new TypedObject(INTEGER, 1)));
        assertFalse(objectB.containsValue(new TypedObject(INTEGER, 1)));
        assertFalse(objectC.containsValue(new TypedObject(INTEGER, 1)));
        assertFalse(objectD.containsValue(new TypedObject(INTEGER, 1)));
        assertTrue(objectD.containsValue(new TypedObject(INTEGER, 2)));
        assertFalse(objectE.containsValue(new TypedObject(INTEGER, 1)));
        assertFalse(objectF.containsValue(new TypedObject(INTEGER, 1)));
        assertTrue(objectF.containsValue(new TypedObject(INTEGER, 2)));
        assertFalse(objectG.containsValue(new TypedObject(INTEGER, 1)));
        assertTrue(objectG.containsValue(new TypedObject(INTEGER, 2)));
        assertFalse(objectH.containsValue(new TypedObject(STRING, "11")));
        assertFalse(objectI.containsValue(new TypedObject(INTEGER, 1)));
    }

    @Test(expectedExceptions = UnsupportedOperationException.class, expectedExceptionsMessageRegExp = ".*This type of field does not support contains value:.*")
    public void testUnsupportedTypeContainsValue() {
        TypedObject object = new TypedObject(INTEGER, 1);
        object.containsValue(new TypedObject(INTEGER, 1));
    }

    @Test
    public void testTernaryContainsKey() {
        TypedObject objectA = new TypedObject(UNKNOWN_MAP_LIST, nestedList(singletonList(emptyMap())));
        TypedObject objectB = new TypedObject(STRING_MAP_LIST, nestedList(singletonList(Collections.emptyMap())));
        TypedObject objectC = new TypedObject(STRING_MAP_LIST, nestedList(singletonList(singletonMap("1", "2"))));
        TypedObject objectD = new TypedObject(UNKNOWN_MAP, new HashMap<>());
        TypedObject objectE = new TypedObject(STRING_MAP, map(singletonMap("1", "2")));
        TypedObject objectF = new TypedObject(STRING_MAP_MAP, nestedMap(singletonMap("11", singletonMap("1", "2"))));
        TypedObject objectG = new TypedObject(UNKNOWN_MAP_MAP, nestedMap(singletonMap("11", emptyMap())));
        assertFalse(objectA.ternaryContainsKey("1"));
        assertFalse(objectB.ternaryContainsKey("1"));
        assertTrue(objectC.ternaryContainsKey("1"));
        assertFalse(objectC.ternaryContainsKey("2"));
        assertFalse(objectD.ternaryContainsKey("1"));
        assertTrue(objectE.ternaryContainsKey("1"));
        assertFalse(objectE.ternaryContainsKey("2"));
        assertTrue(objectF.ternaryContainsKey("1"));
        assertTrue(objectF.ternaryContainsKey("11"));
        assertFalse(objectF.ternaryContainsKey("2"));
        assertFalse(objectG.ternaryContainsKey("1"));
        assertTrue(objectG.ternaryContainsKey("11"));
    }

    @Test
    public void testTernaryContainsKeyWithNull() {
        TypedObject objectA = new TypedObject(STRING_MAP_LIST, nestedList(singletonList(singletonMap(null, "2"))));

        List<Map<String, String>> mapListA = new ArrayList<>();
        mapListA.add(singletonMap("1", "2"));
        mapListA.add(singletonMap(null, "2"));
        mapListA.add(singletonMap("3", "2"));

        TypedObject objectB = new TypedObject(STRING_MAP_LIST, (ArrayList) mapListA);

        List<Map<String, String>> mapListB = new ArrayList<>();
        mapListB.add(singletonMap("1", "2"));
        mapListB.add(null);
        mapListB.add(singletonMap("3", "2"));

        TypedObject objectC = new TypedObject(STRING_MAP_LIST, (ArrayList) mapListB);

        Map<String, Map<String, String>> mapMapA = new HashMap<>();
        mapMapA.put("a", singletonMap("1", "2"));
        mapMapA.put("b", singletonMap(null, "2"));
        mapMapA.put("c", singletonMap("3", "2"));

        TypedObject objectD = new TypedObject(STRING_MAP_MAP, (HashMap) mapMapA);

        Map<String, Map<String, String>> mapMapB = new HashMap<>();
        mapMapB.put("a", singletonMap("1", "2"));
        mapMapB.put(null, singletonMap("2", "2"));
        mapMapB.put("c", singletonMap("3", "2"));

        TypedObject objectE = new TypedObject(STRING_MAP_MAP, (HashMap) mapMapB);

        Map<String, String> mapA = new HashMap<>();
        mapA.put("1", "2");
        mapA.put(null, "2");
        mapA.put("3", "2");

        TypedObject objectF = new TypedObject(STRING_MAP, (HashMap) mapA);

        Assert.assertNull(TypedObject.NULL.ternaryContainsKey("1"));
        Assert.assertNull(objectA.ternaryContainsKey(null));
        Assert.assertNull(objectA.ternaryContainsKey("1"));
        Assert.assertTrue(objectB.ternaryContainsKey("1"));
        Assert.assertNull(objectB.ternaryContainsKey("2"));
        Assert.assertTrue(objectB.ternaryContainsKey("3"));
        Assert.assertTrue(objectC.ternaryContainsKey("1"));
        Assert.assertNull(objectC.ternaryContainsKey("2"));
        Assert.assertTrue(objectC.ternaryContainsKey("3"));
        Assert.assertTrue(objectD.ternaryContainsKey("1"));
        Assert.assertNull(objectD.ternaryContainsKey("2"));
        Assert.assertTrue(objectD.ternaryContainsKey("3"));
        Assert.assertTrue(objectE.ternaryContainsKey("a"));
        Assert.assertNull(objectE.ternaryContainsKey("b"));
        Assert.assertTrue(objectE.ternaryContainsKey("c"));
        Assert.assertTrue(objectF.ternaryContainsKey("1"));
        Assert.assertNull(objectF.ternaryContainsKey("2"));
        Assert.assertTrue(objectF.ternaryContainsKey("3"));
    }

    @Test(expectedExceptions = UnsupportedOperationException.class, expectedExceptionsMessageRegExp = ".*This type does not support mappings.*")
    public void testUnsupportedTypeTernaryContainsKey() {
        TypedObject object = new TypedObject(1);
        object.ternaryContainsKey("1");
    }

    @Test
    public void testTernaryContainsValue() {
        TypedObject objectA = new TypedObject(INTEGER_LIST, list(asList(1, 2)));
        TypedObject objectB = new TypedObject(UNKNOWN_LIST, new ArrayList<>());
        TypedObject objectC = new TypedObject(INTEGER_MAP_LIST, nestedList(singletonList(emptyMap())));
        TypedObject objectD = new TypedObject(INTEGER_MAP_LIST, nestedList(singletonList(singletonMap("1", 2))));
        TypedObject objectE = new TypedObject(UNKNOWN_MAP, new HashMap<>());
        TypedObject objectF = new TypedObject(INTEGER_MAP, map(singletonMap("1", 2)));
        TypedObject objectG = new TypedObject(INTEGER_MAP_MAP, nestedMap(singletonMap("1", singletonMap("1", 2))));
        TypedObject objectH = new TypedObject(UNKNOWN_MAP_MAP, nestedMap(singletonMap("11", emptyMap())));
        TypedObject objectI = new TypedObject(UNKNOWN_MAP_LIST, nestedList(singletonList(emptyMap())));
        assertFalse(objectA.ternaryContainsValue(new TypedObject(INTEGER, 3)));
        assertTrue(objectA.ternaryContainsValue(new TypedObject(INTEGER, 1)));
        assertFalse(objectB.ternaryContainsValue(new TypedObject(INTEGER, 1)));
        assertFalse(objectC.ternaryContainsValue(new TypedObject(INTEGER, 1)));
        assertFalse(objectD.ternaryContainsValue(new TypedObject(INTEGER, 1)));
        assertTrue(objectD.ternaryContainsValue(new TypedObject(INTEGER, 2)));
        assertFalse(objectE.ternaryContainsValue(new TypedObject(INTEGER, 1)));
        assertFalse(objectF.ternaryContainsValue(new TypedObject(INTEGER, 1)));
        assertTrue(objectF.ternaryContainsValue(new TypedObject(INTEGER, 2)));
        assertFalse(objectG.ternaryContainsValue(new TypedObject(INTEGER, 1)));
        assertTrue(objectG.ternaryContainsValue(new TypedObject(INTEGER, 2)));
        assertFalse(objectH.ternaryContainsValue(new TypedObject(STRING, "11")));
        assertFalse(objectI.ternaryContainsValue(new TypedObject(INTEGER, 1)));
    }

    @Test
    public void testTernaryContainsValueWithNull() {
        TypedObject objectA = new TypedObject(STRING_MAP_LIST, nestedList(singletonList(singletonMap("1", null))));

        List<Map<String, String>> mapListA = new ArrayList<>();
        mapListA.add(singletonMap("1", "1"));
        mapListA.add(singletonMap("2", null));
        mapListA.add(singletonMap("3", "3"));

        TypedObject objectB = new TypedObject(STRING_MAP_LIST, (ArrayList) mapListA);

        List<Map<String, String>> mapListB = new ArrayList<>();
        mapListB.add(singletonMap("1", "1"));
        mapListB.add(null);
        mapListB.add(singletonMap("3", "3"));

        TypedObject objectC = new TypedObject(STRING_MAP_LIST, (ArrayList) mapListB);

        Map<String, Map<String, String>> mapMapA = new HashMap<>();
        mapMapA.put("a", singletonMap("1", "1"));
        mapMapA.put("b", singletonMap("2", null));
        mapMapA.put("c", singletonMap("3", "3"));

        TypedObject objectD = new TypedObject(STRING_MAP_MAP, (HashMap) mapMapA);

        Map<String, Map<String, String>> mapMapB = new HashMap<>();
        mapMapB.put("a", singletonMap("1", "1"));
        mapMapB.put("b", null);
        mapMapB.put("c", singletonMap("3", "3"));

        TypedObject objectE = new TypedObject(STRING_MAP_MAP, (HashMap) mapMapB);

        Map<String, String> mapA = new HashMap<>();
        mapA.put("1", "1");
        mapA.put("2", null);
        mapA.put("3", "3");

        TypedObject objectF = new TypedObject(STRING_MAP, (HashMap) mapA);

        Assert.assertNull(TypedObject.NULL.ternaryContainsValue(TypedObject.valueOf("1")));
        Assert.assertNull(objectA.ternaryContainsValue(TypedObject.NULL));
        Assert.assertNull(objectA.ternaryContainsValue(TypedObject.valueOf("1")));
        Assert.assertTrue(objectB.ternaryContainsValue(TypedObject.valueOf("1")));
        Assert.assertNull(objectB.ternaryContainsValue(TypedObject.valueOf("2")));
        Assert.assertTrue(objectB.ternaryContainsValue(TypedObject.valueOf("3")));
        Assert.assertTrue(objectC.ternaryContainsValue(TypedObject.valueOf("1")));
        Assert.assertNull(objectC.ternaryContainsValue(TypedObject.valueOf("2")));
        Assert.assertTrue(objectC.ternaryContainsValue(TypedObject.valueOf("3")));
        Assert.assertTrue(objectD.ternaryContainsValue(TypedObject.valueOf("1")));
        Assert.assertNull(objectD.ternaryContainsValue(TypedObject.valueOf("2")));
        Assert.assertTrue(objectD.ternaryContainsValue(TypedObject.valueOf("3")));
        Assert.assertTrue(objectE.ternaryContainsValue(TypedObject.valueOf("1")));
        Assert.assertNull(objectE.ternaryContainsValue(TypedObject.valueOf("2")));
        Assert.assertTrue(objectE.ternaryContainsValue(TypedObject.valueOf("3")));
        Assert.assertTrue(objectF.ternaryContainsValue(TypedObject.valueOf("1")));
        Assert.assertNull(objectF.ternaryContainsValue(TypedObject.valueOf("2")));
        Assert.assertTrue(objectF.ternaryContainsValue(TypedObject.valueOf("3")));
    }

    @Test(expectedExceptions = UnsupportedOperationException.class, expectedExceptionsMessageRegExp = ".*This type of field does not support contains value:.*")
    public void testUnsupportedTypeTernaryContainsValue() {
        TypedObject object = new TypedObject(INTEGER, 1);
        object.ternaryContainsValue(new TypedObject(INTEGER, 1));
    }

    @Test
    public void testForceCastInteger() {
        TypedObject object = new TypedObject(2);
        assertEquals(object.forceCast(INTEGER).getType(), INTEGER);
        assertEquals(object.forceCast(INTEGER).getValue(), 2);
        assertEquals(object.forceCast(LONG).getType(), LONG);
        assertEquals(object.forceCast(LONG).getValue(), 2L);
        assertEquals(object.forceCast(Type.FLOAT).getType(), Type.FLOAT);
        assertEquals(object.forceCast(Type.FLOAT).getValue(), 2.0f);
        assertEquals(object.forceCast(DOUBLE).getType(), DOUBLE);
        assertEquals(object.forceCast(DOUBLE).getValue(), 2.0);
        assertEquals(object.forceCast(STRING).getType(), STRING);
        assertEquals(object.forceCast(STRING).getValue(), "2");
        assertEquals(object.forceCast(Type.BOOLEAN).getType(), Type.BOOLEAN);
        assertEquals(object.forceCast(Type.BOOLEAN).getValue(), true);
    }

    @Test
    public void testForceCastLong() {
        TypedObject object = new TypedObject(Long.MAX_VALUE);
        assertEquals(object.forceCast(INTEGER).getType(), INTEGER);
        assertEquals(object.forceCast(INTEGER).getValue(), -1);
        assertEquals(object.forceCast(LONG).getType(), LONG);
        assertEquals(object.forceCast(LONG).getValue(), Long.MAX_VALUE);
        assertEquals(object.forceCast(Type.FLOAT).getType(), Type.FLOAT);
        assertTrue(Math.abs((Float) object.forceCast(Type.FLOAT).getValue() - Long.MAX_VALUE) <= 1e-07);
        assertEquals(object.forceCast(DOUBLE).getType(), DOUBLE);
        assertTrue(Math.abs((Double) object.forceCast(DOUBLE).getValue() - Long.MAX_VALUE) <= 1e-07);
        assertEquals(object.forceCast(STRING).getType(), STRING);
        assertEquals(object.forceCast(STRING).getValue(), "9223372036854775807");
        assertEquals(object.forceCast(Type.BOOLEAN).getType(), Type.BOOLEAN);
        assertEquals(object.forceCast(Type.BOOLEAN).getValue(), true);
    }

    @Test
    public void testForceCastFloat() {
        TypedObject object = new TypedObject(3.2f);
        assertEquals(object.forceCast(INTEGER).getType(), INTEGER);
        assertEquals(object.forceCast(INTEGER).getValue(), 3);
        assertEquals(object.forceCast(LONG).getType(), LONG);
        assertEquals(object.forceCast(LONG).getValue(), 3L);
        assertEquals(object.forceCast(Type.FLOAT).getType(), Type.FLOAT);
        assertTrue(Math.abs((Float) object.forceCast(Type.FLOAT).getValue() - 3.2f) <= 1e-07);
        assertEquals(object.forceCast(DOUBLE).getType(), DOUBLE);
        assertTrue(Math.abs((Double) object.forceCast(DOUBLE).getValue() - 3.2) <= 1e-07);
        assertEquals(object.forceCast(STRING).getType(), STRING);
        assertEquals(object.forceCast(STRING).getValue(), "3.2");
        assertEquals(object.forceCast(Type.BOOLEAN).getType(), Type.BOOLEAN);
        assertEquals(object.forceCast(Type.BOOLEAN).getValue(), true);
    }

    @Test
    public void testForceCastDouble() {
        TypedObject object = new TypedObject(-5.2);
        assertEquals(object.forceCast(INTEGER).getType(), INTEGER);
        assertEquals(object.forceCast(INTEGER).getValue(), -5);
        assertEquals(object.forceCast(LONG).getType(), LONG);
        assertEquals(object.forceCast(LONG).getValue(), -5L);
        assertEquals(object.forceCast(Type.FLOAT).getType(), Type.FLOAT);
        assertTrue(Math.abs((Float) object.forceCast(Type.FLOAT).getValue() + 5.2f) <= 1e-07);
        assertEquals(object.forceCast(DOUBLE).getType(), DOUBLE);
        assertTrue(Math.abs((Double) object.forceCast(DOUBLE).getValue() + 5.2) <= 1e-07);
        assertEquals(object.forceCast(STRING).getType(), STRING);
        assertEquals(object.forceCast(STRING).getValue(), "-5.2");
        assertEquals(object.forceCast(Type.BOOLEAN).getType(), Type.BOOLEAN);
        assertEquals(object.forceCast(Type.BOOLEAN).getValue(), true);
    }

    @Test
    public void testForceCastBoolean() {
        TypedObject object = new TypedObject(true);
        assertEquals(object.forceCast(INTEGER).getType(), INTEGER);
        assertEquals(object.forceCast(INTEGER).getValue(), 1);
        assertEquals(object.forceCast(LONG).getType(), LONG);
        assertEquals(object.forceCast(LONG).getValue(), 1L);
        assertEquals(object.forceCast(Type.FLOAT).getType(), Type.FLOAT);
        assertTrue(Math.abs((Float) object.forceCast(Type.FLOAT).getValue() - 1.0f) <= 1e-07);
        assertEquals(object.forceCast(DOUBLE).getType(), DOUBLE);
        assertTrue(Math.abs((Double) object.forceCast(DOUBLE).getValue() - 1.0) <= 1e-07);
        assertEquals(object.forceCast(STRING).getType(), STRING);
        assertEquals(object.forceCast(STRING).getValue(), "true");
        assertEquals(object.forceCast(Type.BOOLEAN).getType(), Type.BOOLEAN);
        assertEquals(object.forceCast(Type.BOOLEAN).getValue(), true);

        object = new TypedObject(false);
        assertEquals(object.forceCast(INTEGER).getType(), INTEGER);
        assertEquals(object.forceCast(INTEGER).getValue(), 0);
        assertEquals(object.forceCast(LONG).getType(), LONG);
        assertEquals(object.forceCast(LONG).getValue(), 0L);
        assertEquals(object.forceCast(Type.FLOAT).getType(), Type.FLOAT);
        assertTrue(Math.abs((Float) object.forceCast(Type.FLOAT).getValue()) <= 1e-07);
        assertEquals(object.forceCast(DOUBLE).getType(), DOUBLE);
        assertTrue(Math.abs((Double) object.forceCast(DOUBLE).getValue()) <= 1e-07);
        assertEquals(object.forceCast(STRING).getType(), STRING);
        assertEquals(object.forceCast(STRING).getValue(), "false");
        assertEquals(object.forceCast(Type.BOOLEAN).getType(), Type.BOOLEAN);
        assertEquals(object.forceCast(Type.BOOLEAN).getValue(), false);
    }

    @Test
    public void testForceCastString() {
        TypedObject object = new TypedObject("0.0");
        assertEquals(object.forceCast(INTEGER).getType(), INTEGER);
        assertEquals(object.forceCast(INTEGER).getValue(), 0);
        assertEquals(object.forceCast(LONG).getType(), LONG);
        assertEquals(object.forceCast(LONG).getValue(), 0L);
        assertEquals(object.forceCast(Type.FLOAT).getType(), Type.FLOAT);
        assertTrue(Math.abs((Float) object.forceCast(Type.FLOAT).getValue()) <= 1e-07);
        assertEquals(object.forceCast(DOUBLE).getType(), DOUBLE);
        assertTrue(Math.abs((Double) object.forceCast(DOUBLE).getValue()) <= 1e-07);
        assertEquals(object.forceCast(STRING).getType(), STRING);
        assertEquals(object.forceCast(STRING).getValue(), "0.0");
        assertEquals(object.forceCast(Type.BOOLEAN).getType(), Type.BOOLEAN);
        assertEquals(object.forceCast(Type.BOOLEAN).getValue(), false);
    }

    @Test(expectedExceptions = ClassCastException.class)
    public void testForceCastToUnsupportedType() {
        TypedObject object = new TypedObject(1);
        object.forceCast(STRING_MAP_MAP);
    }

    @Test(expectedExceptions = ClassCastException.class)
    public void testForceCastUnsupportedTypeToInteger() {
        TypedObject object = new TypedObject(INTEGER_LIST, list(singletonList(1)));
        object.forceCast(INTEGER);
    }

    @Test(expectedExceptions = ClassCastException.class)
    public void testForceCastUnsupportedTypeToLong() {
        TypedObject object = new TypedObject(LONG_LIST, list(singletonList(1L)));
        object.forceCast(LONG);
    }

    @Test(expectedExceptions = ClassCastException.class)
    public void testForceCastUnsupportedTypeToDouble() {
        TypedObject object = new TypedObject(DOUBLE_LIST, list(singletonList(1.0)));
        object.forceCast(DOUBLE);
    }

    @Test(expectedExceptions = ClassCastException.class)
    public void testForceCastUnsupportedTypeToFloat() {
        TypedObject object = new TypedObject(FLOAT_LIST, list(singletonList(1.0f)));
        object.forceCast(Type.FLOAT);
    }

    @Test(expectedExceptions = ClassCastException.class)
    public void testForceCastUnsupportedTypeToBoolean() {
        TypedObject object = new TypedObject(BOOLEAN_LIST, list(singletonList(false)));
        object.forceCast(Type.BOOLEAN);
    }

    @Test
    public void testEqualTo() {
        TypedObject objectA = new TypedObject(NULL, null);
        TypedObject objectB = new TypedObject(BOOLEAN, true);
        TypedObject objectC = new TypedObject(STRING, "foo");
        TypedObject objectD = new TypedObject(INTEGER, 42);
        TypedObject objectE = new TypedObject(LONG, 42L);
        TypedObject objectF = new TypedObject(FLOAT, 41.0f);
        TypedObject objectG = new TypedObject(DOUBLE, 41.0);

        assertTrue(objectA.equalTo(objectA));
        assertTrue(objectB.equalTo(objectB));
        assertTrue(objectC.equalTo(objectC));

        assertTrue(objectD.equalTo(objectD));
        assertTrue(objectD.equalTo(objectE));
        assertFalse(objectD.equalTo(objectF));
        assertFalse(objectD.equalTo(objectG));

        assertTrue(objectE.equalTo(objectD));
        assertTrue(objectE.equalTo(objectE));
        assertFalse(objectE.equalTo(objectF));
        assertFalse(objectE.equalTo(objectG));

        assertFalse(objectF.equalTo(objectD));
        assertFalse(objectF.equalTo(objectE));
        assertTrue(objectF.equalTo(objectF));
        assertTrue(objectF.equalTo(objectG));

        assertFalse(objectG.equalTo(objectD));
        assertFalse(objectG.equalTo(objectE));
        assertTrue(objectG.equalTo(objectF));
        assertTrue(objectG.equalTo(objectG));
    }

    @Test
    public void testNullComparisonToNull() {
        TypedObject objectA = new TypedObject(NULL, null);
        assertEquals(TypedObject.NULL.compareTo(objectA), 0);
    }

    @Test
    public void testBooleanComparison() {
        TypedObject objectA = new TypedObject(BOOLEAN, true);
        TypedObject objectB = new TypedObject(BOOLEAN, false);
        assertTrue(objectA.compareTo(objectB) > 0);
        assertTrue(objectB.compareTo(objectA) < 0);
        assertEquals(objectA.compareTo(objectA), 0);
    }

    @Test
    public void testStringComparison() {
        TypedObject objectA = new TypedObject(STRING, "foo");
        TypedObject objectB = new TypedObject(STRING, "bar");
        assertTrue(objectA.compareTo(objectB) > 0);
        assertTrue(objectB.compareTo(objectA) < 0);
        assertTrue(objectA.compareTo(objectA) == 0);
    }

    @Test
    public void testIntegerComparison() {
        TypedObject objectA = new TypedObject(INTEGER, 42);
        TypedObject objectB = new TypedObject(INTEGER, 43);
        assertTrue(objectA.compareTo(objectB) < 0);
        assertTrue(objectB.compareTo(objectA) > 0);
        assertTrue(objectA.compareTo(objectA) == 0);
    }

    @Test
    public void testLongComparison() {
        TypedObject objectA = new TypedObject(LONG, 42L);
        TypedObject objectB = new TypedObject(LONG, 43L);
        assertTrue(objectA.compareTo(objectB) < 0);
        assertTrue(objectB.compareTo(objectA) > 0);
        assertTrue(objectA.compareTo(objectA) == 0);
    }

    @Test
    public void testFloatComparison() {
        TypedObject objectA = new TypedObject(FLOAT, 41.0f);
        TypedObject objectB = new TypedObject(FLOAT, 42.1f);
        assertTrue(objectA.compareTo(objectB) < 0);
        assertTrue(objectB.compareTo(objectA) > 0);
        assertTrue(objectA.compareTo(objectA) == 0);
    }

    @Test
    public void testDoubleComparison() {
        TypedObject objectA = new TypedObject(DOUBLE, 41.0);
        TypedObject objectB = new TypedObject(DOUBLE, 42.1);
        assertTrue(objectA.compareTo(objectB) < 0);
        assertTrue(objectB.compareTo(objectA) > 0);
        assertTrue(objectA.compareTo(objectA) == 0);
    }

    @Test
    public void testNumericComparison() {
        TypedObject objectA = new TypedObject(DOUBLE, 42.0);
        TypedObject objectB = new TypedObject(FLOAT, 42.1f);
        TypedObject objectC = new TypedObject(INTEGER, 42);
        TypedObject objectD = new TypedObject(LONG, 42L);
        assertEquals(objectA.compareTo(objectA), 0);
        assertTrue(objectA.compareTo(objectB) < 0);
        assertEquals(objectA.compareTo(objectC), 0);
        assertEquals(objectA.compareTo(objectD), 0);
        assertTrue(objectB.compareTo(objectA) > 0);
        assertEquals(objectB.compareTo(objectB), 0);
        assertTrue(objectB.compareTo(objectC) > 0);
        assertTrue(objectB.compareTo(objectD) > 0);
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testUncastableStringComparison() {
        TypedObject objectA = new TypedObject(LONG, 1L);
        TypedObject objectB = new TypedObject(STRING, "1");
        objectA.compareTo(objectB);
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testNonPrimitiveComparison() {
        TypedObject objectA = new TypedObject(LONG, 1L);
        TypedObject objectB = new TypedObject(LONG_LIST, list(singletonList(1L)));
        objectA.compareTo(objectB);
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testUnknownComparison() {
        TypedObject objectA = new TypedObject(UNKNOWN, null);
        TypedObject objectB = new TypedObject(42.1);
        objectA.compareTo(objectB);
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testUnknownContainerComparison() {
        TypedObject objectA = new TypedObject(UNKNOWN_LIST, new ArrayList<>());
        TypedObject objectB = new TypedObject(UNKNOWN_LIST, new ArrayList<>());
        objectA.compareTo(objectB);
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testUnsupportedTypeComparison() {
        TypedObject objectA = new TypedObject(LONG_MAP_LIST, nestedList(singletonList(singletonMap("bar", 42L))));
        TypedObject objectB = new TypedObject(LONG_MAP_LIST, nestedList(singletonList(singletonMap("bar", 42L))));
        objectA.compareTo(objectB);
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testNullComparisonToOthers() {
        TypedObject objectA = new TypedObject(NULL, null);
        TypedObject objectB = new TypedObject(DOUBLE, 42.1);
        objectA.compareTo(objectB);
    }

    @Test
    public void testNullsFirstComparator() {
        List<TypedObject> objects = asList(TypedObject.NULL, new TypedObject(DOUBLE, 42.1),
                                           new TypedObject(INTEGER, 42), TypedObject.NULL, new TypedObject(NULL, null),
                                           new TypedObject(FLOAT, 100.3f), new TypedObject(LONG, 1L));
        objects.sort(TypedObject.nullsFirst());
        assertEquals(objects.get(0), TypedObject.NULL);
        assertEquals(objects.get(1), TypedObject.NULL);
        assertEquals(objects.get(2), TypedObject.NULL);
        assertEquals(objects.get(3), new TypedObject(LONG, 1L));
        assertEquals(objects.get(4), new TypedObject(INTEGER, 42));
        assertEquals(objects.get(5), new TypedObject(DOUBLE, 42.1));
        assertEquals(objects.get(6), new TypedObject(FLOAT, 100.3f));

        objects.sort(TypedObject.nullsFirst().reversed());
        assertEquals(objects.get(0), new TypedObject(FLOAT, 100.3f));
        assertEquals(objects.get(1), new TypedObject(DOUBLE, 42.1));
        assertEquals(objects.get(2), new TypedObject(INTEGER, 42));
        assertEquals(objects.get(3), new TypedObject(LONG, 1L));
        assertEquals(objects.get(4), TypedObject.NULL);
        assertEquals(objects.get(5), TypedObject.NULL);
        assertEquals(objects.get(6), TypedObject.NULL);
    }

    @Test
    public void testNullsLastComparator() {
        List<TypedObject> objects = asList(TypedObject.NULL, new TypedObject(DOUBLE, 42.1),
                                           new TypedObject(INTEGER, 42), TypedObject.NULL, new TypedObject(NULL, null),
                                           new TypedObject(FLOAT, 100.3f), new TypedObject(LONG, 1L));
        objects.sort(TypedObject.nullsLast());
        assertEquals(objects.get(0), new TypedObject(LONG, 1L));
        assertEquals(objects.get(1), new TypedObject(INTEGER, 42));
        assertEquals(objects.get(2), new TypedObject(DOUBLE, 42.1));
        assertEquals(objects.get(3), new TypedObject(FLOAT, 100.3f));
        assertEquals(objects.get(4), TypedObject.NULL);
        assertEquals(objects.get(5), TypedObject.NULL);
        assertEquals(objects.get(6), TypedObject.NULL);

        objects.sort(TypedObject.nullsLast().reversed());
        assertEquals(objects.get(0), TypedObject.NULL);
        assertEquals(objects.get(1), TypedObject.NULL);
        assertEquals(objects.get(2), TypedObject.NULL);
        assertEquals(objects.get(3), new TypedObject(FLOAT, 100.3f));
        assertEquals(objects.get(4), new TypedObject(DOUBLE, 42.1));
        assertEquals(objects.get(5), new TypedObject(INTEGER, 42));
        assertEquals(objects.get(6), new TypedObject(LONG, 1L));
    }

    @Test
    public void testToString() {
        TypedObject object;

        object = new TypedObject(null);
        assertEquals(object.getType(), NULL);
        assertEquals(object.toString(), Objects.toString(null) + "::NULL");

        object = new TypedObject("foo");
        assertEquals(object.getType(), STRING);
        assertEquals(object.toString(), "foo::STRING");

        object = new TypedObject(nestedList(singletonList(singletonMap("foo", "bar"))));
        assertEquals(object.getType(), Type.STRING_MAP_LIST);
        assertEquals(object.toString(), "[{foo=bar}]::STRING_MAP_LIST");

        object = new TypedObject(new ArrayList<>());
        assertEquals(object.getType(), UNKNOWN_LIST);
        assertEquals(object.toString(), "[]::UNKNOWN_LIST");

        object = new TypedObject(new HashMap<>());
        assertEquals(object.getType(), UNKNOWN_MAP);
        assertEquals(object.toString(), "{}::UNKNOWN_MAP");

        object = new TypedObject(nestedList(singletonList(emptyMap())));
        assertEquals(object.getType(), UNKNOWN_MAP_LIST);
        assertEquals(object.toString(), "[{}]::UNKNOWN_MAP_LIST");

        object = new TypedObject(nestedMap(singletonMap("foo", emptyMap())));
        assertEquals(object.getType(), UNKNOWN_MAP_MAP);
        assertEquals(object.toString(), "{foo={}}::UNKNOWN_MAP_MAP");
    }

    @Test
    public void testEquals() {
        assertFalse(new TypedObject(DOUBLE, 42.0).equals(null));
        assertFalse(new TypedObject(DOUBLE, 42.0).equals(42.0));
        assertEquals(new TypedObject(DOUBLE, 42.0), new TypedObject(DOUBLE, 42.0));
        assertNotEquals(new TypedObject(DOUBLE, 42.0), new TypedObject(FLOAT, 42.0f));
        assertEquals(new TypedObject(STRING_MAP, map(singletonMap("foo", "bar"))),
                     new TypedObject(STRING_MAP, map(singletonMap("foo", "bar"))));
        assertEquals(new TypedObject(STRING_MAP_LIST, null), new TypedObject(STRING_MAP_LIST, null));
        assertNotEquals(new TypedObject(STRING_MAP_MAP, null), new TypedObject(STRING_MAP_LIST, null));
        assertNotEquals(new TypedObject(STRING_MAP, map(singletonMap("foo", "bar"))), new TypedObject(STRING_MAP, null));
        assertNotEquals(new TypedObject(STRING_MAP, null), new TypedObject(STRING_MAP, map(singletonMap("foo", "bar"))));
        assertEquals(new TypedObject(nestedMap(singletonMap("foo", emptyMap()))),
                     new TypedObject(nestedMap(singletonMap("foo", emptyMap()))));
    }

    @Test
    public void testHashCode() {
        assertEquals(new TypedObject(STRING_MAP, map(singletonMap("foo", "bar"))).hashCode(),
                     new TypedObject(STRING_MAP, map(singletonMap("foo", "bar"))).hashCode());
        assertNotEquals(new TypedObject(DOUBLE, 42.0).hashCode(), new TypedObject(FLOAT, 42.0f).hashCode());
        assertEquals(new TypedObject(STRING_MAP_LIST, null).hashCode(),
                     new TypedObject(STRING_MAP_LIST, null).hashCode());
    }

    @Test
    public void testSafeCastingFromNull() {
        // From null to anything is possible
        assertEquals(safeCastFromObject(NULL, null), TypedObject.NULL);
        assertEquals(safeCastFromObject(UNKNOWN, null), TypedObject.UNKNOWN);
        assertEquals(safeCastFromObject(INTEGER, null), new TypedObject(INTEGER, null));
        assertEquals(safeCastFromObject(LONG, null), new TypedObject(LONG, null));
        assertEquals(safeCastFromObject(FLOAT, null), new TypedObject(FLOAT, null));
        assertEquals(safeCastFromObject(DOUBLE, null), new TypedObject(DOUBLE, null));
        assertEquals(safeCastFromObject(BOOLEAN, null), new TypedObject(BOOLEAN, null));
        assertEquals(safeCastFromObject(STRING_MAP, null), new TypedObject(STRING_MAP, null));
        assertEquals(safeCastFromObject(DOUBLE_MAP, null), new TypedObject(DOUBLE_MAP, null));
        assertEquals(safeCastFromObject(UNKNOWN_MAP, null), new TypedObject(UNKNOWN_MAP, null));
        assertEquals(safeCastFromObject(LONG_LIST, null), new TypedObject(LONG_LIST, null));
        assertEquals(safeCastFromObject(FLOAT_LIST, null), new TypedObject(FLOAT_LIST, null));
        assertEquals(safeCastFromObject(UNKNOWN_LIST, null), new TypedObject(UNKNOWN_LIST, null));
        assertEquals(safeCastFromObject(DOUBLE_MAP_MAP, null), new TypedObject(DOUBLE_MAP_MAP, null));
        assertEquals(safeCastFromObject(INTEGER_MAP_MAP, null), new TypedObject(INTEGER_MAP_MAP, null));
        assertEquals(safeCastFromObject(UNKNOWN_MAP_MAP, null), new TypedObject(UNKNOWN_MAP_MAP, null));
        assertEquals(safeCastFromObject(BOOLEAN_MAP_LIST, null), new TypedObject(BOOLEAN_MAP_LIST, null));
        assertEquals(safeCastFromObject(STRING_MAP_LIST, null), new TypedObject(STRING_MAP_LIST, null));
        assertEquals(safeCastFromObject(UNKNOWN_MAP_LIST, null), new TypedObject(UNKNOWN_MAP_LIST, null));
    }

    @Test
    public void testSafeCastingToString() {
        // To String from Primitives is possible
        assertEquals(safeCastFromObject(UNKNOWN, null), TypedObject.UNKNOWN);
        assertEquals(safeCastFromObject(STRING, null), new TypedObject(STRING, null));
        assertEquals(safeCastFromObject(STRING, 1), new TypedObject(STRING, "1"));
        assertEquals(safeCastFromObject(STRING, 1.0f), new TypedObject(STRING, "1.0"));
        assertEquals(safeCastFromObject(STRING, 1.0), new TypedObject(STRING, "1.0"));
        assertEquals(safeCastFromObject(STRING, "foo"), new TypedObject(STRING, "foo"));
        assertEquals(safeCastFromObject(STRING, false), new TypedObject(STRING, "false"));
        assertEquals(safeCastFromObject(STRING, list(singletonList("[1]"))), TypedObject.UNKNOWN);
    }

    @Test
    public void testSafeCastingToLong() {
        // To Long
        assertEquals(safeCastFromObject(LONG, Integer.MAX_VALUE), new TypedObject(LONG, (long) Integer.MAX_VALUE));
        assertEquals(safeCastFromObject(LONG, 1L), new TypedObject(LONG, 1L));
        assertEquals(safeCastFromObject(LONG, 1.0f), TypedObject.UNKNOWN);
        assertEquals(safeCastFromObject(LONG, Double.NaN), TypedObject.UNKNOWN);
    }

    @Test
    public void testSafeCastingToLongMap() {
        // To Long Map
        assertEquals(safeCastFromObject(LONG_MAP, map(singletonMap("foo", Integer.MAX_VALUE))),
                     new TypedObject(LONG_MAP, map(singletonMap("foo", (long) Integer.MAX_VALUE))));
        assertEquals(safeCastFromObject(LONG_MAP, map(singletonMap("foo", 1L))),
                     new TypedObject(LONG_MAP, map(singletonMap("foo", 1L))));
        assertEquals(safeCastFromObject(LONG_MAP, map(singletonMap("foo", 1.0f))), TypedObject.UNKNOWN);
        assertEquals(safeCastFromObject(LONG_MAP, map(singletonMap("foo", 1.0))), TypedObject.UNKNOWN);
    }

    @Test
    public void testSafeCastingToLongMapMap() {
        // To Long Map Map
        assertEquals(safeCastFromObject(LONG_MAP_MAP, nestedMap(singletonMap("foo", singletonMap("bar", 1)))),
                     new TypedObject(LONG_MAP_MAP, nestedMap(singletonMap("foo", singletonMap("bar", 1L)))));
        assertEquals(safeCastFromObject(LONG_MAP_MAP, nestedMap(singletonMap("foo", singletonMap("bar", 1L)))),
                     new TypedObject(LONG_MAP_MAP, nestedMap(singletonMap("foo", singletonMap("bar", 1L)))));
        assertEquals(safeCastFromObject(LONG_MAP_MAP, nestedMap(singletonMap("foo", singletonMap("bar", 1.0f)))),
                     TypedObject.UNKNOWN);
        assertEquals(safeCastFromObject(LONG_MAP_MAP, nestedMap(singletonMap("foo", singletonMap("bar", 1.0)))),
                     TypedObject.UNKNOWN);
    }

    @Test
    public void testSafeCastingToDouble() {
        // To Double
        assertEquals(safeCastFromObject(DOUBLE, 1), new TypedObject(DOUBLE, 1.0));
        assertEquals(safeCastFromObject(DOUBLE, 1L), new TypedObject(DOUBLE, 1.0));
        assertEquals(safeCastFromObject(DOUBLE, 1.0f), new TypedObject(DOUBLE, 1.0));
        assertEquals(safeCastFromObject(DOUBLE, 1.0), new TypedObject(DOUBLE, 1.0));
        assertEquals(safeCastFromObject(DOUBLE, "foo"), TypedObject.UNKNOWN);
        assertEquals(safeCastFromObject(DOUBLE, nestedList(singletonList(singletonMap("foo", 1.0)))), TypedObject.UNKNOWN);
    }

    @Test
    public void testSafeCastingToDoubleMap() {
        // To Double Map
        assertEquals(safeCastFromObject(DOUBLE_MAP, map(singletonMap("foo", 1))),
                     new TypedObject(DOUBLE_MAP, map(singletonMap("foo", 1.0))));
        assertEquals(safeCastFromObject(DOUBLE_MAP, map(singletonMap("foo", 1L))),
                     new TypedObject(DOUBLE_MAP, map(singletonMap("foo", 1.0))));
        assertEquals(safeCastFromObject(DOUBLE_MAP, map(singletonMap("foo", 1.0f))),
                     new TypedObject(DOUBLE_MAP, map(singletonMap("foo", 1.0))));
        assertEquals(safeCastFromObject(DOUBLE_MAP, map(singletonMap("foo", 1.0))),
                     new TypedObject(DOUBLE_MAP, map(singletonMap("foo", 1.0))));
        assertEquals(safeCastFromObject(DOUBLE_MAP, map(singletonMap("foo", "bar"))), TypedObject.UNKNOWN);
    }

    @Test
    public void testSafeCastingToDoubleMapMap() {
        // To Double Map Map
        assertEquals(safeCastFromObject(DOUBLE_MAP_MAP, nestedMap(singletonMap("foo", singletonMap("bar", 1)))),
                     new TypedObject(DOUBLE_MAP_MAP, nestedMap(singletonMap("foo", singletonMap("bar", 1.0)))));
        assertEquals(safeCastFromObject(DOUBLE_MAP_MAP, nestedMap(singletonMap("foo", singletonMap("bar", 1L)))),
                     new TypedObject(DOUBLE_MAP_MAP, nestedMap(singletonMap("foo", singletonMap("bar", 1.0)))));
        assertEquals(safeCastFromObject(DOUBLE_MAP_MAP, nestedMap(singletonMap("foo", singletonMap("bar", 1.0f)))),
                     new TypedObject(DOUBLE_MAP_MAP, nestedMap(singletonMap("foo", singletonMap("bar", 1.0)))));
        assertEquals(safeCastFromObject(DOUBLE_MAP_MAP, nestedMap(singletonMap("foo", singletonMap("bar", 1.0)))),
                     new TypedObject(DOUBLE_MAP_MAP, nestedMap(singletonMap("foo", singletonMap("bar", 1.0)))));
        assertEquals(safeCastFromObject(DOUBLE_MAP_MAP, nestedMap(singletonMap("foo", singletonMap("bar", "bar")))),
                     TypedObject.UNKNOWN);
    }

    @Test
    public void testFailSafeCasting() {
        assertEquals(safeCastFromObject(STRING_MAP, "{}"), TypedObject.UNKNOWN);
        assertEquals(safeCastFromObject(STRING_LIST, "[]"), TypedObject.UNKNOWN);
        assertEquals(safeCastFromObject(INTEGER_MAP_MAP, "{1:{2:3}}"), TypedObject.UNKNOWN);
        assertEquals(safeCastFromObject(LONG_MAP_LIST, "{1:{2:3}}"), TypedObject.UNKNOWN);
        assertEquals(safeCastFromObject(UNKNOWN_MAP, new HashMap<>()), TypedObject.UNKNOWN);
        assertEquals(safeCastFromObject(UNKNOWN_LIST, new ArrayList<>()), TypedObject.UNKNOWN);
        assertEquals(safeCastFromObject(UNKNOWN_MAP_MAP, nestedMap(singletonMap("foo", emptyMap()))), TypedObject.UNKNOWN);
        assertEquals(safeCastFromObject(UNKNOWN_MAP_LIST, nestedList(singletonList(emptyMap()))), TypedObject.UNKNOWN);
    }

    @Test
    public void testForceCastingToNumber() {
        Object value;

        TypedObject objectA = TypedObject.forceCastStringToNumber("42.1");
        value = objectA.getValue();
        assertEquals(objectA.getType(), DOUBLE);
        assertNotNull(value);
        assertTrue(value instanceof Number);
        assertEquals(value, 42.1);

        TypedObject objectB = TypedObject.forceCastStringToNumber("42");
        value = objectB.getValue();
        assertEquals(objectB.getType(), DOUBLE);
        assertNotNull(value);
        assertTrue(value instanceof Number);
        assertEquals(value, 42.0);

        TypedObject objectC = TypedObject.forceCastStringToNumber("{}");
        assertEquals(objectC.getType(), UNKNOWN);
        assertNull(objectC.getValue());

        TypedObject objectD = TypedObject.forceCastStringToNumber(new ArrayList<>());
        assertEquals(objectD.getType(), UNKNOWN);
        assertNull(objectD.getValue());

        TypedObject objectE = TypedObject.forceCastStringToNumber(new HashMap<>());
        assertEquals(objectE.getType(), UNKNOWN);
        assertNull(objectE.getValue());

        TypedObject objectF = TypedObject.forceCastStringToNumber(null);
        assertEquals(objectF.getType(), UNKNOWN);
        assertNull(objectF.getValue());
    }

    @Test
    public void testValueOf() {
        Assert.assertEquals(TypedObject.valueOf("abc"), new TypedObject("abc"));
        Assert.assertEquals(TypedObject.valueOf(true), TypedObject.TRUE);
        Assert.assertEquals(TypedObject.valueOf(false), TypedObject.FALSE);
        Assert.assertEquals(TypedObject.valueOf(1), new TypedObject(1));
        Assert.assertEquals(TypedObject.valueOf(2L), new TypedObject(2L));
        Assert.assertEquals(TypedObject.valueOf(3.0f), new TypedObject(3.0f));
        Assert.assertEquals(TypedObject.valueOf(4.0), new TypedObject(4.0));
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testValueOfNullString() {
        TypedObject.valueOf(null);
    }
}
