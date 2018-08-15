package com.yahoo.bullet.record;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;

public class BulletRecordConverterTest {

    private class SimplePOJO {
        private List<String> listA = new ArrayList<>();
    }

    @Test
    public void testFromSimplePOJO() {
        SimplePOJO simple = new SimplePOJO();
        simple.listA.add("abc");

    }

    @Test
    public void testFromSimpleMap() {
        Map<String, Object> data = new HashMap<>();
        data.put("A", true);
        data.put("B", 123);
        data.put("C", 456L);
        data.put("D", 7.89f);
        data.put("E", 0.12);
        data.put("F", "345");

        /*BulletRecord record = BulletRecordConverter.fromMap(data);
        Assert.assertEquals(record.get("A"), true);
        Assert.assertEquals(record.get("B"), 123);
        Assert.assertEquals(record.get("C"), 456L);
        Assert.assertEquals(record.get("D"), 7.89f);
        Assert.assertEquals(record.get("E"), 0.12);
        Assert.assertEquals(record.get("F"), "345");
        Assert.assertNull(record.get("G"));*/
    }

    @Test
    public void testFromMapWithList() {
        Map<String, Object> data = new HashMap<>();
        data.put("A", true);
        data.put("B", 123);
        data.put("C", 456L);
        data.put("D", singletonList(7.89f));
        data.put("E", singletonList(0.12));
        data.put("F", singletonList("345"));
        data.put("G", emptyList());

        /*BulletRecord record = BulletRecordConverter.fromMap(data);
        Assert.assertEquals(record.get("A"), true);
        Assert.assertEquals(record.get("B"), 123);
        Assert.assertEquals(record.get("C"), 456L);
        Assert.assertEquals(record.get("D"), singletonList(7.89f));
        Assert.assertEquals(record.get("E"), singletonList(0.12));
        Assert.assertEquals(record.get("F"), singletonList("345"));
        Assert.assertNull(record.get("G"));*/

    }

    @Test
    public void blah() {
        Map<String, Map<String, Boolean>> data = new HashMap<>();
        data.put("A", singletonMap("B", false));

        //data.getClass().
    }
}
