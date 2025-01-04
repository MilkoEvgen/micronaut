package com.milko.service;


import com.milko.dto.CourseDto;
import com.milko.dto.StudentDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface StudentService {
    Mono<StudentDto> create(StudentDto dto);
    Flux<StudentDto> findAll();
    Mono<StudentDto> findById(Long id);
    Mono<StudentDto> update(StudentDto dto);
    Mono<Void> deleteById(Long id);
    Mono<StudentDto> addCourseToStudent(Long studentId, Long courseId);
    Flux<CourseDto> findAllCoursesByStudentId(Long id);
}
