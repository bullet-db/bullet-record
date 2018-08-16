package com.yahoo.bullet.record;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.lang.reflect.Field;
import java.util.*;

import static java.util.Collections.singletonMap;

public class BulletRecordConverterTest {

    private static class Foo {
        private Integer myInt = 123;
        private Long myLong = 456L;
        private Boolean myBool = true;
        private String myString = "789";
        private Double myDouble = 0.12;
        private Float myFloat = 3.45f;

        private Integer getMyInt() { return myInt; }
        private Long getMyLong() { return myLong; }
        private Boolean getMyBool() { return myBool; }
        private String getMyString() { return myString; }
        private Double getMyDouble() { return myDouble; }
        private Float getMyFloat() { return myFloat; }
        private Integer throwMyInt() throws Exception { throw new Exception(); }
    }

    private static class Bar {
        private Map<String, Boolean> myBoolMap = new HashMap<>();
        private Map<String, Integer> myIntMap = new HashMap<>();
        private Map<String, Long> myLongMap = new HashMap<>();
        private Map<String, Double> myDoubleMap = new HashMap<>();
        private Map<String, Float> myFloatMap = new HashMap<>();
        private Map<String, String> myStringMap = new HashMap<>();
        private Map<String, Map<String, Boolean>> myBoolMapMap = new HashMap<>();
        private Map<String, Map<String, Integer>> myIntMapMap = new HashMap<>();
        private Map<String, Map<String, Long>> myLongMapMap = new HashMap<>();
        private Map<String, Map<String, Double>> myDoubleMapMap = new HashMap<>();
        private Map<String, Map<String, Float>> myFloatMapMap = new HashMap<>();
        private Map<String, Map<String, String>> myStringMapMap = new HashMap<>();
        private List<Boolean> myBoolList = new ArrayList<>();
        private List<Integer> myIntList = new ArrayList<>();
        private List<Long> myLongList = new ArrayList<>();
        private List<Double> myDoubleList = new ArrayList<>();
        private List<Float> myFloatList = new ArrayList<>();
        private List<String> myStringList = new ArrayList<>();
        private List<Map<String, Boolean>> myBoolMapList = new ArrayList<>();
        private List<Map<String, Integer>> myIntMapList = new ArrayList<>();
        private List<Map<String, Long>> myLongMapList = new ArrayList<>();
        private List<Map<String, Double>> myDoubleMapList = new ArrayList<>();
        private List<Map<String, Float>> myFloatMapList = new ArrayList<>();
        private List<Map<String, String>> myStringMapList = new ArrayList<>();
    }

    @Test
    public void testFromFoo() {
        Foo foo = new Foo();
        BulletRecordConverter<Foo> converter = BulletRecordConverter.from(Foo.class);

        BulletRecord record = converter.convert(foo);
        Assert.assertEquals(record.get("myInt"), 123);
        Assert.assertEquals(record.get("myLong"), 456L);
        Assert.assertEquals(record.get("myBool"), true);
        Assert.assertEquals(record.get("myString"), "789");
        Assert.assertEquals(record.get("myDouble"), 0.12);
        Assert.assertEquals(record.get("myFloat"), 3.45f);
        Assert.assertNull(record.get("bar"));

        // make some values null
        foo.myBool = null;
        foo.myString = null;
        foo.myFloat = null;

        record = converter.convert(foo);
        Assert.assertEquals(record.get("myInt"), 123);
        Assert.assertEquals(record.get("myLong"), 456L);
        Assert.assertNull(record.get("myBool"));
        Assert.assertNull(record.get("myString"));
        Assert.assertEquals(record.get("myDouble"), 0.12);
        Assert.assertNull(record.get("myFloat"));
        Assert.assertNull(record.get("bar"));
    }

