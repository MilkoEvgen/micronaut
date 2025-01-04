package com.milko.service;


import com.milko.dto.TeacherDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TeacherService {
    Mono<TeacherDto> create(TeacherDto dto);
    Mono<TeacherDto> update(TeacherDto dto);
    Mono<TeacherDto> findById(Long id);
    Flux<TeacherDto> findAll();
    Mono<Void> deleteById(Long id);
}
