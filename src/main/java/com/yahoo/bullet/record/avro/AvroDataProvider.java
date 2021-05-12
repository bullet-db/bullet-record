/*
 *  Copyright 2021, Yahoo Inc.
 *  Licensed under the terms of the Apache License, Version 2.0.
 *  See the LICENSE file associated with the project for terms.
 */
package com.yahoo.bullet.record.avro;

import java.util.Map;

/**
 * This is the interface that can be passed to {@link LazyAvro} to get data out from its lazily deserialized AVRO data.
 *
 * @param <T> The type of the AVRO generated class.
 */
@FunctionalInterface
public interface AvroDataProvider<T> {
    /**
     * Given an AVRO deserialized data object, return the data in it as a {@link Map}.
     *
     * @param data The AVRO deserialized data object
     * @return The {@link Map} of field names to their values, matching the types supported in Bullet.
     */
    Map<String, Object> getData(T data);
}
