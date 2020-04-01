/*
 *  Copyright 2020, Yahoo Inc.
 *  Licensed under the terms of the Apache License, Version 2.0.
 *  See the LICENSE file associated with the project for terms.
 */
package com.yahoo.bullet;

import org.testng.Assert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class TestHelpers {
    public static <T extends Exception> void assertException(Runnable statement, String expectedExceptionRegex) {
        try {
            statement.run();
        } catch (Exception e) {
            if (expectedExceptionRegex != null) {
                Assert.assertTrue(Pattern.matches(expectedExceptionRegex, e.toString()));
            }
            return;
        }
        throw new RuntimeException("Expected a RuntimeException with " + expectedExceptionRegex);
    }

    public static <T> ArrayList<T> list(List<T> list) {
        return new ArrayList<>(list);
    }

    public static <T> ArrayList<HashMap<String, T>> nestedList(List<Map<String, T>> list) {
        ArrayList<HashMap<String, T>> newList = new ArrayList<>();
        list.forEach(e -> newList.add(map(e)));
        return newList;
    }

    public static <T> HashMap<String, T> map(Map<String, T> map) {
        return new HashMap<>(map);
    }

    public static <T> HashMap<String, HashMap<String, T>> nestedMap(Map<String, Map<String, T>> map) {
        HashMap<String, HashMap<String, T>> newMap = new HashMap<>();
        map.forEach((key, value) -> newMap.put(key, map(value)));
        return newMap;
    }
}
