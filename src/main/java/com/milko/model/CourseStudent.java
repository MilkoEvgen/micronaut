package com.milko.model;

import io.micronaut.data.annotation.GeneratedValue;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@MappedEntity(value = "course_student")
public class CourseStudent {
    @Id
    @GeneratedValue(GeneratedValue.Type.IDENTITY)
    private Long id;
    private Long courseId;
    private Long studentId;

    public CourseStudent(Long courseId, Long studentId) {
        this.courseId = courseId;
        this.studentId = studentId;
    }
}