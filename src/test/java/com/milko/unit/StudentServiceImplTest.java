package com.milko.unit;

import com.milko.dto.CourseDto;
import com.milko.dto.StudentDto;
import com.milko.exception.EntityNotFoundException;
import com.milko.mapper.CourseMapper;
import com.milko.mapper.StudentMapper;
import com.milko.mapper.TeacherMapper;
import com.milko.model.Course;
import com.milko.model.CourseStudent;
import com.milko.model.Student;
import com.milko.repository.CourseRepository;
import com.milko.repository.CourseStudentRepository;
import com.milko.repository.StudentRepository;
import com.milko.repository.TeacherRepository;
import com.milko.service.impl.StudentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
public class StudentServiceImplTest {
    @Mock
    private StudentRepository studentRepository;
    @Mock
    private CourseRepository courseRepository;
    @Mock
    private TeacherRepository teacherRepository;
    @Mock
    private CourseStudentRepository courseStudentRepository;
    @Mock
    private StudentMapper studentMapper;
    @Mock
    private CourseMapper courseMapper;
    @Mock
    private TeacherMapper teacherMapper;
    @InjectMocks
    private StudentServiceImpl studentService;

    private StudentDto studentDto;
    private Student student;
    private Course course;
    private CourseDto courseDto;

    @BeforeEach
    public void init(){
        studentDto = StudentDto.builder()
                .id(1L)
                .name("John Doe")
                .build();

        student = new Student();
        student.setId(1L);
        student.setName("John Doe");

        course = new Course();
        course.setId(2L);
        course.setTitle("Math 101");
        course.setTeacherId(3L);

        courseDto = CourseDto.builder()
                .id(2L)
                .title("Math 101")
                .build();
    }

    @Test
    void createShouldSaveStudentAndReturnDto() {
        Mockito.when(studentMapper.toStudent(studentDto)).thenReturn(student);
        Mockito.when(studentRepository.save(student)).thenReturn(Mono.just(student));
        Mockito.when(studentMapper.toStudentDto(student)).thenReturn(studentDto);

        Mono<StudentDto> result = studentService.create(studentDto);

        StepVerifier.create(result)
                .assertNext(dto -> {
                    assertNotNull(dto);
                    assertEquals(studentDto.getId(), dto.getId());
                    assertEquals(studentDto.getName(), dto.getName());
                })
                .verifyComplete();

        Mockito.verify(studentMapper).toStudent(studentDto);
        Mockito.verify(studentRepository).save(student);
        Mockito.verify(studentMapper).toStudentDto(student);
    }

    @Test
    void createShouldThrowExceptionWhenSaveFails() {
        String errorMessage = "Database error during save";

        Mockito.when(studentMapper.toStudent(studentDto)).thenReturn(student);
        Mockito.when(studentRepository.save(student))
                .thenReturn(Mono.error(new RuntimeException(errorMessage)));

        Mono<StudentDto> result = studentService.create(studentDto);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException &&
                        throwable.getMessage().equals(errorMessage))
                .verify();

