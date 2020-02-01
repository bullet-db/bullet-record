/*
 *  Copyright 2017, Yahoo Inc.
 *  Licensed under the terms of the Apache License, Version 2.0.
 *  See the LICENSE file associated with the project for terms.
 */
package com.yahoo.bullet.typesystem;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

public class TypeTest {
    @Test
    public void testCurrentTypes() {
        Assert.assertEquals(new HashSet<>(Type.PRIMITIVES),
                            new HashSet<>(Arrays.asList(Type.INTEGER, Type.LONG, Type.BOOLEAN, Type.FLOAT, Type.DOUBLE, Type.STRING)));
        Assert.assertEquals(new HashSet<>(Type.NUMERICS),
                            new HashSet<>(Arrays.asList(Type.INTEGER, Type.LONG, Type.FLOAT, Type.DOUBLE)));
    }

    @Test
    public void testTypeGetting() {
        Assert.assertEquals(Type.getType(null), Type.NULL);
        Assert.assertEquals(Type.getType(true), Type.BOOLEAN);
        Assert.assertEquals(Type.getType("foo"), Type.STRING);
        Assert.assertEquals(Type.getType(1), Type.INTEGER);
        Assert.assertEquals(Type.getType(1L), Type.LONG);
        Assert.assertEquals(Type.getType(3.14F), Type.FLOAT);
        Assert.assertEquals(Type.getType(1.2), Type.DOUBLE);
        Assert.assertEquals(Type.getType('8'), Type.UNKNOWN);
        Assert.assertEquals(Type.getType(new HashSet<String>()), Type.UNKNOWN);
    }

    @Test
    public void testBooleanCasting() {
        Assert.assertEquals(Type.BOOLEAN.castString("true"), true);
        Assert.assertEquals(Type.BOOLEAN.castString("false"), false);
        Assert.assertEquals(Type.BOOLEAN.castString("foo"), false);
        Assert.assertEquals(Type.BOOLEAN.castString("1"), false);
    }

    @Test
    public void testNullCasting() {
        Assert.assertEquals(Type.NULL.castString("null"), null);
        Assert.assertEquals(Type.NULL.castString("NULL"), null);
        Assert.assertEquals(Type.NULL.castString("Null"), null);
        Assert.assertEquals(Type.NULL.castString("false"), "false");
        Assert.assertEquals(Type.NULL.castString("42"), "42");
    }

    @Test
    public void testStringCasting() {
        Assert.assertEquals(Type.STRING.castString("1"), "1");
        Assert.assertEquals(Type.STRING.castString("foo"), "foo");
        Assert.assertEquals(Type.STRING.castString("true"), "true");
        Assert.assertEquals(Type.STRING.castString("1.23"), "1.23");
    }

    @Test(expectedExceptions = ClassCastException.class)
    public void testUnknownCasting() {
        Assert.assertEquals(Type.UNKNOWN.castString("1"), "1");
    }

    @Test
    public void testIntegerCasting() {
        Assert.assertEquals(Type.INTEGER.castString("41"), 41);
    }

    @Test(expectedExceptions = NumberFormatException.class)
    public void testIntegerFailCastingDouble() {
        Type.INTEGER.castString("41.99");
    }

    @Test(expectedExceptions = NumberFormatException.class)
    public void testIntegerFailCastingString() {
        Type.INTEGER.castString("foo");
    }

    @Test
    public void testLongCasting() {
        Assert.assertEquals(Type.LONG.castString("41"), 41L);
    }

    @Test(expectedExceptions = NumberFormatException.class)
    public void testLongFailCastingDouble() {
        Type.LONG.castString("41.99");
    }

    @Test(expectedExceptions = NumberFormatException.class)
    public void testLongFailCastingString() {
        Type.LONG.castString("foo");
    }

    @Test
    public void testFloatCasting() {
        Assert.assertEquals(Type.FLOAT.castString("42.0"), 42.0f);
        Assert.assertEquals(Type.FLOAT.castString("42"), 42.0f);
    }

    @Test(expectedExceptions = NumberFormatException.class)
    public void testFloatFailCastingString() {
        Type.FLOAT.castString("foo");
    }

    @Test
    public void testDoubleCasting() {
        Assert.assertEquals(Type.DOUBLE.castString("42.0"), 42.0);
        Assert.assertEquals(Type.DOUBLE.castString("42"), 42.0);
    }

    @Test(expectedExceptions = NumberFormatException.class)
    public void testDoubleFailCastingString() {
        Type.DOUBLE.castString("foo");
    }

    @Test(expectedExceptions = ClassCastException.class)
    public void testListUnsupportedCasting() {
        Type.LIST.cast(Collections.emptyList().toString());
    }

    @Test(expectedExceptions = ClassCastException.class)
    public void testMapUnsupportedCasting() {
        Type.MAP.cast(Collections.emptyMap().toString());
    }

}
