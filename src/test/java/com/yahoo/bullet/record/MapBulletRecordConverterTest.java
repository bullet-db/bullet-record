package com.yahoo.bullet.record;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MapBulletRecordConverterTest {

    @Test
    public void testEverything() {
        Map<String, Object> map = new HashMap<>();
        map.put("myBool", true);
        map.put("myInt", 123);
        map.put("myLong", 456L);
        map.put("myFloat", 7.89f);
        map.put("myDouble", 0.12);
        map.put("myString", "345");
        map.put("myBoolMap", Collections.singletonMap("a", false));
        map.put("myIntMap", Collections.singletonMap("b", 2));
        map.put("myLongMap", Collections.singletonMap("c", 3L));
        map.put("myFloatMap", Collections.singletonMap("d", 4.0f));
        map.put("myDoubleMap", Collections.singletonMap("e", 5.0));
        map.put("myStringMap", Collections.singletonMap("f", "6"));
        map.put("myBoolMapMap", Collections.singletonMap("g", Collections.singletonMap("h", true)));
        map.put("myIntMapMap", Collections.singletonMap("i", Collections.singletonMap("j", 8)));
        map.put("myLongMapMap", Collections.singletonMap("k", Collections.singletonMap("l", 9L)));
        map.put("myFloatMapMap", Collections.singletonMap("m", Collections.singletonMap("n", 10.0f)));
        map.put("myDoubleMapMap", Collections.singletonMap("o", Collections.singletonMap("p", 11.0)));
        map.put("myStringMapMap", Collections.singletonMap("q", Collections.singletonMap("r", "12")));
        map.put("myBoolList", Collections.singletonList(false));
        map.put("myIntList", Collections.singletonList(14));
        map.put("myLongList", Collections.singletonList(15L));
        map.put("myFloatList", Collections.singletonList(16.0f));
        map.put("myDoubleList", Collections.singletonList(17.0));
        map.put("myStringList", Collections.singletonList("18"));
        map.put("myBoolMapList", Collections.singletonList(Collections.singletonMap("s", true)));
        map.put("myIntMapList", Collections.singletonList(Collections.singletonMap("t", 20)));
        map.put("myLongMapList", Collections.singletonList(Collections.singletonMap("u", 21L)));
        map.put("myFloatMapList", Collections.singletonList(Collections.singletonMap("v", 22.0f)));
        map.put("myDoubleMapList", Collections.singletonList(Collections.singletonMap("w", 23.0)));
        map.put("myStringMapList", Collections.singletonList(Collections.singletonMap("x", "24")));

        MapBulletRecordConverter converter = MapBulletRecordConverter.from("src/test/resources/map_schema.json");
        BulletRecord record = converter.convert(map);

        Assert.assertEquals(record.get("myBool"), map.get("myBool"));
        Assert.assertEquals(record.get("myInt"), map.get("myInt"));
        Assert.assertEquals(record.get("myLong"), map.get("myLong"));
        Assert.assertEquals(record.get("myFloat"), map.get("myFloat"));
        Assert.assertEquals(record.get("myDouble"), map.get("myDouble"));
        Assert.assertEquals(record.get("myString"), map.get("myString"));
        Assert.assertEquals(record.get("myBoolMap"), map.get("myBoolMap"));
        Assert.assertEquals(record.get("myIntMap"), map.get("myIntMap"));
        Assert.assertEquals(record.get("myLongMap"), map.get("myLongMap"));
        Assert.assertEquals(record.get("myFloatMap"), map.get("myFloatMap"));
        Assert.assertEquals(record.get("myDoubleMap"), map.get("myDoubleMap"));
        Assert.assertEquals(record.get("myStringMap"), map.get("myStringMap"));
        Assert.assertEquals(record.get("myBoolMapMap"), map.get("myBoolMapMap"));
        Assert.assertEquals(record.get("myIntMapMap"), map.get("myIntMapMap"));
        Assert.assertEquals(record.get("myLongMapMap"), map.get("myLongMapMap"));
        Assert.assertEquals(record.get("myFloatMapMap"), map.get("myFloatMapMap"));
        Assert.assertEquals(record.get("myDoubleMapMap"), map.get("myDoubleMapMap"));
        Assert.assertEquals(record.get("myStringMapMap"), map.get("myStringMapMap"));
        Assert.assertEquals(record.get("myBoolList"), map.get("myBoolList"));
        Assert.assertEquals(record.get("myIntList"), map.get("myIntList"));
        Assert.assertEquals(record.get("myLongList"), map.get("myLongList"));
        Assert.assertEquals(record.get("myFloatList"), map.get("myFloatList"));
        Assert.assertEquals(record.get("myDoubleList"), map.get("myDoubleList"));
        Assert.assertEquals(record.get("myStringList"), map.get("myStringList"));
        Assert.assertEquals(record.get("myBoolMapList"), map.get("myBoolMapList"));
        Assert.assertEquals(record.get("myIntMapList"), map.get("myIntMapList"));
        Assert.assertEquals(record.get("myLongMapList"), map.get("myLongMapList"));
        Assert.assertEquals(record.get("myFloatMapList"), map.get("myFloatMapList"));
        Assert.assertEquals(record.get("myDoubleMapList"), map.get("myDoubleMapList"));
        Assert.assertEquals(record.get("myStringMapList"), map.get("myStringMapList"));
        Assert.assertNull(record.get("dne"));
    }

    @Test
    public void testEnums() {
        Map<String, Object> map = new HashMap<>();
        Map<String, Boolean> myBoolMap = new HashMap<>();
        myBoolMap.put("myBool", true);
        myBoolMap.put("notMyBool", false);
        map.put("myBoolMap", myBoolMap);

        BulletRecord record = MapBulletRecordConverter.from("src/test/resources/enums.json").convert(map);
        Assert.assertTrue(((Map<String, Boolean>) record.get("myBoolMap")).get("myBool"));
        Assert.assertNull(((Map<String, Boolean>) record.get("myBoolMap")).get("notMyBool"));
    }

    @Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = "does-not-exist\\.json file not found\\.")
    public void testMissingSchema() {
        MapBulletRecordConverter.from("does-not-exist.json");
    }

    @Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = "'name' is null\\.")
    public void testMissingName() {
        MapBulletRecordConverter.from("src/test/resources/missing_name.json");
    }

    @Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = "'type' is either.*")
    public void testBadType() {
        MapBulletRecordConverter.from("src/test/resources/bad_type.json");
    }

    @Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = "'subtype' is either.*")
    public void testBadSubtype() {
        MapBulletRecordConverter.from("src/test/resources/bad_subtype.json");
    }

    @Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = "MAPOFMAP is not an appropriate 'subtype'\\.")
    public void testSubtypeNotPrimitive() {
        MapBulletRecordConverter.from("src/test/resources/not_primitive.json");
    }

    @Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = "'enumerations' array in schema is empty\\.")
    public void testEmptyEnumerations() {
        MapBulletRecordConverter.from("src/test/resources/empty_enums.json");
    }

    @Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = "'enumerations' array in schema contains an empty string\\.")
    public void testEnumerationsEmptyName() {
        MapBulletRecordConverter.from("src/test/resources/enums_empty_name.json");
    }

    @Test(expectedExceptions = Exception.class)
    public void testBadSchema() {
        // Gson exception
        MapBulletRecordConverter.from("src/test/resources/bad_schema.json");
    }
}