        Mockito.verify(studentMapper).toStudent(studentDto);
        Mockito.verify(studentRepository).save(student);
    }

    @Test
    void findAllShouldReturnListOfStudentsWithoutRelatedEntities() {
        Mockito.when(studentRepository.findAll()).thenReturn(Flux.just(student));
        Mockito.when(courseRepository.findAllByStudentsIdList(Mockito.anyList())).thenReturn(Flux.empty());
        Mockito.when(teacherRepository.findAllByCoursesIdList(Mockito.anyList())).thenReturn(Flux.empty());
        Mockito.when(studentMapper.toStudentDto(student)).thenReturn(studentDto);

        Flux<StudentDto> result = studentService.findAll();

        StepVerifier.create(result)
                .assertNext(dto -> {
                    assertNotNull(dto);
                    assertEquals(studentDto.getId(), dto.getId());
                    assertEquals(studentDto.getName(), dto.getName());
                })
                .verifyComplete();

        Mockito.verify(studentRepository).findAll();
        Mockito.verify(courseRepository).findAllByStudentsIdList(Mockito.anyList());
        Mockito.verify(teacherRepository).findAllByCoursesIdList(Mockito.anyList());
        Mockito.verify(studentMapper).toStudentDto(student);
        Mockito.verifyNoMoreInteractions(studentRepository, courseRepository, teacherRepository, studentMapper);
    }

    @Test
    void findAllShouldReturnEmptyList() {
        Mockito.when(studentRepository.findAll()).thenReturn(Flux.empty());
        Mockito.when(courseRepository.findAllByStudentsIdList(Mockito.anyList())).thenReturn(Flux.empty());
        Mockito.when(teacherRepository.findAllByCoursesIdList(Mockito.anyList())).thenReturn(Flux.empty());

        Flux<StudentDto> result = studentService.findAll();

        StepVerifier.create(result)
                .expectNextCount(0)
                .verifyComplete();

        Mockito.verify(studentRepository).findAll();
        Mockito.verify(courseRepository).findAllByStudentsIdList(Mockito.anyList());
        Mockito.verify(teacherRepository).findAllByCoursesIdList(Mockito.anyList());
        Mockito.verifyNoMoreInteractions(studentRepository, courseRepository, teacherRepository, studentMapper);
    }


    @Test
    void findByIdShouldReturnStudentDto() {
        Mockito.when(studentRepository.findById(student.getId())).thenReturn(Mono.just(student));
        Mockito.when(courseRepository.findAllByStudentId(student.getId())).thenReturn(Flux.empty());
        Mockito.when(studentMapper.toStudentDto(student)).thenReturn(studentDto);

        Mono<StudentDto> result = studentService.findById(student.getId());

        StepVerifier.create(result)
                .assertNext(dto -> {
                    assertNotNull(dto);
                    assertEquals(studentDto.getId(), dto.getId());
                    assertEquals(studentDto.getName(), dto.getName());
                })
                .verifyComplete();

        Mockito.verify(studentRepository).findById(student.getId());
        Mockito.verify(courseRepository).findAllByStudentId(student.getId());
        Mockito.verify(studentMapper).toStudentDto(student);
    }

    @Test
    void findByIdShouldThrowExceptionWhenNotFound() {
        Long studentId = 999L;

        Mockito.when(studentRepository.findById(studentId))
                .thenReturn(Mono.error(new EntityNotFoundException("Student with ID " + studentId + " not found")));

        Mono<StudentDto> result = studentService.findById(studentId);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof EntityNotFoundException &&
                        throwable.getMessage().equals("Student with ID " + studentId + " not found"))
                .verify();

        Mockito.verify(studentRepository).findById(studentId);
    }

    @Test
    void updateShouldUpdateStudentAndReturnDto() {
        Mockito.when(studentRepository.findById(studentDto.getId())).thenReturn(Mono.just(student));
        Mockito.when(studentRepository.update(student)).thenReturn(Mono.just(student));
        Mockito.when(courseRepository.findAllByStudentId(student.getId())).thenReturn(Flux.empty());
        Mockito.when(studentMapper.toStudentDto(student)).thenReturn(studentDto);

        Mono<StudentDto> result = studentService.update(studentDto);

        StepVerifier.create(result)
                .assertNext(dto -> {
                    assertNotNull(dto);
                    assertEquals(studentDto.getId(), dto.getId());
                    assertEquals(studentDto.getName(), dto.getName());
                })
                .verifyComplete();

        Mockito.verify(studentRepository).findById(studentDto.getId());
        Mockito.verify(studentRepository).update(student);
        Mockito.verify(courseRepository).findAllByStudentId(student.getId());
        Mockito.verify(studentMapper).toStudentDto(student);
        Mockito.verify(studentMapper).updateFromDto(studentDto, student);
    }

    @Test
    void updateShouldThrowExceptionWhenStudentNotFound() {
        Long studentId = 999L;
        studentDto.setId(studentId);
        Mockito.when(studentRepository.findById(studentId)).thenReturn(Mono.empty());

        Mono<StudentDto> result = studentService.update(studentDto);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof EntityNotFoundException &&
                        throwable.getMessage().equals("Student with ID " + studentId + " not found"))
                .verify();

        Mockito.verify(studentRepository).findById(studentId);
        Mockito.verifyNoMoreInteractions(studentRepository, studentMapper);
    }


    @Test
    void deleteByIdShouldRemoveStudent() {
        Mockito.when(studentRepository.deleteById(student.getId())).thenReturn(Mono.empty());

        Mono<Void> result = studentService.deleteById(student.getId());

        StepVerifier.create(result)
                .verifyComplete();

        Mockito.verify(studentRepository).deleteById(student.getId());
    }

    @Test
    void deleteByIdShouldThrowExceptionWhenDeleteFails() {
        String errorMessage = "Failed to delete student";

        Mockito.when(studentRepository.deleteById(student.getId()))
                .thenReturn(Mono.error(new RuntimeException(errorMessage)));

        Mono<Void> result = studentService.deleteById(student.getId());

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException &&
                        throwable.getMessage().equals(errorMessage))
                .verify();

        Mockito.verify(studentRepository).deleteById(student.getId());
    }

    @Test
    void addCourseToStudentShouldAddCourseAndReturnUpdatedStudent() {
        Mockito.when(studentRepository.findById(student.getId())).thenReturn(Mono.just(student));
        Mockito.when(courseRepository.findById(course.getId())).thenReturn(Mono.just(course));
        Mockito.when(courseStudentRepository.save(Mockito.any(CourseStudent.class))).thenReturn(Mono.just(new CourseStudent()));
        Mockito.when(courseRepository.findAllByStudentId(student.getId())).thenReturn(Flux.just(course));
        Mockito.when(studentMapper.toStudentDto(student)).thenReturn(studentDto);
        Mockito.when(courseMapper.toCourseDto(course)).thenReturn(courseDto);

        Mono<StudentDto> result = studentService.addCourseToStudent(student.getId(), course.getId());

        StepVerifier.create(result)
                .assertNext(dto -> {
                    assertNotNull(dto);
                    assertEquals(studentDto.getId(), dto.getId());
                    assertEquals(studentDto.getName(), dto.getName());
                    assertEquals(studentDto.getCourses().getFirst().getTitle(), courseDto.getTitle());
                })
                .verifyComplete();

        Mockito.verify(studentRepository).findById(student.getId());
        Mockito.verify(courseRepository).findById(course.getId());
        Mockito.verify(courseStudentRepository).save(Mockito.any(CourseStudent.class));
        Mockito.verify(courseRepository).findAllByStudentId(student.getId());
        Mockito.verify(studentMapper).toStudentDto(student);
        Mockito.verify(courseMapper).toCourseDto(course);
    }

    @Test
    void addCourseToStudentShouldThrowExceptionWhenStudentNotFound() {
        Long studentId = 999L;

        Mockito.when(studentRepository.findById(studentId))
                .thenReturn(Mono.error(new EntityNotFoundException("Student with ID " + studentId + " not found")));
        Mockito.when(courseRepository.findById(course.getId())).thenReturn(Mono.just(course));

        Mono<StudentDto> result = studentService.addCourseToStudent(studentId, course.getId());

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof EntityNotFoundException &&
                        throwable.getMessage().equals("Student with ID " + studentId + " not found"))
                .verify();

        Mockito.verify(studentRepository).findById(studentId);
        Mockito.verify(courseRepository).findById(course.getId());
        Mockito.verifyNoInteractions(courseStudentRepository, studentMapper, courseMapper);
    }
}
