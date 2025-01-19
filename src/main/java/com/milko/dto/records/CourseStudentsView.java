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
public class CourseStudentsView{
    private Long courseId;
    private Long studentId;
    private String studentName;
    private String studentEmail;
}
