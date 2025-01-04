package com.milko.service.impl;

import com.milko.dto.CourseDto;
import com.milko.dto.DepartmentDto;
import com.milko.dto.TeacherDto;
import com.milko.exception.EntityNotFoundException;
import com.milko.mapper.CourseMapper;
import com.milko.mapper.DepartmentMapper;
import com.milko.mapper.TeacherMapper;
import com.milko.model.Course;
import com.milko.model.Department;
import com.milko.model.Teacher;
import com.milko.repository.CourseRepository;
import com.milko.repository.DepartmentRepository;
import com.milko.repository.TeacherRepository;
import com.milko.service.TeacherService;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple3;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Singleton
@RequiredArgsConstructor
public class TeacherServiceImpl implements TeacherService {
    private final TeacherRepository teacherRepository;
    private final DepartmentRepository departmentRepository;
    private final CourseRepository courseRepository;
    private final TeacherMapper teacherMapper;
    private final DepartmentMapper departmentMapper;
    private final CourseMapper courseMapper;

    @Override
    public Mono<TeacherDto> create(TeacherDto dto) {
        log.info("in create, dto = {}", dto);
        Teacher teacher = teacherMapper.toTeacher(dto);
        return teacherRepository.save(teacher)
                .map(teacherMapper::toTeacherDto);
    }

    @Override
    public Mono<TeacherDto> update(TeacherDto dto) {
        log.info("in update, dto = {}", dto);
        return teacherRepository.findById(dto.getId())
                .switchIfEmpty(Mono.error(new EntityNotFoundException("Teacher with ID " + dto.getId() + " not found")))
                .flatMap(teacher -> {
                    teacherMapper.updateFromDto(dto, teacher);
                    return teacherRepository.updateTeacher(dto.getId(), dto.getName())
                            .thenReturn(teacher);
                })
                .flatMap(this::fetchRelatedEntitiesForTeacher)
                .map(this::buildTeacherDto);
    }

    @Override
    public Mono<TeacherDto> findById(Long id) {
        log.info("in findById, id = {}", id);
        return teacherRepository.findById(id)
                .switchIfEmpty(Mono.error(new EntityNotFoundException("Teacher with ID " + id + " not found")))
                .flatMap(this::fetchRelatedEntitiesForTeacher)
                .map(this::buildTeacherDto);
    }

    @Override
    public Flux<TeacherDto> findAll() {
        log.info("in findAll");

        return teacherRepository.findAll()
                .flatMap(this::fetchRelatedEntitiesForTeacher)
                .map(this::buildTeacherDto);
    }

    @Override
    public Mono<Void> deleteById(Long id) {
        log.info("in deleteById, id = {}", id);
        return teacherRepository.deleteById(id)
                .then();
    }

    private Mono<Tuple3<Teacher, Department, List<Course>>> fetchRelatedEntitiesForTeacher(Teacher teacher) {
        Mono<Department> departmentMono = departmentRepository.findByHeadOfDepartmentId(teacher.getId())
                .defaultIfEmpty(new Department());

        Mono<List<Course>> coursesMono = courseRepository.findAllByTeacherId(teacher.getId())
                .collectList();

        return Mono.zip(
                Mono.just(teacher),
                departmentMono,
                coursesMono
        );
    }


    private TeacherDto buildTeacherDto(Tuple3<Teacher, Department, List<Course>> tuple) {
        Teacher teacher = tuple.getT1();
        Department department = tuple.getT2();
        List<Course> courses = tuple.getT3();

        TeacherDto teacherDto = teacherMapper.toTeacherDto(teacher);
        DepartmentDto departmentDto = departmentMapper.toDepartmentDto(department);
        Set<CourseDto> courseDtos = courses.stream()
                .map(courseMapper::toCourseDto)
                .collect(Collectors.toSet());

        teacherDto.setDepartment(departmentDto);
        teacherDto.setCourses(courseDtos);
        return teacherDto;
    }
}
