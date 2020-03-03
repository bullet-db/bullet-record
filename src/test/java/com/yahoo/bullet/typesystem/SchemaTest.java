/*
 *  Copyright 2020, Yahoo Inc.
 *  Licensed under the terms of the Apache License, Version 2.0.
 *  See the LICENSE file associated with the project for terms.
 */
package com.yahoo.bullet.typesystem;

import com.yahoo.bullet.typesystem.Schema.DetailedField;
import com.yahoo.bullet.typesystem.Schema.DetailedMapField;
import com.yahoo.bullet.typesystem.Schema.DetailedMapListField;
import com.yahoo.bullet.typesystem.Schema.DetailedMapMapField;
import com.yahoo.bullet.typesystem.Schema.Field;
import com.yahoo.bullet.typesystem.Schema.Parser;
import com.yahoo.bullet.typesystem.Schema.PlainField;
import com.yahoo.bullet.typesystem.Schema.SubField;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.yahoo.bullet.TestHelpers.assertException;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

public class SchemaTest {
    private static final Map<String, Field> FIELDS = new LinkedHashMap<>();
    private static final List<Field> ALL = new ArrayList<>();
    private static final List<DetailedField> DETAILED_FIELDS = new ArrayList<>();
    private static final List<DetailedMapField> DETAILED_MAP_FIELDS = new ArrayList<>();
    private static final List<DetailedMapMapField> DETAILED_MAP_MAP_FIELDS = new ArrayList<>();
    private static final List<DetailedMapListField> DETAILED_MAP_LIST_FIELDS = new ArrayList<>();
    static {
        FIELDS.put("a", new PlainField("a", Type.STRING));
        FIELDS.put("b", new PlainField("b", Type.INTEGER));
        FIELDS.put("c", new PlainField("c", Type.FLOAT));
        FIELDS.put("d", new PlainField("d", Type.DOUBLE));
        FIELDS.put("e", new PlainField("e", Type.LONG));
        FIELDS.put("f", new PlainField("f", Type.BOOLEAN));
        FIELDS.put("g", new PlainField("g", Type.STRING_LIST));
        FIELDS.put("h", new DetailedField("h", Type.DOUBLE_MAP, "This is a detailed map of doubles"));
        FIELDS.put("i", new PlainField("i", Type.FLOAT_MAP_MAP));
        FIELDS.put("j", new PlainField("j", Type.INTEGER_MAP_LIST));
        FIELDS.put("k", new DetailedField("k", Type.BOOLEAN_LIST, "This is a detailed list of booleans"));
        FIELDS.put("l", new DetailedMapField("l", Type.INTEGER_MAP, "This is a detailed map of integers",
                                             asList(new SubField("a", null), new SubField("b", ""),
                                                    new SubField("c", "This is an integer sub-field"))));
        FIELDS.put("m", new DetailedMapMapField("m", Type.STRING_MAP_MAP,
                                                "This is a detailed map of map of strings",
                                                asList(new SubField("A", "This is a map sub-field"),
                                                       new SubField("B", "This is another map sub-field")),
                                                singletonList(new SubField("a", "This is a string sub-sub-field"))));
        FIELDS.put("n", new DetailedMapMapField("n", Type.LONG_MAP_MAP, "This is a detailed map of map of longs", null,
                                                singletonList(new SubField("a", "This is a long sub-sub-field"))));
        FIELDS.put("o", new DetailedMapListField("o", Type.FLOAT_MAP_LIST, "This is a detailed list of map of floats",
                                                 singletonList(new SubField("a", "This is a float sub-list-field"))));
        FIELDS.put("p", new DetailedMapField("p", Type.LONG_MAP_MAP, "This is a detailed map of map of longs but enumerates only the first level",
                                             singletonList(new SubField("a", "This is a map of longs sub-field"))));

        ALL.addAll(FIELDS.values());

        DETAILED_FIELDS.add((DetailedField) FIELDS.get("h"));
        DETAILED_FIELDS.add((DetailedField) FIELDS.get("k"));
        DETAILED_FIELDS.add((DetailedField) FIELDS.get("l"));
        DETAILED_FIELDS.add((DetailedField) FIELDS.get("m"));
        DETAILED_FIELDS.add((DetailedField) FIELDS.get("n"));
        DETAILED_FIELDS.add((DetailedField) FIELDS.get("o"));
        DETAILED_FIELDS.add((DetailedField) FIELDS.get("p"));

        DETAILED_MAP_FIELDS.add((DetailedMapField) FIELDS.get("l"));
        DETAILED_MAP_FIELDS.add((DetailedMapField) FIELDS.get("m"));
        DETAILED_MAP_FIELDS.add((DetailedMapField) FIELDS.get("n"));
        DETAILED_MAP_FIELDS.add((DetailedMapField) FIELDS.get("p"));

        DETAILED_MAP_MAP_FIELDS.add((DetailedMapMapField) FIELDS.get("m"));
        DETAILED_MAP_MAP_FIELDS.add((DetailedMapMapField) FIELDS.get("n"));

        DETAILED_MAP_LIST_FIELDS.add((DetailedMapListField) FIELDS.get("o"));
    }

