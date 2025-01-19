package com.milko.repository;

import com.milko.dto.records.CourseStudentsView;
import com.milko.model.Student;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.r2dbc.annotation.R2dbcRepository;
import io.micronaut.data.repository.reactive.ReactorCrudRepository;
import lombok.NonNull;
import reactor.core.publisher.Flux;

import java.util.List;

@R2dbcRepository(dialect = Dialect.POSTGRES)
public interface StudentRepository extends ReactorCrudRepository<Student, Long> {
    @Query("""
                SELECT * FROM students s
                INNER JOIN course_student cs ON s.id = cs.student_id
                INNER JOIN courses c ON cs.course_id = c.id
                WHERE c.id = :courseId
            """)
    Flux<Student> findAllByCourseId(@NonNull Long courseId);

    @Query("""
            SELECT c.id AS course_id,
                   s.id as student_id,
                   s.name as student_name,
                   s.email as student_email
            FROM students s
            INNER JOIN course_student cs ON s.id = cs.student_id
            INNER JOIN courses c ON cs.course_id = c.id
            WHERE c.id IN (:coursesIdList)
            """)
    Flux<CourseStudentsView> findAllByCoursesIdList(@NonNull List<Long> coursesIdList);

}
