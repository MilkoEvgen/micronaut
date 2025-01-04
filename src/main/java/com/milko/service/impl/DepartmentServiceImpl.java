package com.milko.service.impl;

import com.milko.dto.DepartmentDto;
import com.milko.dto.TeacherDto;
import com.milko.exception.EntityNotFoundException;
import com.milko.mapper.DepartmentMapper;
import com.milko.mapper.TeacherMapper;
import com.milko.model.Department;
import com.milko.model.Teacher;
import com.milko.repository.DepartmentRepository;
import com.milko.repository.TeacherRepository;
import com.milko.repository.impl.CustomRepositoryImpl;
import com.milko.service.DepartmentService;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.util.Optional;

@Slf4j
@Singleton
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {
    private final DepartmentRepository departmentRepository;
    private final TeacherRepository teacherRepository;
    private final DepartmentMapper departmentMapper;
    private final TeacherMapper teacherMapper;

    @Override
    public Mono<DepartmentDto> create(DepartmentDto dto) {
        log.info("in create, dto = {}", dto);
        Department department = departmentMapper.toDepartment(dto);
        return departmentRepository.save(department)
                .map(departmentMapper::toDepartmentDto);
    }

    @Override
    public Mono<DepartmentDto> update(DepartmentDto dto) {
        log.info("in update, dto = {}", dto);
        return departmentRepository.findById(dto.getId())
                .switchIfEmpty(Mono.error(new EntityNotFoundException("Department with ID " + dto.getId() + " not found")))
                .flatMap(department -> {
                    departmentMapper.updateFromDto(dto, department);
                    return departmentRepository.updateDepartment(dto.getId(), dto.getName())
                            .thenReturn(department);
                })
                .flatMap(this::fetchRelatedEntitiesForDepartment)
                .map(this::buildDepartmentDto);
    }

    @Override
    public Mono<DepartmentDto> findById(Long id) {
        log.info("in findById, id = {}", id);
        return departmentRepository.findById(id)
                .switchIfEmpty(Mono.error(new EntityNotFoundException("Department with ID " + id + " not found")))
                .flatMap(this::fetchRelatedEntitiesForDepartment)
                .map(this::buildDepartmentDto);
    }

    @Override
    public Flux<DepartmentDto> findAll() {
        log.info("in findAll");
        return departmentRepository.findAll()
                .flatMap(this::fetchRelatedEntitiesForDepartment)
                .map(this::buildDepartmentDto);
    }

    @Override
    public Mono<Void> deleteById(Long id) {
        log.info("in deleteById, id = {}", id);
        return departmentRepository.deleteById(id).then();
    }

    @Override
    public Mono<DepartmentDto> setTeacherToDepartment(Long departmentId, Long teacherId) {
        log.info("in setTeacherToDepartment, departmentId = {}, teacherId = {}", departmentId, teacherId);
        Mono<Department> departmentMono = departmentRepository.findById(departmentId)
                .switchIfEmpty(Mono.error(new EntityNotFoundException("Department with ID " + departmentId + " not found")));
        Mono<Teacher> teacherMono = teacherRepository.findById(teacherId)
                .switchIfEmpty(Mono.error(new EntityNotFoundException("Teacher with ID " + teacherId + " not found")));
        return Mono.zip(departmentMono, teacherMono)
                .flatMap(tuple -> {
                    Department department = tuple.getT1();
                    Teacher teacher = tuple.getT2();

                    return departmentRepository.setTeacherToDepartment(departmentId, teacher)
                            .thenReturn(department)
                            .flatMap(this::fetchRelatedEntitiesForDepartment)
                            .map(this::buildDepartmentDto);
                });
    }

    private Mono<Tuple2<Department, Teacher>> fetchRelatedEntitiesForDepartment(Department department) {
        Mono<Teacher> teacherMono = teacherRepository.findByDepartmentId(department.getId());

        return Mono.zip(
                Mono.just(department),
                teacherMono
        );
    }

    private DepartmentDto buildDepartmentDto(Tuple2<Department,  Teacher> tuple) {
        Department department = tuple.getT1();
        Teacher teacher = tuple.getT2();

        DepartmentDto departmentDto = departmentMapper.toDepartmentDto(department);
        TeacherDto teacherDto = teacherMapper.toTeacherDto(teacher);

        departmentDto.setHeadOfDepartment(teacherDto);
        return departmentDto;
    }
}
