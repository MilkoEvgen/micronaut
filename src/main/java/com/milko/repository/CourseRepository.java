package com.milko.repository;


import com.milko.model.Course;
import com.milko.model.Teacher;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.reactive.ReactorCrudRepository;
import jakarta.annotation.Nullable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface CourseRepository extends ReactorCrudRepository<Course, Long> {

    @Query("""
                UPDATE Course c
                SET
                    c.title = COALESCE(:title, c.title)
                WHERE c.id = :courseId
            """)
    Mono<Void> updateCourse(Long courseId, @Nullable String title);

    @Query("""
                UPDATE Course c
                SET
                    c.teacher = :teacher
                WHERE c.id = :courseId
            """)
    Mono<Void> setTeacherToCourse(Long courseId, Teacher teacher);

    @Query("""
            SELECT c FROM Course c
            JOIN c.students s
            WHERE s.id = :studentId
            """)
    Flux<Course> findAllByStudentId(Long studentId);

    @Query("""
            SELECT c FROM Course c
            JOIN c.teacher t
            WHERE t.id = :teacherId
            """)
    Flux<Course> findAllByTeacherId(Long teacherId);
}