    private static void assertEquals(List<SubField> actual, List<SubField> expected) {
        if (actual == null) {
            Assert.assertNull(expected);
        } else if (actual.isEmpty()) {
            Assert.assertTrue(expected.isEmpty());
        } else {
            Assert.assertEquals(actual.size(), expected.size());
            Iterator<SubField> actualFields = actual.iterator();
            Iterator<SubField> expectedFields = expected.iterator();
            while (actualFields.hasNext() && expectedFields.hasNext()) {
                SubField actualField = actualFields.next();
                SubField expectedField = expectedFields.next();
                if (actualField == null) {
                    Assert.assertNull(expectedField);
                } else if (expectedField == null) {
                    Assert.fail("Expected actual subfield to be null " + actualField);
                } else {
                    Assert.assertEquals(actualField.getName(), expectedField.getName());
                    Assert.assertEquals(actualField.getDescription(), expectedField.getDescription());
                }
            }
        }
    }

    private static <T extends Field> void assertEquals(T actualField, T expectedField) {
        if (actualField == null) {
            Assert.assertNull(expectedField);
        } else if (expectedField == null) {
            Assert.fail("Expected null but found " + actualField);
        } else {
            if (actualField instanceof PlainField) {
                PlainField a = (PlainField) actualField;
                PlainField e = (PlainField) expectedField;
                Assert.assertEquals(a.getName(), e.getName());
                Assert.assertEquals(a.getType(), e.getType());
            }
            if (actualField instanceof DetailedField) {
                DetailedField a = (DetailedField) actualField;
                DetailedField e = (DetailedField) expectedField;
                Assert.assertEquals(a.getDescription(), e.getDescription());
            }
            if (actualField instanceof DetailedMapField) {
                DetailedMapField a = (DetailedMapField) actualField;
                DetailedMapField e = (DetailedMapField) expectedField;
                assertEquals(a.getSubFields(), e.getSubFields());
            }
            if (actualField instanceof DetailedMapMapField) {
                DetailedMapMapField a = (DetailedMapMapField) actualField;
                DetailedMapMapField e = (DetailedMapMapField) expectedField;
                assertEquals(a.getSubSubFields(), e.getSubSubFields());
            }
            if (actualField instanceof DetailedMapListField) {
                DetailedMapListField a = (DetailedMapListField) actualField;
                DetailedMapListField e = (DetailedMapListField) expectedField;
                assertEquals(a.getSubListFields(), e.getSubListFields());
            }
        }
    }

    private static <T extends Field> void assertEquals(Collection<T> actual, Collection<T> expected) {
        if (actual == null) {
            Assert.assertNull(expected);
        } else if (actual.isEmpty()) {
            Assert.assertTrue(expected.isEmpty());
        } else {
            Assert.assertEquals(actual.size(), expected.size());
            Iterator<T> actualFields = actual.iterator();
            Iterator<T> expectedFields = expected.iterator();
            while (actualFields.hasNext() && expectedFields.hasNext()) {
                T actualField = actualFields.next();
                T expectedField = expectedFields.next();
                assertEquals(actualField, expectedField);
            }
        }
    }

    @Test
    public void testCreationFromClassPath() {
        Schema schema = new Schema("test-schema.json");
        Assert.assertNotNull(schema);
        Assert.assertEquals(schema.size(), FIELDS.size());
        assertEquals(schema.getFields(), ALL);
        assertEquals(schema.getDetailedFields(), DETAILED_FIELDS);
        assertEquals(schema.getDetailedMapFields(), DETAILED_MAP_FIELDS);
        assertEquals(schema.getDetailedMapMapFields(), DETAILED_MAP_MAP_FIELDS);
        assertEquals(schema.getDetailedMapListFields(), DETAILED_MAP_LIST_FIELDS);
        Assert.assertEquals(schema.getTypes(), ALL.stream().map(Field::getType).collect(Collectors.toSet()));
    }

