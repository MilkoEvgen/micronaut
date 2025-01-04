package com.milko.repository.impl;

import io.r2dbc.spi.ConnectionFactory;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import io.r2dbc.spi.Result;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
@RequiredArgsConstructor
public class CustomRepositoryImpl {
    private final ConnectionFactory connectionFactory;

    public Mono<Void> addCourseToStudent(Long courseId, Long studentId) {
        return Mono.usingWhen(
                connectionFactory.create(),
                connection -> Flux.from(
                                connection.createStatement("INSERT INTO course_student (course_id, student_id) VALUES ($1, $2)")
                                        .bind(0, courseId)
                                        .bind(1, studentId)
                                        .execute()
                        )
                        .flatMap(Result::getRowsUpdated)
                        .then(),
                connection -> Mono.from(connection.close())
        );
    }
}