    @Test
    public void testFromFooSchema() {
        Foo foo = new Foo();

        // This schema accepts lists all fields except myInt and also provides getters
        BulletRecordConverter<Foo> converter = BulletRecordConverter.from(Foo.class, "src/test/resources/foo_schema_simple.json");

        BulletRecord record = converter.convert(foo);
        Assert.assertNull(record.get("myInt"));
        Assert.assertEquals(record.get("myLong"), 456L);
        Assert.assertEquals(record.get("myBool"), true);
        Assert.assertEquals(record.get("myString"), "789");
        Assert.assertEquals(record.get("myDouble"), 0.12);
        Assert.assertEquals(record.get("myFloat"), 3.45f);
        Assert.assertNull(record.get("bar"));

        // make some values null
        foo.myBool = null;
        foo.myString = null;
        foo.myFloat = null;

        record = converter.convert(foo);
        Assert.assertNull(record.get("myInt"));
        Assert.assertEquals(record.get("myLong"), 456L);
        Assert.assertNull(record.get("myBool"));
        Assert.assertNull(record.get("myString"));
        Assert.assertEquals(record.get("myDouble"), 0.12);
        Assert.assertNull(record.get("myFloat"));
        Assert.assertNull(record.get("bar"));
    }

    @Test
    public void testFromBar() {
        Bar bar = new Bar();

        BulletRecordConverter<Bar> converter = BulletRecordConverter.from(Bar.class);

        // Lists and maps in record are not new objects
        BulletRecord record = converter.convert(bar);

        bar.myBoolMap.put("foo", true);
        bar.myBoolList.add(false);
        bar.myStringList.add("123");
        bar.myStringList.add("456");
        bar.myDoubleMap.put("hello", 0.12);
        bar.myIntMapMap.put("good", singletonMap("bye", 3));
        bar.myLongMapList.add(singletonMap("morning", 4L));

        Assert.assertEquals(record.get("myBoolMap"), bar.myBoolMap);
        Assert.assertEquals(record.get("myIntMap"), bar.myIntMap);
        Assert.assertEquals(record.get("myLongMap"), bar.myLongMap);
        Assert.assertEquals(record.get("myDoubleMap"), bar.myDoubleMap);
        Assert.assertEquals(record.get("myFloatMap"), bar.myFloatMap);
        Assert.assertEquals(record.get("myStringMap"), bar.myStringMap);
        Assert.assertEquals(record.get("myBoolMapMap"), bar.myBoolMapMap);
        Assert.assertEquals(record.get("myIntMapMap"), bar.myIntMapMap);
        Assert.assertEquals(record.get("myLongMapMap"), bar.myLongMapMap);
        Assert.assertEquals(record.get("myDoubleMapMap"), bar.myDoubleMapMap);
        Assert.assertEquals(record.get("myFloatMapMap"), bar.myFloatMapMap);
        Assert.assertEquals(record.get("myStringMapMap"), bar.myStringMapMap);
        Assert.assertEquals(record.get("myBoolList"), bar.myBoolList);
        Assert.assertEquals(record.get("myIntList"), bar.myIntList);
        Assert.assertEquals(record.get("myLongList"), bar.myLongList);
        Assert.assertEquals(record.get("myDoubleList"), bar.myDoubleList);
        Assert.assertEquals(record.get("myFloatList"), bar.myFloatList);
        Assert.assertEquals(record.get("myStringList"), bar.myStringList);
        Assert.assertEquals(record.get("myBoolMapList"), bar.myBoolMapList);
        Assert.assertEquals(record.get("myIntMapList"), bar.myIntMapList);
        Assert.assertEquals(record.get("myLongMapList"), bar.myLongMapList);
        Assert.assertEquals(record.get("myDoubleMapList"), bar.myDoubleMapList);
        Assert.assertEquals(record.get("myFloatMapList"), bar.myFloatMapList);
        Assert.assertEquals(record.get("myStringMapList"), bar.myStringMapList);
        Assert.assertNull(record.get("bar"));

        // make some values null
        bar.myLongList = null;
        bar.myFloatMapMap = null;

        record = converter.convert(bar);

        Assert.assertEquals(record.get("myBoolMap"), bar.myBoolMap);
        Assert.assertEquals(record.get("myIntMap"), bar.myIntMap);
        Assert.assertEquals(record.get("myLongMap"), bar.myLongMap);
        Assert.assertEquals(record.get("myDoubleMap"), bar.myDoubleMap);
        Assert.assertEquals(record.get("myFloatMap"), bar.myFloatMap);
        Assert.assertEquals(record.get("myStringMap"), bar.myStringMap);
        Assert.assertEquals(record.get("myBoolMapMap"), bar.myBoolMapMap);
        Assert.assertEquals(record.get("myIntMapMap"), bar.myIntMapMap);
        Assert.assertEquals(record.get("myLongMapMap"), bar.myLongMapMap);
        Assert.assertEquals(record.get("myDoubleMapMap"), bar.myDoubleMapMap);
        Assert.assertNull(record.get("myFloatMapMap"));
        Assert.assertEquals(record.get("myStringMapMap"), bar.myStringMapMap);
        Assert.assertEquals(record.get("myBoolList"), bar.myBoolList);
        Assert.assertEquals(record.get("myIntList"), bar.myIntList);
        Assert.assertNull(record.get("myLongList"));
        Assert.assertEquals(record.get("myDoubleList"), bar.myDoubleList);
        Assert.assertEquals(record.get("myFloatList"), bar.myFloatList);
        Assert.assertEquals(record.get("myStringList"), bar.myStringList);
        Assert.assertEquals(record.get("myBoolMapList"), bar.myBoolMapList);
        Assert.assertEquals(record.get("myIntMapList"), bar.myIntMapList);
        Assert.assertEquals(record.get("myLongMapList"), bar.myLongMapList);
        Assert.assertEquals(record.get("myDoubleMapList"), bar.myDoubleMapList);
        Assert.assertEquals(record.get("myFloatMapList"), bar.myFloatMapList);
        Assert.assertEquals(record.get("myStringMapList"), bar.myStringMapList);
        Assert.assertNull(record.get("bar"));
    }

