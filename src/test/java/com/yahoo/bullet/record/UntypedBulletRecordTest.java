/*
 *  Copyright 2021, Yahoo Inc.
 *  Licensed under the terms of the Apache License, Version 2.0.
 *  See the LICENSE file associated with the project for terms.
 */
package com.yahoo.bullet.record;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class UntypedBulletRecordTest {
    private static class UntypedTestBulletRecord extends UntypedBulletRecord {
        private Map<String, Serializable> data = new HashMap<>();

        @Override
        protected BulletRecord<Serializable> rawSet(String field, Serializable object) {
            return null;
        }

        @Override
        public Serializable get(String field) {
            return null;
        }

        @Override
        public boolean hasField(String field) {
            return false;
        }

        @Override
        public int fieldCount() {
            return 0;
        }

        @Override
        public Serializable getAndRemove(String field) {
            return null;
        }

        @Override
        public BulletRecord<Serializable> remove(String field) {
            return null;
        }

        @Override
        public BulletRecord<Serializable> copy() {
            return null;
        }

        @Override
        public Iterator<Map.Entry<String, Serializable>> iterator() {
            return data.entrySet().iterator();
        }
    }

    @Test
    public void testGetRawDataMap() {
        UntypedTestBulletRecord record = new UntypedTestBulletRecord();
        record.data.put("a", 0);
        record.data.put("b", 1);
        record.data.put("c", 2);

        Map<String, Serializable> map = record.getRawDataMap();
        Assert.assertEquals(map, record.data);
    }
}
