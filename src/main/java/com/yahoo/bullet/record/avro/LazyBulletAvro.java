/*
 *  Copyright 2020, Yahoo Inc.
 *  Licensed under the terms of the Apache License, Version 2.0.
 *  See the LICENSE file associated with the project for terms.
 */
package com.yahoo.bullet.record.avro;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;

import java.util.Map;

@Slf4j @NoArgsConstructor
public class LazyBulletAvro extends LazyAvro<BulletAvro> {
    private static final long serialVersionUID = -5368363606317600282L;

    private static final SpecificDatumReader<BulletAvro> READER = new CustomAvroReader<>(BulletAvro.class);
    private static final SpecificDatumWriter<BulletAvro> WRITER = new SpecificDatumWriter<>(BulletAvro.class);

    /**
     * Copy constructor.
     *
     * @param other The {@link LazyBulletAvro} to copy.
     * @throws RuntimeException if failed to copy data from the source.
     */
    public LazyBulletAvro(LazyBulletAvro other) {
        super(other);
    }

    @Override
    public SpecificDatumReader<BulletAvro> getReader() {
        return READER;
    }

    @Override
    public SpecificDatumWriter<BulletAvro> getWriter() {
        return WRITER;
    }

    @Override
    public BulletAvro getRecord(Map<String, Object> data) {
        return new BulletAvro(data);
    }

    @Override
    public Map<String, Object> getData(BulletAvro record) {
        return record.getData();
    }
}
