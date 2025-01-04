package com.milko.service;

import com.milko.dto.CourseDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CourseService{
    Mono<CourseDto> create(CourseDto dto);
    Mono<CourseDto> update(CourseDto dto);
    Mono<CourseDto> findById(Long id);
    Flux<CourseDto> findAll();
    Mono<Void> deleteById(Long id);
    Mono<CourseDto> setTeacherToCourse(Long courseId, Long teacherId);
}
