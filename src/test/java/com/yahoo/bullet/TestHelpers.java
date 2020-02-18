/*
 *  Copyright 2020, Yahoo Inc.
 *  Licensed under the terms of the Apache License, Version 2.0.
 *  See the LICENSE file associated with the project for terms.
 */
package com.yahoo.bullet;

import org.testng.Assert;

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
}
