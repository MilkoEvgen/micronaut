package com.milko.repository;

import com.milko.model.Teacher;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.r2dbc.annotation.R2dbcRepository;
import io.micronaut.data.repository.reactive.ReactorCrudRepository;
import lombok.NonNull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@R2dbcRepository(dialect = Dialect.POSTGRES)
public interface TeacherRepository extends ReactorCrudRepository<Teacher, Long> {
    @Query("""
            SELECT * FROM teachers t
            JOIN courses c ON t.id = c.teacher_id
            WHERE c.id = :courseId
            """)
    Mono<Teacher> findByCourseId(@NonNull Long courseId);

    @Query("""
            SELECT DISTINCT t.id, t.name
            FROM teachers t
            JOIN courses c ON t.id = c.teacher_id
            WHERE c.id IN (:coursesIdList)
            """)
    Flux<Teacher> findAllByCoursesIdList(@NonNull  List<Long> coursesIdList);

    @Query("""
            SELECT * FROM teachers t
            JOIN departments d ON t.id = d.head_of_department_id
            WHERE d.id = :departmentId
            """)
    Mono<Teacher> findByDepartmentId(@NonNull Long departmentId);

    @Query("""
            SELECT DISTINCT t.id, t.name
            FROM teachers t
            JOIN departments d ON t.id = d.head_of_department_id
            WHERE d.id IN (:departmentsIdList)
            """)
    Flux<Teacher> findAllByDepartmentsIdList(@NonNull List<Long> departmentsIdList);
}
