package com.milko.repository;

import com.milko.model.Teacher;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.reactive.ReactorCrudRepository;
import jakarta.annotation.Nullable;
import reactor.core.publisher.Mono;

@Repository
public interface TeacherRepository extends ReactorCrudRepository<Teacher, Long> {
    @Query("""
                UPDATE Teacher t
                SET t.name = COALESCE(:name, t.name)
                WHERE t.id = :id
            """)
    Mono<Void> updateTeacher(Long id, @Nullable String name);

    @Query("""
            SELECT t FROM Teacher t
            JOIN t.courses c
            WHERE c.id = :courseId
            """)
    Mono<Teacher> findByCourseId(Long courseId);

    @Query("""
            SELECT t FROM Teacher t
            JOIN t.department d
            WHERE d.id = :departmentId
            """)
    Mono<Teacher> findByDepartmentId(Long departmentId);
}
