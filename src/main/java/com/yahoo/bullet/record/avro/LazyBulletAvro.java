/*
 *  Copyright 2020, Yahoo Inc.
 *  Licensed under the terms of the Apache License, Version 2.0.
 *  See the LICENSE file associated with the project for terms.
 */
package com.yahoo.bullet.record.avro;

import lombok.extern.slf4j.Slf4j;

@Slf4j
class LazyBulletAvro extends LazyAvro<BulletAvro> {
    private static final long serialVersionUID = -5368363606317600282L;

    /**
     * Default constructor.
     */
    LazyBulletAvro() {
        super(null, BulletAvro.class, BulletAvro::getData);
    }

    /**
     * For coverage.
     */
    LazyBulletAvro(byte[] data) {
        super(data, BulletAvro.class, BulletAvro::getData);
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