    @Test
    public void testFromBarSchema() {
        Bar bar = new Bar();

        BulletRecordConverter<Bar> converter = BulletRecordConverter.from(Bar.class, "src/test/resources/bar_schema_simple.json");

        // Lists and maps in record are not new objects
        BulletRecord record = converter.convert(bar);

        bar.myBoolMap.put("foo", true);
        bar.myBoolList.add(false);
        bar.myStringList.add("123");
        bar.myStringList.add("456");
        bar.myDoubleMap.put("hello", 0.12);
        bar.myIntMapMap.put("good", singletonMap("bye", 3));
        bar.myLongMapList.add(singletonMap("morning", 4L));

        Assert.assertEquals(record.get("myBoolMap"), bar.myBoolMap);
        Assert.assertNull(record.get("myIntMap"));
        Assert.assertNull(record.get("myLongMap"));
        Assert.assertEquals(record.get("myDoubleMap"), bar.myDoubleMap);
        Assert.assertNull(record.get("myFloatMap"));
        Assert.assertNull(record.get("myStringMap"));
        Assert.assertNull(record.get("myBoolMapMap"));
        Assert.assertEquals(record.get("myIntMapMap"), bar.myIntMapMap);
        Assert.assertNull(record.get("myLongMapMap"));
        Assert.assertNull(record.get("myDoubleMapMap"));
        Assert.assertNull(record.get("myFloatMapMap"));
        Assert.assertNull(record.get("myStringMapMap"));
        Assert.assertEquals(record.get("myBoolList"), bar.myBoolList);
        Assert.assertNull(record.get("myIntList"));
        Assert.assertNull(record.get("myLongList"));
        Assert.assertNull(record.get("myDoubleList"));
        Assert.assertNull(record.get("myFloatList"));
        Assert.assertEquals(record.get("myStringList"), bar.myStringList);
        Assert.assertNull(record.get("myBoolMapList"));
        Assert.assertNull(record.get("myIntMapList"));
        Assert.assertEquals(record.get("myLongMapList"), bar.myLongMapList);
        Assert.assertNull(record.get("myDoubleMapList"));
        Assert.assertNull(record.get("myFloatMapList"));
        Assert.assertNull(record.get("myStringMapList"));
        Assert.assertNull(record.get("bar"));

        // make some values null
        bar.myBoolMap = null;
        bar.myBoolList = null;

        record = converter.convert(bar);

        Assert.assertNull(record.get("myBoolMap"));
        Assert.assertNull(record.get("myIntMap"));
        Assert.assertNull(record.get("myLongMap"));
        Assert.assertEquals(record.get("myDoubleMap"), bar.myDoubleMap);
        Assert.assertNull(record.get("myFloatMap"));
        Assert.assertNull(record.get("myStringMap"));
        Assert.assertNull(record.get("myBoolMapMap"));
        Assert.assertEquals(record.get("myIntMapMap"), bar.myIntMapMap);
        Assert.assertNull(record.get("myLongMapMap"));
        Assert.assertNull(record.get("myDoubleMapMap"));
        Assert.assertNull(record.get("myFloatMapMap"));
        Assert.assertNull(record.get("myStringMapMap"));
        Assert.assertNull(record.get("myBoolList"));
        Assert.assertNull(record.get("myIntList"));
        Assert.assertNull(record.get("myLongList"));
        Assert.assertNull(record.get("myDoubleList"));
        Assert.assertNull(record.get("myFloatList"));
        Assert.assertEquals(record.get("myStringList"), bar.myStringList);
        Assert.assertNull(record.get("myBoolMapList"));
        Assert.assertNull(record.get("myIntMapList"));
        Assert.assertEquals(record.get("myLongMapList"), bar.myLongMapList);
        Assert.assertNull(record.get("myDoubleMapList"));
        Assert.assertNull(record.get("myFloatMapList"));
        Assert.assertNull(record.get("myStringMapList"));
        Assert.assertNull(record.get("bar"));
    }

