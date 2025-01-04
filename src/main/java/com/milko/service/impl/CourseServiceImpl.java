package com.milko.service.impl;

import com.milko.dto.CourseDto;
import com.milko.dto.StudentDto;
import com.milko.dto.TeacherDto;
import com.milko.exception.EntityNotFoundException;
import com.milko.mapper.CourseMapper;
import com.milko.mapper.StudentMapper;
import com.milko.mapper.TeacherMapper;
import com.milko.model.Course;
import com.milko.model.Student;
import com.milko.model.Teacher;
import com.milko.repository.CourseRepository;
import com.milko.repository.StudentRepository;
import com.milko.repository.TeacherRepository;
import com.milko.service.CourseService;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple3;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Singleton
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {
    private final CourseRepository courseRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final CourseMapper courseMapper;
    private final StudentMapper studentMapper;
    private final TeacherMapper teacherMapper;

    @Override
    public Mono<CourseDto> create(CourseDto dto) {
        log.info("in create, dto = {}", dto);
        Course course = courseMapper.toCourse(dto);
        return courseRepository.save(course)
                .map(courseMapper::toCourseDto);
    }

    @Override
    public Mono<CourseDto> update(CourseDto dto) {
        log.info("in update, dto = {}", dto);
        return courseRepository.findById(dto.getId())
                .switchIfEmpty(Mono.error(new EntityNotFoundException("Course with ID " + dto.getId() + " not found")))
                .flatMap(course -> {
                    courseMapper.updateFromDto(dto, course);
                    return courseRepository.updateCourse(dto.getId(), dto.getTitle())
                            .thenReturn(course);
                })
                .flatMap(this::fetchRelatedEntitiesForCourse)
                .map(this::buildCourseDto);
    }

    @Override
    public Mono<CourseDto> findById(Long id) {
        log.info("in findById, id = {}", id);
        return courseRepository.findById(id)
                .switchIfEmpty(Mono.error(new EntityNotFoundException("Course with ID " + id + " not found")))
                .flatMap(this::fetchRelatedEntitiesForCourse)
                .map(this::buildCourseDto);
    }

    @Override
    public Flux<CourseDto> findAll() {
        log.info("in findAll");
        return courseRepository.findAll()
                .flatMap(this::fetchRelatedEntitiesForCourse)
                .map(this::buildCourseDto);
    }

    @Override
    public Mono<Void> deleteById(Long id) {
        log.info("in deleteById, id = {}", id);
        return courseRepository.deleteById(id).then();
    }

    @Override
    public Mono<CourseDto> setTeacherToCourse(Long courseId, Long teacherId) {
        log.info("in setTeacherToCourse, courseId = {}, teacherId = {}", courseId, teacherId);
        Mono<Course> courseMono = courseRepository.findById(courseId)
                .switchIfEmpty(Mono.error(new EntityNotFoundException("Course with ID " + courseId + " not found")));
        Mono<Teacher> teacherMono = teacherRepository.findById(teacherId)
                .switchIfEmpty(Mono.error(new EntityNotFoundException("Teacher with ID " + teacherId + " not found")));
        return Mono.zip(courseMono, teacherMono)
                .flatMap(tuple -> {
                    Course course = tuple.getT1();
                    Teacher teacher = tuple.getT2();
                    return courseRepository.setTeacherToCourse(courseId, teacher)
                            .thenReturn(course);
                })
                .flatMap(this::fetchRelatedEntitiesForCourse)
                .map(this::buildCourseDto);
    }

    private Mono<Tuple3<Course, Teacher, List<Student>>> fetchRelatedEntitiesForCourse(Course course) {
        Mono<Teacher> teacherMono = teacherRepository.findByCourseId(course.getId());

        Mono<List<Student>> studentsMono = studentRepository.findAllByCourseId(course.getId()).collectList();

        return Mono.zip(
                Mono.just(course),
                teacherMono.defaultIfEmpty(new Teacher()),
                studentsMono
        );
    }


    private CourseDto buildCourseDto(Tuple3<Course, Teacher, List<Student>> tuple) {
        Course course = tuple.getT1();
        Teacher teacher = tuple.getT2();
        List<Student> students = tuple.getT3();

        CourseDto courseDto = courseMapper.toCourseDto(course);
        TeacherDto teacherDto = teacherMapper.toTeacherDto(teacher);
        Set<StudentDto> studentsDto = students.stream()
                .map(studentMapper::toStudentDto)
                .collect(Collectors.toSet());

        courseDto.setTeacher(teacherDto);
        courseDto.setStudents(studentsDto);
        return courseDto;
    }
}
