package com.milko.repository;

import com.milko.model.Student;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.reactive.ReactorCrudRepository;
import jakarta.annotation.Nullable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface StudentRepository extends ReactorCrudRepository<Student, Long> {

    @Query("""
                UPDATE Student s
                SET
                    s.name = COALESCE(:name, s.name),
                    s.email = COALESCE(:email, s.email)
                WHERE s.id = :studentId
            """)
    Mono<Void> updateStudent(Long studentId, @Nullable String name, @Nullable String email);

    @Query("""
                SELECT s FROM Student s
                INNER JOIN s.courses c
                WHERE c.id = :courseId
            """)
    Flux<Student> findAllByCourseId(Long courseId);

}
