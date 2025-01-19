package com.milko.repository;


import com.milko.dto.records.StudentCoursesView;
import com.milko.model.Course;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.r2dbc.annotation.R2dbcRepository;
import io.micronaut.data.repository.reactive.ReactorCrudRepository;
import lombok.NonNull;
import reactor.core.publisher.Flux;
import reactor.util.function.Tuple2;

import java.util.List;

@R2dbcRepository(dialect = Dialect.POSTGRES)
public interface CourseRepository extends ReactorCrudRepository<Course, Long> {
    @Query("""
            SELECT * FROM courses c
            INNER JOIN course_student cs ON cs.course_id = c.id
            WHERE cs.student_id = :studentId
            """)
    Flux<Course> findAllByStudentId(@NonNull Long studentId);

    @Query("""
            SELECT * FROM courses c
            WHERE c.teacher_id = :teacherId
            """)
    Flux<Course> findAllByTeacherId(@NonNull Long teacherId);

    @Query("""
            SELECT s.id AS student_id,
                   c.id as course_id,
                   c.title as course_title,
                   c.teacher_id as course_teacher_id
            FROM courses c
            INNER JOIN course_student cs ON c.id = cs.course_id
            INNER JOIN students s ON cs.student_id = s.id
            WHERE s.id IN (:studentsIdList)
            """)
    Flux<StudentCoursesView> findAllByStudentsIdList(@NonNull List<Long> studentsIdList);
}
