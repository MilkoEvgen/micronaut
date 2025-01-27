package com.milko.integration.utils;

import io.r2dbc.spi.ConnectionFactory;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Singleton
@RequiredArgsConstructor
public class DatabaseCleaner {
    private final ConnectionFactory connectionFactory;

    public Mono<Void> clearTables() {
        return Mono.from(connectionFactory.create())
                .flatMap(connection ->
                        Flux.from(connection.createBatch()
                                        .add("TRUNCATE TABLE course_student RESTART IDENTITY CASCADE")
                                        .add("TRUNCATE TABLE teachers RESTART IDENTITY CASCADE")
                                        .add("TRUNCATE TABLE courses RESTART IDENTITY CASCADE")
                                        .add("TRUNCATE TABLE students RESTART IDENTITY CASCADE")
                                        .add("TRUNCATE TABLE departments RESTART IDENTITY CASCADE")
                                        .execute())
                                .then()
                                .publishOn(Schedulers.boundedElastic())
                                .doFinally(signal -> Mono.from(connection.close()).subscribe())
                );
    }

}
