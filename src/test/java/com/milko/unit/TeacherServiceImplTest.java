package com.milko.unit;

import com.milko.dto.TeacherDto;
import com.milko.exception.EntityNotFoundException;
import com.milko.mapper.CourseMapper;
import com.milko.mapper.DepartmentMapper;
import com.milko.mapper.TeacherMapper;
import com.milko.model.Teacher;
import com.milko.repository.CourseRepository;
import com.milko.repository.DepartmentRepository;
import com.milko.repository.TeacherRepository;
import com.milko.service.impl.TeacherServiceImpl;
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
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
public class TeacherServiceImplTest {
    @Mock
    private TeacherRepository teacherRepository;
    @Mock
    private DepartmentRepository departmentRepository;
    @Mock
    private CourseRepository courseRepository;
    @Mock
    private TeacherMapper teacherMapper;
    @Mock
    private DepartmentMapper departmentMapper;
    @Mock
    private CourseMapper courseMapper;

    @InjectMocks
    private TeacherServiceImpl teacherService;

    private TeacherDto teacherDto;

    private Teacher teacher;

    @BeforeEach
    public void init(){
        teacherDto = TeacherDto.builder()
                .id(1L)
                .name("name")
                .build();

        teacher = new Teacher();
        teacher.setId(1L);
        teacher.setName("name");
    }

    @Test
    void createShouldSaveAndReturnTeacherDto() {
        Mockito.when(teacherMapper.toTeacher(teacherDto)).thenReturn(teacher);
        Mockito.when(teacherRepository.save(teacher)).thenReturn(Mono.just(teacher));
        Mockito.when(teacherMapper.toTeacherDto(teacher)).thenReturn(teacherDto);

        Mono<TeacherDto> result = teacherService.create(teacherDto);

        StepVerifier.create(result)
                .assertNext(dto -> {
                    assertNotNull(dto);
                    assertEquals(teacherDto.getId(), dto.getId());
                    assertEquals(teacherDto.getName(), dto.getName());
                })
                .verifyComplete();

        Mockito.verify(teacherMapper).toTeacher(teacherDto);
        Mockito.verify(teacherRepository).save(teacher);
        Mockito.verify(teacherMapper).toTeacherDto(teacher);
    }

    @Test
    void createShouldThrowExceptionWhenSaveFails() {
        Mockito.when(teacherMapper.toTeacher(teacherDto)).thenReturn(teacher);
        Mockito.when(teacherRepository.save(teacher)).thenReturn(Mono.error(new RuntimeException("Database save error")));

        Mono<TeacherDto> result = teacherService.create(teacherDto);

        StepVerifier.create(result)
                .expectErrorMatches(e -> e instanceof RuntimeException &&
                        e.getMessage().equals("Database save error"))
                .verify();

        Mockito.verify(teacherMapper).toTeacher(teacherDto);
        Mockito.verify(teacherRepository).save(teacher);
    }


    @Test
    void updateShouldUpdateTeacherAndReturnDto() {
        teacher.setName("updated name");
        teacherDto.setName("updated name");
        Mockito.when(teacherRepository.findById(teacher.getId())).thenReturn(Mono.just(teacher));
        Mockito.when(teacherRepository.update(teacher)).thenReturn(Mono.just(teacher));
        Mockito.when(teacherMapper.toTeacherDto(teacher)).thenReturn(teacherDto);
        Mockito.when(departmentRepository.findByHeadOfDepartmentId(any())).thenReturn(Mono.empty());
        Mockito.when(courseRepository.findAllByTeacherId(any())).thenReturn(Flux.empty());

        Mono<TeacherDto> result = teacherService.update(teacherDto);

        StepVerifier.create(result)
                .assertNext(dto -> {
                    assertNotNull(dto);
                    assertEquals(teacherDto.getId(), dto.getId());
                    assertEquals(teacherDto.getName(), dto.getName());
                })
                .verifyComplete();

        Mockito.verify(teacherRepository).findById(teacher.getId());
        Mockito.verify(teacherRepository).update(teacher);
        Mockito.verify(teacherMapper).toTeacherDto(teacher);
        Mockito.verify(departmentRepository).findByHeadOfDepartmentId(any());
        Mockito.verify(courseRepository).findAllByTeacherId(any());
    }