    @Test
    public void testCreationFromList() {
        Schema schema = new Schema(new ArrayList<>(FIELDS.values()));
        assertEquals(schema.getFields(), ALL);
        assertEquals(schema.getDetailedFields(), DETAILED_FIELDS);
        assertEquals(schema.getDetailedMapFields(), DETAILED_MAP_FIELDS);
        assertEquals(schema.getDetailedMapMapFields(), DETAILED_MAP_MAP_FIELDS);
        assertEquals(schema.getDetailedMapListFields(), DETAILED_MAP_LIST_FIELDS);
        Assert.assertEquals(schema.getTypes(), ALL.stream().map(Field::getType).collect(Collectors.toSet()));
    }

    @Test
    public void testCopying() {
        Schema schema = new Schema("src/test/resources/test-schema.json");
        Schema copy = schema.copy();
        assertEquals(schema.getFields(), copy.getFields());
        assertEquals(schema.getFields(), ALL);
        assertEquals(schema.getDetailedFields(), DETAILED_FIELDS);
        assertEquals(schema.getDetailedMapFields(), DETAILED_MAP_FIELDS);
        assertEquals(schema.getDetailedMapMapFields(), DETAILED_MAP_MAP_FIELDS);
        assertEquals(schema.getDetailedMapListFields(), DETAILED_MAP_LIST_FIELDS);
        Assert.assertEquals(schema.getTypes(), ALL.stream().map(Field::getType).collect(Collectors.toSet()));
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testLoadingFromMissingFile() {
        new Schema("does/not/exist.json");
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testLoadingNullFilePath() {
        new Schema((String) null);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testLoadingEmptyFilePath() {
        new Schema("");
    }

    @Test
    public void testParsingFromString() throws Exception {
        String data = new String(Files.readAllBytes(Paths.get("src/test/resources/test-schema.json")));
        // For coverage
        Parser parser = new Parser();
        List<Field> fields = parser.parse(data);
        assertEquals(fields, ALL);
    }

    @Test
    public void testFailParsingFromString() {
        Assert.assertEquals(Parser.parse("['foo']"), singletonList(null));
        Assert.assertEquals(Parser.parse("[{}]"), singletonList(null));
        Assert.assertEquals(Parser.parse("[{'name': null}]"), singletonList(null));
        Assert.assertEquals(Parser.parse("[{'name': null, 'type': null}]"), singletonList(null));
        Assert.assertEquals(Parser.parse("[{'name': 'a'}]"), singletonList(null));
        Assert.assertEquals(Parser.parse("[{'name': 'a', 'type': null}]"), singletonList(null));
        Assert.assertEquals(Parser.parse("[{'name': 'a', 'type': 'garbage'}]"), singletonList(null));
        Assert.assertEquals(Parser.parse("[{'name': null, 'type': 'STRING'}]"), singletonList(null));
        String everything = "[{'name': null, 'type': 'STRING', 'description': 'a', 'subFields': null, " +
                              "'subSubFields': null, 'subListFields': null}]";
        Assert.assertEquals(Parser.parse(everything), singletonList(null));
    }

    @Test
    public void testEmptySchema() {
        Schema schema = new Schema();
        Assert.assertTrue(schema.getFields().isEmpty());
    }

    @Test
    public void testInvalidSchemaFields() {
        assertException(() -> new Schema(singletonList(null)), ".*Found a null field at position 0.*");
        assertException(() -> new Schema(singletonList(new PlainField())), ".*The name or the type must be provided.*");
        assertException(() -> new Schema(singletonList(new PlainField("", null))),
                        ".*The name or the type must be provided.*");
        assertException(() -> new Schema(singletonList(new PlainField("a", null))),
                        ".*The name or the type must be provided.*");
        assertException(() -> new Schema(singletonList(new DetailedField("a", Type.FLOAT, null))),
                        ".*The description must be provided.*");
        assertException(() -> new Schema(singletonList(new DetailedField("a", Type.FLOAT, ""))),
                        ".*The description must be provided.*");
        assertException(() -> new Schema(singletonList(new DetailedMapField("a", Type.DOUBLE_MAP_MAP, "desc", null))),
                        ".*The subFields are not provided.*");
        assertException(() -> new Schema(singletonList(new DetailedMapField("a", Type.FLOAT, "desc", null))),
                        ".*should have a type that is a map.*");
        assertException(() -> new Schema(singletonList(new DetailedMapField("a", Type.INTEGER_MAP, "desc", emptyList()))),
                        ".*The subFields are not provided.*");
        assertException(() -> new Schema(singletonList(new DetailedMapMapField("a", Type.STRING_MAP_MAP, "desc", null, null))),
                        ".*The subSubFields are not provided.*");
        assertException(() -> new Schema(singletonList(new DetailedMapMapField("a", Type.STRING_MAP_MAP, "desc", null, emptyList()))),
                        ".*The subSubFields are not provided.*");
        assertException(() -> new Schema(singletonList(new DetailedMapMapField("a", Type.FLOAT, "desc", null, null))),
                        ".*should have a type that is a map of map.*");
        assertException(() -> new Schema(singletonList(new DetailedMapMapField("a", Type.STRING_MAP, "desc", null, null))),
                        ".*should have a type that is a map of map.*");
        assertException(() -> new Schema(singletonList(new DetailedMapListField("a", Type.STRING_MAP_LIST, "desc", null))),
                        ".*The subListFields are not provided.*");
        assertException(() -> new Schema(singletonList(new DetailedMapListField("a", Type.DOUBLE_MAP_LIST, "desc", emptyList()))),
                        ".*The subListFields are not provided.*");
        assertException(() -> new Schema(singletonList(new DetailedMapListField("a", Type.FLOAT, "desc", emptyList()))),
                        ".*should have a type that is a list of map.*");
    }

    @Test
    public void testSchemaFieldAddingRemovingAndChecking() {
        Schema schema = new Schema("test-schema.json");
        Assert.assertEquals(schema.getType("k"), FIELDS.get("k").getType());
        Assert.assertEquals(schema.getType("dne"), Type.NULL);
        assertEquals(schema.getField("a"), FIELDS.get("a"));
        Assert.assertTrue(schema.hasField("a"));

        // Remove a
        schema.removeField("a");
        Assert.assertFalse(schema.hasField("a"));
        // Fields are validated when adding
        assertException(() -> schema.addField(new PlainField("a", null)), ".*The name or the type must be provided.*");
        schema.addField("a", Type.STRING);
        Assert.assertTrue(schema.hasField("a"));
        // The order of the newly added a is at the end. The second field is now at the beginning
        List<Field> allEdited = new ArrayList<>(ALL.subList(1, ALL.size()));
        allEdited.add(new PlainField("a", Type.STRING));
        assertEquals(schema.getFields(), allEdited);
    }

    @Test
    public void testSchemaChanging() {
        Schema schema = new Schema("test-schema.json");

        schema.changeFieldType("j", Type.DOUBLE_MAP_LIST);
        schema.changeFieldType("k", Type.INTEGER_LIST);
        schema.changeFieldDescription("k", "This is now an integer list");
        assertException(() -> schema.changeSubFields("a", singletonList(new SubField("a", "b"))),
                        ".*a is not a sub-type of.*");
        schema.changeSubFields("l", singletonList(new SubField("c", "desc")));
        schema.changeSubFields("m", singletonList(new SubField("A", "desc")));
        schema.changeSubSubFields("m", singletonList(new SubField("b", "desc")));
        schema.changeSubListFields("o", singletonList(new SubField("a", "desc")));

        // The order shouldn't change in the schema
        List<Field> actual = new ArrayList<>(schema.getFields());
        List<Field> expected = new ArrayList<>(FIELDS.values());
        Assert.assertEquals(actual.size(), expected.size());

        Set<String> excludes = new HashSet<>(Arrays.asList("j", "k", "l", "m", "o"));
        for (int i = 0; i < actual.size(); ++i) {
            Field actualField = actual.get(i);
            Field expectedField = expected.get(i);
            String expectedName = expectedField.getName();
            Assert.assertEquals(actualField.getName(), expectedName);
            // Everything else is the same unless they're in excludes
            if (!excludes.contains(expectedName)) {
                assertEquals(actualField, expectedField);
            }
        }

        Field expectedJ = FIELDS.get("j").copy();
        // For coverage
        expectedJ.setName("j");
        expectedJ.setType(Type.DOUBLE_MAP_LIST);
        assertEquals(schema.getField("j"), expectedJ);

        DetailedField expectedK = (DetailedField) FIELDS.get("k").copy();
        expectedK.setType(Type.INTEGER_LIST);
        expectedK.setDescription("This is now an integer list");
        assertEquals(schema.getField("k"), expectedK);

        DetailedMapField expectedL = (DetailedMapField) FIELDS.get("l").copy();
        expectedL.setSubFields(singletonList(new SubField("c", "desc")));
        assertEquals(schema.getField("l"), expectedL);

        DetailedMapMapField expectedM = (DetailedMapMapField) FIELDS.get("m").copy();
        expectedM.setSubFields(singletonList(new SubField("A", "desc")));
        expectedM.setSubSubFields(singletonList(new SubField("b", "desc")));
        assertEquals(schema.getField("m"), expectedM);

        DetailedMapListField expectedO = (DetailedMapListField) FIELDS.get("o").copy();
        expectedO.setSubListFields(singletonList(new SubField("a", "desc")));
        assertEquals(schema.getField("o"), expectedO);
    }
}
