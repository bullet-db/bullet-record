package com.yahoo.bullet.record;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *  Converts Map to BulletRecord based on schema file.
 *  If 'enumerations' is used, the outer map is replaced by a new map in the record.
 *  Not recommended for large maps since the schema will need an entry for each field.
 *  BUG: Doesn't throw classcastexceptions....
 */
public class MapBulletRecordConverter {
    public enum Type {
        @SerializedName("BOOLEAN")
        BOOLEAN (true),

        @SerializedName("INTEGER")
        INTEGER (true),

        @SerializedName("LONG")
        LONG (true),

        @SerializedName("FLOAT")
        FLOAT (true),

        @SerializedName("DOUBLE")
        DOUBLE (true),

        @SerializedName("STRING")
        STRING (true),

        @SerializedName("MAP")
        MAP (false),

        @SerializedName("MAPOFMAP")
        MAPOFMAP (false),

        @SerializedName("LIST")
        LIST (false),

        @SerializedName("LISTOFMAP")
        LISTOFMAP (false);

        private final boolean primitive;

        Type(boolean primitive) {
            this.primitive = primitive;
        }

        private boolean isPrimitive() {
            return primitive;
        }
    }

    private static class MapBulletRecordField {
        String name;
        Type type;
        Type subtype;
        List<String> enumerations;

        /**
         * Helper function. pulls out values from map using the listed keys in enumerations
         *
         * @param map
         * @param <T>
         * @return
         */
        private <T> Map<String, T> getEnumerations(Map<String, T> map) {
            if (enumerations == null) {
                return map;
            }
            Map<String, T> newMap = new HashMap<>();
            for (String s : enumerations) {
                newMap.put(s, map.get(s));
            }
            return newMap;
        }
    }
    private List<MapBulletRecordField> fields;

    private MapBulletRecordConverter(String schema) {
        JsonReader reader;
        try {
            reader = new JsonReader(new FileReader(schema));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(schema + " file not found.", e);
        }
        Gson gson = new Gson();
        fields = gson.fromJson(reader, new TypeToken<List<MapBulletRecordField>>() { }.getType());

        // validate fields; does not check for duplicate names
        // why
        for (MapBulletRecordField field : fields) {
            if (field.name == null || field.name.isEmpty()) {
                throw new RuntimeException("'name' is null.");
            }
            if (field.type == null) {
                throw new RuntimeException("'type' is either null or unsupported.");
            }
            if (!field.type.isPrimitive()) {
                if (field.subtype == null) {
                    throw new RuntimeException("'subtype' is either null or unsupported given 'type' is a list/map.");
                }
                if (!field.subtype.isPrimitive()) {
                    throw new RuntimeException(field.subtype + " is not an appropriate 'subtype'.");
                }
                if ((field.type == Type.MAP || field.type == Type.MAPOFMAP) && field.enumerations != null) {
                    if (field.enumerations.isEmpty()) {
                        throw new RuntimeException("'enumerations' array in schema is empty.");
                    }
                    for (String name : field.enumerations) {
                        if (name == null || name.isEmpty()) {
                            throw new RuntimeException("'enumerations' array in schema contains an empty string.");
                        }
                    }
                }
            }
        }
    }

    /**
     * Not necessary but mirrors {@link POJOBulletRecordConverter}.
     *
     * @param schema
     * @return
     */
    public static MapBulletRecordConverter from(String schema) {
        return new MapBulletRecordConverter(schema);
    }

    /**
     * Converts map to BulletRecord with previously-given schema.
     *
     * @param map
     * @return
     */
    @SuppressWarnings("unchecked")
    public BulletRecord convert(Map<String, Object> map) {
        BulletRecord record = new SimpleBulletRecord();

        // Should throw ClassCastException if the map/schema is wrong
        // Why why why
        for (MapBulletRecordField field : fields) {
            switch (field.type) {
                case BOOLEAN:
                    record.setBoolean(field.name, (Boolean) map.get(field.name));
                    break;
                case INTEGER:
                    record.setInteger(field.name, (Integer) map.get(field.name));
                    break;
                case LONG:
                    record.setLong(field.name, (Long) map.get(field.name));
                    break;
                case FLOAT:
                    record.setFloat(field.name, (Float) map.get(field.name));
                    break;
                case DOUBLE:
                    record.setDouble(field.name, (Double) map.get(field.name));
                    break;
                case STRING:
                    record.setString(field.name, (String) map.get(field.name));
                    break;
                case MAP:
                    setMap(record, field, map);
                    break;
                case MAPOFMAP:
                    setMapOfMap(record, field, map);
                    break;
                case LIST:
                    setList(record, field, map);
                    break;
                case LISTOFMAP:
                    setListOfMap(record, field, map);
                    break;
            }
        }
        return record;
    }

