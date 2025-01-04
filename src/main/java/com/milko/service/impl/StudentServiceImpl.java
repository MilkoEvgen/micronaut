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
import com.milko.repository.impl.CustomRepositoryImpl;
import com.milko.service.StudentService;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Singleton
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {
    private final CustomRepositoryImpl customRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final TeacherRepository teacherRepository;
    private final StudentMapper studentMapper;
    private final CourseMapper courseMapper;
    private final TeacherMapper teacherMapper;

    @Override
    public Mono<StudentDto> create(StudentDto dto) {
        log.info("in create, dto = {}", dto);
        Student student = studentMapper.toStudent(dto);
        return studentRepository.save(student)
                .map(studentMapper::toStudentDto);
    }

    @Override
    public Flux<StudentDto> findAll() {
        log.info("in findAll");
        return studentRepository.findAll()
                .flatMap(this::fetchRelatedEntitiesForStudent)
                .map(this::buildStudentDto);
    }

    @Override
    public Mono<StudentDto> findById(Long id) {
        log.info("in findById, id = {}", id);
        return studentRepository.findById(id)
                .switchIfEmpty(Mono.error(new EntityNotFoundException("Student with ID " + id + " not found")))
                .flatMap(this::fetchRelatedEntitiesForStudent)
                .map(this::buildStudentDto);
    }

    @Override
    public Flux<CourseDto> findAllCoursesByStudentId(Long id) {
        log.info("in findAllCoursesByStudentId, id = {}", id);
        return courseRepository.findAllByStudentId(id)
                .flatMap(course -> teacherRepository.findByCourseId(course.getId())
                        .map(teacher -> buildCourseDtoWithTeacher(course, teacher))
                        .switchIfEmpty(Mono.just(courseMapper.toCourseDto(course))));
    }

    private CourseDto buildCourseDtoWithTeacher(Course course, Teacher teacher) {
        TeacherDto teacherDto = teacherMapper.toTeacherDto(teacher);
        CourseDto courseDto = courseMapper.toCourseDto(course);
        courseDto.setTeacher(teacherDto);
        return courseDto;
    }

    @Override
    public Mono<StudentDto> update(StudentDto dto) {
        log.info("in update, dto = {}", dto);
        return studentRepository.findById(dto.getId())
                .switchIfEmpty(Mono.error(new EntityNotFoundException("Student with ID " + dto.getId() + " not found")))
                .flatMap(student -> {
                    studentMapper.updateFromDto(dto, student);
                    return studentRepository.updateStudent(dto.getId(), dto.getName(), dto.getEmail())
                            .thenReturn(student);
                }).flatMap(this::fetchRelatedEntitiesForStudent)
                .map(this::buildStudentDto);
    }

    @Override
    public Mono<Void> deleteById(Long id) {
        log.info("in deleteById, id = {}", id);
        return studentRepository.deleteById(id)
                .then();
    }

    @Override
    public Mono<StudentDto> addCourseToStudent(Long studentId, Long courseId) {
        log.info("in addCourseToStudent, studentId = {}, courseId = {}", studentId, courseId);
        Mono<Student> studentMono = studentRepository.findById(studentId)
                .switchIfEmpty(Mono.error(new EntityNotFoundException("Student with ID " + studentId + " not found")));
        Mono<Course> courseMono = courseRepository.findById(courseId)
                .switchIfEmpty(Mono.error(new EntityNotFoundException("Course with ID " + courseId + " not found")));

        return Mono.zip(studentMono, courseMono)
                        .flatMap(tuple -> {
                            Student student = tuple.getT1();
                            return customRepository.addCourseToStudent(courseId, studentId)
                                    .thenReturn(student)
                                    .flatMap(this::fetchRelatedEntitiesForStudent)
                                    .map(this::buildStudentDto);
                        });
    }

    private Mono<Tuple2<Student, List<Course>>> fetchRelatedEntitiesForStudent(Student student) {
        return Mono.zip(
                Mono.just(student),
                courseRepository.findAllByStudentId(student.getId()).collectList()
        );
    }

    private StudentDto buildStudentDto(Tuple2<Student, List<Course>> tuple) {
        Student student = tuple.getT1();
        List<Course> courses = tuple.getT2();

        StudentDto studentDto = studentMapper.toStudentDto(student);
        Set<CourseDto> courseDtos = courses.stream()
                .map(courseMapper::toCourseDto)
                .collect(Collectors.toSet());
        studentDto.setCourses(courseDtos);
        return studentDto;
    }
}
