/*
 *  Copyright 2021, Yahoo Inc.
 *  Licensed under the terms of the Apache License, Version 2.0.
 *  See the LICENSE file associated with the project for terms.
 */
package com.yahoo.bullet.record.avro;

import org.apache.avro.Schema;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.Decoder;
import org.apache.avro.specific.SpecificData;
import org.apache.avro.specific.SpecificDatumReader;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * This exists so that when we read arrays from the serialized AVRO for Bullet, they are read as {@link ArrayList}
 * instances. Otherwise, {@link org.apache.avro.generic.GenericData.Array} is used which is not {@link Serializable},
 * which breaks the guarantee that records return {@link Serializable} instances.
 */
public class CustomAvroReader<T> extends SpecificDatumReader<T> {
    /**
     * This is a custom {@link SpecificData} that simply returns an {@link ArrayList} when a new array is asked. It
     * does not support reusing old containers when creating new arrays so make sure to pass in null for reuse when
     * invoking {@link DatumReader#read(Object, Decoder)}. By using {@link ArrayList}, we also lose the pruning and
     * container reuse benefits of the {@link org.apache.avro.generic.GenericData.Array}.
     *
     * We do not need one for {@link Map} because AVRO uses {@link HashMap} instances.
     */
    private static class SpecificDataArrayList extends SpecificData {
        @Override
        public Object newArray(Object old, int size, Schema schema) {
            return new ArrayList<>();
        }
    }

    private static final SpecificData INSTANCE = new SpecificDataArrayList();

    /**
     * The AVRO generated class for the reader.
     *
     * @param avroClass The {@link Type} for the AVRO class.
     */
    public CustomAvroReader(Type avroClass) {
        super(INSTANCE);
        setSchema(getSpecificData().getSchema(avroClass));
    }

    @Override
    public SpecificData getSpecificData() {
        return INSTANCE;
    }
}