    @Test
    void updateShouldThrowExceptionWhenTeacherNotFound() {
        Mockito.when(teacherRepository.findById(teacher.getId())).thenReturn(Mono.empty());

        Mono<TeacherDto> result = teacherService.update(teacherDto);

        StepVerifier.create(result)
                .expectErrorMatches(e -> e instanceof EntityNotFoundException &&
                        e.getMessage().equals("Teacher with ID " + teacher.getId() + " not found"))
                .verify();

        Mockito.verify(teacherRepository).findById(teacher.getId());
        Mockito.verifyNoMoreInteractions(teacherRepository);
    }

    @Test
    void findByIdShouldReturnTeacherDto() {
        Mockito.when(teacherRepository.findById(teacher.getId())).thenReturn(Mono.just(teacher));
        Mockito.when(teacherMapper.toTeacherDto(teacher)).thenReturn(teacherDto);
        Mockito.when(departmentRepository.findByHeadOfDepartmentId(any())).thenReturn(Mono.empty());
        Mockito.when(courseRepository.findAllByTeacherId(any())).thenReturn(Flux.empty());

        Mono<TeacherDto> result = teacherService.findById(teacher.getId());

        StepVerifier.create(result)
                .assertNext(dto -> {
                    assertNotNull(dto);
                    assertEquals(teacherDto.getId(), dto.getId());
                    assertEquals(teacherDto.getName(), dto.getName());
                })
                .verifyComplete();

        Mockito.verify(teacherRepository).findById(teacher.getId());
        Mockito.verify(teacherMapper).toTeacherDto(teacher);
        Mockito.verify(departmentRepository).findByHeadOfDepartmentId(any());
        Mockito.verify(courseRepository).findAllByTeacherId(any());
    }

    @Test
    void findByIdShouldThrowExceptionWhenTeacherNotFound() {
        Mockito.when(teacherRepository.findById(teacher.getId())).thenReturn(Mono.empty());

        Mono<TeacherDto> result = teacherService.findById(teacher.getId());

        StepVerifier.create(result)
                .expectErrorMatches(e -> e instanceof EntityNotFoundException &&
                        e.getMessage().equals("Teacher with ID " + teacher.getId() + " not found"))
                .verify();

        Mockito.verify(teacherRepository).findById(teacher.getId());
    }

    @Test
    void findAllShouldReturnListOfTeacherDtos() {
        Mockito.when(teacherRepository.findAll()).thenReturn(Flux.just(teacher));
        Mockito.when(departmentRepository.findAllByHeadOfDepartmentIds(any())).thenReturn(Flux.empty());
        Mockito.when(teacherMapper.toTeacherDto(teacher)).thenReturn(teacherDto);

        Flux<TeacherDto> result = teacherService.findAll();

        StepVerifier.create(result)
                .assertNext(dto -> {
                    assertNotNull(dto);
                    assertEquals(teacherDto.getId(), dto.getId());
                    assertEquals(teacherDto.getName(), dto.getName());
                })
                .verifyComplete();

        Mockito.verify(teacherRepository).findAll();
        Mockito.verify(departmentRepository).findAllByHeadOfDepartmentIds(any());
        Mockito.verify(teacherMapper).toTeacherDto(teacher);
    }

    @Test
    void findAllShouldReturnEmptyListWhenNoTeachersFound() {
        Mockito.when(teacherRepository.findAll()).thenReturn(Flux.empty());
        Mockito.when(departmentRepository.findAllByHeadOfDepartmentIds(Mockito.anyList())).thenReturn(Flux.empty());

        Flux<TeacherDto> result = teacherService.findAll();

        StepVerifier.create(result)
                .expectNextCount(0)
                .verifyComplete();

        Mockito.verify(teacherRepository).findAll();
        Mockito.verify(departmentRepository).findAllByHeadOfDepartmentIds(Mockito.anyList());
    }


    @Test
    void deleteByIdShouldCompleteSuccessfully() {
        Mockito.when(teacherRepository.deleteById(teacher.getId())).thenReturn(Mono.empty());

        Mono<Void> result = teacherService.deleteById(teacher.getId());

        StepVerifier.create(result)
                .verifyComplete();

        Mockito.verify(teacherRepository).deleteById(teacher.getId());
    }

    @Test
    void deleteByIdShouldThrowExceptionWhenDeletionFails() {
        Mockito.when(teacherRepository.deleteById(teacher.getId())).thenReturn(Mono.error(new RuntimeException("Database deletion error")));

        Mono<Void> result = teacherService.deleteById(teacher.getId());

        StepVerifier.create(result)
                .expectErrorMatches(e -> e instanceof RuntimeException &&
                        e.getMessage().equals("Database deletion error"))
                .verify();

        Mockito.verify(teacherRepository).deleteById(teacher.getId());
    }


}
