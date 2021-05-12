/*
 *  Copyright 2020, Yahoo Inc.
 *  Licensed under the terms of the Apache License, Version 2.0.
 *  See the LICENSE file associated with the project for terms.
 */
package com.yahoo.bullet.record.avro;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
class LazyBulletAvro extends LazyAvro<BulletAvro> {
    private static final long serialVersionUID = -5368363606317600282L;

    private static class BulletAvroDataProvider implements AvroDataProvider<BulletAvro> {
        private static final long serialVersionUID = -4126428757611776867L;

        @Override
        public Map<String, Object> getData(BulletAvro data) {
            return data.getData();
        }

        @Override
        public BulletAvro getRecord(Map<String, Object> data) {
            return new BulletAvro(data);
        }
    }

    private static final BulletAvroDataProvider INSTANCE = new BulletAvroDataProvider();

    /**
     * Default constructor.
     */
    LazyBulletAvro() {
        super(null, BulletAvro.class, INSTANCE);
    }

    /**
     * For coverage.
     */
    LazyBulletAvro(byte[] data) {
        super(data, BulletAvro.class, INSTANCE);
    }

    /**
     * Copy constructor.
     *
     * @param other The {@link LazyBulletAvro} to copy.
     * @throws RuntimeException if failed to copy data from the source.
     */
    LazyBulletAvro(LazyBulletAvro other) {
        super(other);
    }
}