    /**
     * Helper function. handles subtype cases for Map type
     *
     * @param record
     * @param field
     * @param map
     */
    @SuppressWarnings("unchecked")
    private void setMap(BulletRecord record, MapBulletRecordField field, Map<String, Object> map) {
        switch (field.subtype) {
            case BOOLEAN:
                record.setBooleanMap(field.name, field.getEnumerations((Map<String, Boolean>) map.get(field.name)));
                break;
            case INTEGER:
                record.setIntegerMap(field.name, field.getEnumerations((Map<String, Integer>) map.get(field.name)));
                break;
            case LONG:
                record.setLongMap(field.name, field.getEnumerations((Map<String, Long>) map.get(field.name)));
                break;
            case FLOAT:
                record.setFloatMap(field.name, field.getEnumerations((Map<String, Float>) map.get(field.name)));
                break;
            case DOUBLE:
                record.setDoubleMap(field.name, field.getEnumerations((Map<String, Double>) map.get(field.name)));
                break;
            case STRING:
                record.setStringMap(field.name, field.getEnumerations((Map<String, String>) map.get(field.name)));
                break;
        }
    }

    /**
     * Helper function. handles subtype cases for MapOfMap type
     *
     * @param record
     * @param field
     * @param map
     */
    @SuppressWarnings("unchecked")
    private void setMapOfMap(BulletRecord record, MapBulletRecordField field, Map<String, Object> map) {
        switch (field.subtype) {
            case BOOLEAN:
                record.setMapOfBooleanMap(field.name,
                        field.getEnumerations((Map<String, Map<String, Boolean>>) map.get(field.name)));
                break;
            case INTEGER:
                record.setMapOfIntegerMap(field.name,
                        field.getEnumerations((Map<String, Map<String, Integer>>) map.get(field.name)));
                break;
            case LONG:
                record.setMapOfLongMap(field.name,
                        field.getEnumerations((Map<String, Map<String, Long>>) map.get(field.name)));
                break;
            case FLOAT:
                record.setMapOfFloatMap(field.name,
                        field.getEnumerations((Map<String, Map<String, Float>>) map.get(field.name)));
                break;
            case DOUBLE:
                record.setMapOfDoubleMap(field.name,
                        field.getEnumerations((Map<String, Map<String, Double>>) map.get(field.name)));
                break;
            case STRING:
                record.setMapOfStringMap(field.name,
                        field.getEnumerations((Map<String, Map<String, String>>) map.get(field.name)));
                break;
        }
    }

    /**
     * Helper function. handles subtype cases for List type
     *
     * @param record
     * @param field
     * @param map
     */
    @SuppressWarnings("unchecked")
    private void setList(BulletRecord record, MapBulletRecordField field, Map<String, Object> map) {
        switch (field.subtype) {
            case BOOLEAN:
                record.setBooleanList(field.name, (List<Boolean>) map.get(field.name));
                break;
            case INTEGER:
                record.setIntegerList(field.name, (List<Integer>) map.get(field.name));
                break;
            case LONG:
                record.setLongList(field.name, (List<Long>) map.get(field.name));
                break;
            case FLOAT:
                record.setFloatList(field.name, (List<Float>) map.get(field.name));
                break;
            case DOUBLE:
                record.setDoubleList(field.name, (List<Double>) map.get(field.name));
                break;
            case STRING:
                record.setStringList(field.name, (List<String>) map.get(field.name));
                break;
        }
    }

    /**
     * Helper function. handles subtype cases for ListOfMap type
     *
     * @param record
     * @param field
     * @param map
     */
    @SuppressWarnings("unchecked")
    private void setListOfMap(BulletRecord record, MapBulletRecordField field, Map<String, Object> map) {
        switch (field.subtype) {
            case BOOLEAN:
                record.setListOfBooleanMap(field.name, (List<Map<String, Boolean>>) map.get(field.name));
                break;
            case INTEGER:
                record.setListOfIntegerMap(field.name, (List<Map<String, Integer>>) map.get(field.name));
                break;
            case LONG:
                record.setListOfLongMap(field.name, (List<Map<String, Long>>) map.get(field.name));
                break;
            case FLOAT:
                record.setListOfFloatMap(field.name, (List<Map<String, Float>>) map.get(field.name));
                break;
            case DOUBLE:
                record.setListOfDoubleMap(field.name, (List<Map<String, Double>>) map.get(field.name));
                break;
            case STRING:
                record.setListOfStringMap(field.name, (List<Map<String, String>>) map.get(field.name));
                break;
        }
    }
}
