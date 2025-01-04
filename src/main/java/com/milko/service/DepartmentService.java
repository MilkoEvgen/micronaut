package com.milko.service;


import com.milko.dto.DepartmentDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DepartmentService{
    Mono<DepartmentDto> create(DepartmentDto dto);
    Mono<DepartmentDto> update(DepartmentDto dto);
    Mono<DepartmentDto> findById(Long id);
    Flux<DepartmentDto> findAll();
    Mono<Void> deleteById(Long id);
    Mono<DepartmentDto> setTeacherToDepartment(Long departmentId, Long teacherId);
}
