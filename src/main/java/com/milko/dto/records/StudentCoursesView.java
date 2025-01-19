package com.milko.dto.records;

import io.micronaut.core.annotation.Introspected;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Introspected
public class StudentCoursesView {
    private Long studentId;
    private Long courseId;
    private String courseTitle;
    private Long courseTeacherId;
}