    @Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = "Object contains a listed field.*")
    public void testNotValidType() {
        class Dummy {
            Byte myInt;
        }
        BulletRecordConverter.from(Dummy.class, "src/test/resources/throw.json");
    }

    @Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = "Object is missing listed field.*")
    public void testNoSuchField() {
        class Dummy {

        }
        BulletRecordConverter.from(Dummy.class, "src/test/resources/throw.json");
    }

    @Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = "Listed getter return type.*")
    public void testNotValidGetter() {
        class Dummy {
            Integer myInt;
            void throwMyInt() {}
        }
        BulletRecordConverter.from(Dummy.class, "src/test/resources/throw.json");
    }

    @Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = "Object is missing listed getter.*")
    public void testNoSuchGetter() {
        class Dummy {
            Integer myInt;
        }
        BulletRecordConverter.from(Dummy.class, "src/test/resources/throw.json");
    }

    @Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = "Object contains a field with an unsupported type: myMap")
    public void testMapNotStringKey() {
        class Dummy {
            Map<Integer, String> myMap;
        }
        // Map key needs to be String
        BulletRecordConverter.from(Dummy.class);
    }

    @Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = "Object contains a field with an unsupported type: myListMap")
    public void testMapOfNotMap() {
        class Dummy {
            Map<String, List<String>> myListMap;
        }
        // Doesn't support Map of List
        BulletRecordConverter.from(Dummy.class);
    }

    @Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = "Object contains a field with an unsupported type: myMapMap")
    public void testMapOfMapNotStringKey() {
        class Dummy {
            Map<String, Map<Integer, String>> myMapMap;
        }
        // Inner Map key needs to be String
        BulletRecordConverter.from(Dummy.class);
    }

    @Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = "Object contains a field with an unsupported type: myByteMapMap")
    public void testMapOfMapNotPrimitiveValue() {
        class Dummy {
            Map<String, Map<String, Byte>> myByteMapMap;
        }
        // Inner Map value needs to be a supported primitive
        BulletRecordConverter.from(Dummy.class);
    }

    @Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = "Object contains a field with an unsupported type: myListMapMap")
    public void testMapOfMapParameterizedType() {
        class Dummy {
            Map<String, Map<String, List<String>>> myListMapMap;
        }
        // ClassCastException if inner value is a parameterized type <>
        BulletRecordConverter.from(Dummy.class);
    }

    @Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = "Object contains a field with an unsupported type: myList")
    public void testUnsupportedType() {
        class Dummy {
            ArrayList<String> myList;
        }
        // ArrayList doesn't count as List
        BulletRecordConverter.from(Dummy.class);
    }

    @Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = "Object contains a field with an unsupported type: myListList")
    public void testListOfNotMap() {
        class Dummy {
            List<List<String>> myListList;
        }
        // Doesn't support List of List
        BulletRecordConverter.from(Dummy.class);
    }

    @Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = "Object contains a field with an unsupported type: myMapList")
    public void testListOfMapNotStringKey() {
        class Dummy {
            List<Map<Integer, String>> myMapList;
        }
        // Map key needs to be String
        BulletRecordConverter.from(Dummy.class);
    }

    @Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = "Object contains a field with an unsupported type: myByteMapList")
    public void testListOfMapNotPrimitiveValue() {
        class Dummy {
            List<Map<String, Byte>> myByteMapList;
        }
        // Map value needs to be a supported primitive
        BulletRecordConverter.from(Dummy.class);
    }

    @Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = "Object contains a field with an unsupported type: myListMapList")
    public void testListOfMapParameterizedType() {
        class Dummy {
            List<Map<String, List<String>>> myListMapList;
        }
        // ClassCastException if inner value is a parameterized type <>
        BulletRecordConverter.from(Dummy.class);
    }

    @Test
    public void testEmptyClass() {
        class Dummy {

        }
        // Ok
        BulletRecord record = BulletRecordConverter.from(Dummy.class).convert(new Dummy());
        Assert.assertEquals(record.fieldCount(), 0);
    }

    @Test
    public void testEmptySchema() {
        // Without empty schema
        BulletRecord record = BulletRecordConverter.from(Foo.class).convert(new Foo());
        Assert.assertEquals(record.fieldCount(), 6);

        // Ok
        record = BulletRecordConverter.from(Foo.class, "src/test/resources/empty.json").convert(new Foo());
        Assert.assertEquals(record.fieldCount(), 0);
    }

    @Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = "does-not-exist\\.json file not found\\.")
    public void testMissingSchema() {
        BulletRecordConverter.from(Foo.class, "does-not-exist.json");
    }

    @Test(expectedExceptions = Exception.class)
    public void testBadSchema() {
        // Gson exception
        BulletRecordConverter.from(Foo.class, "src/test/resources/bad_schema.json");
    }

    @Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = "Object contains inaccessible field\\.")
    public void testConvertInaccessible() {
        // Should never happen really
        BulletRecordConverter<AnotherDummy> converter = BulletRecordConverter.from(AnotherDummy.class);
        try {
            Field f = converter.getClass().getDeclaredField("fields");
            f.setAccessible(true);
            ((List<Field>) f.get(converter)).get(0).setAccessible(false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        converter.convert(new AnotherDummy());
    }

    @Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = "Exception thrown by getter.")
    public void testConvertGetterThrows() {
        BulletRecordConverter<Foo> converter = BulletRecordConverter.from(Foo.class, "src/test/resources/throw.json");
        converter.convert(new Foo());
    }

    @Test
    public void testBulletRecordField() {
        // For coverage
        new BulletRecordConverter.BulletRecordField();
    }
}
