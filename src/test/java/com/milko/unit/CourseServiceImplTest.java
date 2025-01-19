package com.milko.unit;

import com.milko.dto.CourseDto;
import com.milko.exception.EntityNotFoundException;
import com.milko.mapper.CourseMapper;
import com.milko.mapper.StudentMapper;
import com.milko.mapper.TeacherMapper;
import com.milko.model.Course;
import com.milko.model.Teacher;
import com.milko.repository.CourseRepository;
import com.milko.repository.StudentRepository;
import com.milko.repository.TeacherRepository;
import com.milko.service.impl.CourseServiceImpl;
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

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class CourseServiceImplTest {
    @Mock
    private CourseRepository courseRepository;
    @Mock
    private StudentRepository studentRepository;
    @Mock
    private TeacherRepository teacherRepository;
    @Mock
    private CourseMapper courseMapper;
    @Mock
    private TeacherMapper teacherMapper;
    @Mock
    private StudentMapper studentMapper;
    @InjectMocks
    private CourseServiceImpl courseService;

    private CourseDto courseDto;

    private Course course;


    @BeforeEach
    public void init(){
        course = new Course();
        course.setId(1L);
        course.setTitle("title");

        courseDto = CourseDto.builder()
                .id(1L)
                .title("Test Course")
                .build();
    }

    @Test
    void createShouldSaveCourseAndReturnDto() {
        Mockito.when(courseMapper.toCourse(courseDto)).thenReturn(course);
        Mockito.when(courseRepository.save(course)).thenReturn(Mono.just(course));
        Mockito.when(courseMapper.toCourseDto(course)).thenReturn(courseDto);

        Mono<CourseDto> result = courseService.create(courseDto);

        StepVerifier.create(result)
                .assertNext(dto -> {
                    assertNotNull(dto);
                    assertEquals(courseDto.getId(), dto.getId());
                    assertEquals(courseDto.getTitle(), dto.getTitle());
                })
                .verifyComplete();

        Mockito.verify(courseMapper).toCourse(courseDto);
        Mockito.verify(courseRepository).save(course);
        Mockito.verify(courseMapper).toCourseDto(course);
    }

    @Test
    void createShouldThrowExceptionWhenSaveFails() {
        String errorMessage = "Database error during save";

        Mockito.when(courseMapper.toCourse(courseDto)).thenReturn(course);
        Mockito.when(courseRepository.save(course)).thenReturn(Mono.error(new RuntimeException(errorMessage)));

        Mono<CourseDto> result = courseService.create(courseDto);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException &&
                        throwable.getMessage().equals(errorMessage))
                .verify();

        Mockito.verify(courseMapper).toCourse(courseDto);
        Mockito.verify(courseRepository).save(course);
        Mockito.verifyNoMoreInteractions(courseMapper, courseRepository);
    }

    @Test
    void updateShouldUpdateCourseAndReturnDto() {
        Mockito.when(courseRepository.findById(courseDto.getId())).thenReturn(Mono.just(course));
        Mockito.when(courseRepository.update(course)).thenReturn(Mono.just(course));
        Mockito.when(teacherRepository.findByCourseId(course.getId())).thenReturn(Mono.empty());
        Mockito.when(studentRepository.findAllByCourseId(course.getId())).thenReturn(Flux.empty());
        Mockito.when(courseMapper.toCourseDto(course)).thenReturn(courseDto);

        Mockito.doAnswer(invocation -> {
            CourseDto dto = invocation.getArgument(0);
            Course entity = invocation.getArgument(1);
            entity.setTitle(dto.getTitle());
            return null;
        }).when(courseMapper).updateFromDto(courseDto, course);

        Mono<CourseDto> result = courseService.update(courseDto);

        StepVerifier.create(result)
                .assertNext(dto -> {
                    assertNotNull(dto);
                    assertEquals(courseDto.getId(), dto.getId());
                    assertEquals(courseDto.getTitle(), dto.getTitle());
                })
                .verifyComplete();

        Mockito.verify(courseRepository).findById(courseDto.getId());
        Mockito.verify(courseRepository).update(course);
        Mockito.verify(teacherRepository).findByCourseId(course.getId());
        Mockito.verify(studentRepository).findAllByCourseId(course.getId());
        Mockito.verify(courseMapper).toCourseDto(course);
        Mockito.verify(courseMapper).updateFromDto(courseDto, course);
    }



    @Test
    void updateShouldThrowExceptionWhenCourseNotFound() {
        Long courseId = 999L;
        courseDto.setId(courseId);

        Mockito.when(courseRepository.findById(courseId)).thenReturn(Mono.empty());

        Mono<CourseDto> result = courseService.update(courseDto);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof EntityNotFoundException &&
                        throwable.getMessage().equals("Course with ID " + courseId + " not found"))
                .verify();

        Mockito.verify(courseRepository).findById(courseId);
        Mockito.verifyNoMoreInteractions(courseRepository, courseMapper);
    }

    @Test
    void findByIdShouldReturnCourseDto() {
        Mockito.when(courseRepository.findById(course.getId())).thenReturn(Mono.just(course));
        Mockito.when(studentRepository.findAllByCourseId(course.getId())).thenReturn(Flux.empty());
        Mockito.when(teacherRepository.findByCourseId(course.getId())).thenReturn(Mono.empty());
        Mockito.when(courseMapper.toCourseDto(course)).thenReturn(courseDto);

        Mono<CourseDto> result = courseService.findById(course.getId());

        StepVerifier.create(result)
                .assertNext(dto -> {
                    assertNotNull(dto);
                    assertEquals(courseDto.getId(), dto.getId());
                    assertEquals(courseDto.getTitle(), dto.getTitle());
                })
                .verifyComplete();

        Mockito.verify(courseRepository).findById(course.getId());
        Mockito.verify(studentRepository).findAllByCourseId(course.getId());
        Mockito.verify(teacherRepository).findByCourseId(course.getId());
        Mockito.verify(courseMapper).toCourseDto(course);
    }

    @Test
    void findByIdShouldThrowExceptionWhenCourseNotFound() {
        Long courseId = 999L;

        Mockito.when(courseRepository.findById(courseId)).thenReturn(Mono.empty());

        Mono<CourseDto> result = courseService.findById(courseId);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof EntityNotFoundException &&
                        throwable.getMessage().equals("Course with ID " + courseId + " not found"))
                .verify();

        Mockito.verify(courseRepository).findById(courseId);
        Mockito.verifyNoMoreInteractions(courseRepository, studentRepository, teacherRepository, courseMapper);
    }

    @Test
    void findAllShouldReturnListOfCourses() {
        Mockito.when(courseRepository.findAll()).thenReturn(Flux.just(course));
        Mockito.when(teacherRepository.findAllByCoursesIdList(List.of(course.getId()))).thenReturn(Flux.empty());
        Mockito.when(studentRepository.findAllByCoursesIdList(List.of(course.getId()))).thenReturn(Flux.empty());
        Mockito.when(courseMapper.toCourseDto(course)).thenReturn(courseDto);

        Flux<CourseDto> result = courseService.findAll();

        StepVerifier.create(result)
                .assertNext(dto -> {
                    assertNotNull(dto);
                    assertEquals(courseDto.getId(), dto.getId());
                    assertEquals(courseDto.getTitle(), dto.getTitle());
                })
                .verifyComplete();

        Mockito.verify(courseRepository).findAll();
        Mockito.verify(teacherRepository).findAllByCoursesIdList(List.of(course.getId()));
        Mockito.verify(studentRepository).findAllByCoursesIdList(List.of(course.getId()));
        Mockito.verify(courseMapper).toCourseDto(course);
    }

    @Test
    void findAllShouldReturnEmptyList() {
        Mockito.when(courseRepository.findAll()).thenReturn(Flux.empty());
        Mockito.when(teacherRepository.findAllByCoursesIdList(Mockito.anyList())).thenReturn(Flux.empty());
        Mockito.when(studentRepository.findAllByCoursesIdList(Mockito.anyList())).thenReturn(Flux.empty());

        Flux<CourseDto> result = courseService.findAll();

        StepVerifier.create(result)
                .expectNextCount(0)
                .verifyComplete();

        Mockito.verify(courseRepository).findAll();
        Mockito.verifyNoMoreInteractions(teacherRepository, studentRepository, courseMapper);
    }


    @Test
    void deleteByIdShouldRemoveCourse() {
        Mockito.when(courseRepository.deleteById(course.getId())).thenReturn(Mono.empty());

        Mono<Void> result = courseService.deleteById(course.getId());

        StepVerifier.create(result)
                .verifyComplete();

        Mockito.verify(courseRepository).deleteById(course.getId());
    }

    @Test
    void setTeacherToCourseShouldAssignTeacher() {
        Long teacherId = 2L;
        Teacher teacher = new Teacher();
        teacher.setId(teacherId);

        Mockito.when(courseRepository.findById(course.getId())).thenReturn(Mono.just(course));
        Mockito.when(teacherRepository.findById(teacherId)).thenReturn(Mono.just(teacher));
        Mockito.when(courseRepository.update(course)).thenReturn(Mono.just(course));
        Mockito.when(studentRepository.findAllByCourseId(course.getId())).thenReturn(Flux.empty());
        Mockito.when(teacherRepository.findByCourseId(course.getId())).thenReturn(Mono.just(teacher));
        Mockito.when(courseMapper.toCourseDto(course)).thenReturn(courseDto);

        Mono<CourseDto> result = courseService.setTeacherToCourse(course.getId(), teacherId);

        StepVerifier.create(result)
                .assertNext(dto -> {
                    assertNotNull(dto);
                    assertEquals(courseDto.getId(), dto.getId());
                    assertEquals(courseDto.getTitle(), dto.getTitle());
                })
                .verifyComplete();

        Mockito.verify(courseRepository).findById(course.getId());
        Mockito.verify(teacherRepository).findById(teacherId);
        Mockito.verify(courseRepository).update(course);
        Mockito.verify(studentRepository).findAllByCourseId(course.getId());
        Mockito.verify(courseMapper).toCourseDto(course);
    }



    @Test
    void setTeacherToCourseShouldThrowEntityNotFoundExceptionForCourse() {
        Long teacherId = 2L;

        Mockito.when(courseRepository.findById(course.getId()))
                .thenReturn(Mono.error(new EntityNotFoundException("Course with ID " + course.getId() + " not found")));
        Mockito.when(teacherRepository.findById(teacherId)).thenReturn(Mono.empty());

        Mono<CourseDto> result = courseService.setTeacherToCourse(course.getId(), teacherId);

        StepVerifier.create(result)
                .expectErrorSatisfies(throwable -> {
                    assertInstanceOf(EntityNotFoundException.class, throwable);
                    assertEquals("Course with ID " + course.getId() + " not found", throwable.getMessage());
                })
                .verify();

        Mockito.verify(courseRepository).findById(course.getId());
        Mockito.verifyNoInteractions(courseMapper);
    }

}
