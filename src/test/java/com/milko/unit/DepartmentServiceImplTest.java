package com.milko.unit;

import com.milko.dto.DepartmentDto;
import com.milko.dto.TeacherDto;
import com.milko.exception.EntityNotFoundException;
import com.milko.mapper.DepartmentMapper;
import com.milko.mapper.TeacherMapper;
import com.milko.model.Department;
import com.milko.model.Teacher;
import com.milko.repository.DepartmentRepository;
import com.milko.repository.TeacherRepository;
import com.milko.service.impl.DepartmentServiceImpl;
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
public class DepartmentServiceImplTest {
    @Mock
    private DepartmentRepository departmentRepository;
    @Mock
    private TeacherRepository teacherRepository;
    @Mock
    private DepartmentMapper departmentMapper;
    @Mock
    private TeacherMapper teacherMapper;
    @InjectMocks
    private DepartmentServiceImpl departmentService;

    private DepartmentDto departmentDto;

    private Department department;

    private Teacher teacher;

    private DepartmentDto expectedDepartmentDto;

    @BeforeEach
    public void init(){
        departmentDto = DepartmentDto.builder()
                .id(1L)
                .name("Test Department")
                .build();

        department = new Department();
        department.setId(1L);
        department.setName("Test Department");

        teacher = new Teacher();
        teacher.setId(2L);

        expectedDepartmentDto = DepartmentDto.builder()
                .id(1L)
                .name("Test Department")
                .build();
    }

    @Test
    void createShouldSaveDepartmentAndReturnDto() {
        Mockito.when(departmentMapper.toDepartment(departmentDto)).thenReturn(department);
        Mockito.when(departmentRepository.save(department)).thenReturn(Mono.just(department));
        Mockito.when(departmentMapper.toDepartmentDto(department)).thenReturn(expectedDepartmentDto);

        Mono<DepartmentDto> result = departmentService.create(departmentDto);

        StepVerifier.create(result)
                .assertNext(dto -> {
                    assertNotNull(dto);
                    assertEquals(expectedDepartmentDto.getId(), dto.getId());
                    assertEquals(expectedDepartmentDto.getName(), dto.getName());
                })
                .verifyComplete();

        Mockito.verify(departmentMapper).toDepartment(departmentDto);
        Mockito.verify(departmentRepository).save(department);
        Mockito.verify(departmentMapper).toDepartmentDto(department);
    }

    @Test
    void createShouldThrowExceptionWhenSaveFails() {
        String errorMessage = "Database save error";
        Mockito.when(departmentMapper.toDepartment(departmentDto)).thenReturn(department);
        Mockito.when(departmentRepository.save(department)).thenReturn(Mono.error(new RuntimeException(errorMessage)));

        Mono<DepartmentDto> result = departmentService.create(departmentDto);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException &&
                        throwable.getMessage().equals(errorMessage))
                .verify();

        Mockito.verify(departmentMapper).toDepartment(departmentDto);
        Mockito.verify(departmentRepository).save(department);
    }

    @Test
    void updateShouldUpdateDepartmentAndReturnDto() {
        Mockito.when(departmentRepository.findById(departmentDto.getId())).thenReturn(Mono.just(department));
        Mockito.when(departmentRepository.update(department)).thenReturn(Mono.just(department));
        Mockito.when(teacherRepository.findByDepartmentId(department.getId())).thenReturn(Mono.empty());
        Mockito.when(departmentMapper.toDepartmentDto(department)).thenReturn(expectedDepartmentDto);

        Mockito.doAnswer(invocation -> {
            DepartmentDto dto = invocation.getArgument(0);
            Department entity = invocation.getArgument(1);
            entity.setName(dto.getName());
            return null;
        }).when(departmentMapper).updateFromDto(departmentDto, department);

        Mono<DepartmentDto> result = departmentService.update(departmentDto);

        StepVerifier.create(result)
                .assertNext(dto -> {
                    assertNotNull(dto);
                    assertEquals(expectedDepartmentDto.getId(), dto.getId());
                    assertEquals(expectedDepartmentDto.getName(), dto.getName());
                })
                .verifyComplete();

        Mockito.verify(departmentRepository).findById(departmentDto.getId());
        Mockito.verify(departmentRepository).update(department);
        Mockito.verify(departmentMapper).toDepartmentDto(department);
        Mockito.verify(departmentMapper).updateFromDto(departmentDto, department);
    }

    @Test
    void updateShouldThrowExceptionWhenDepartmentNotFound() {
        Long departmentId = 999L;
        departmentDto.setId(departmentId);

        Mockito.when(departmentRepository.findById(departmentId)).thenReturn(Mono.empty());

        Mono<DepartmentDto> result = departmentService.update(departmentDto);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof EntityNotFoundException &&
                        throwable.getMessage().equals("Department with ID " + departmentId + " not found"))
                .verify();

        Mockito.verify(departmentRepository).findById(departmentId);
    }

    @Test
    void findByIdShouldReturnDepartmentDto() {
        Mockito.when(departmentRepository.findById(department.getId())).thenReturn(Mono.just(department));
        Mockito.when(teacherRepository.findByDepartmentId(department.getId())).thenReturn(Mono.just(teacher));
        Mockito.when(departmentMapper.toDepartmentDto(department)).thenReturn(expectedDepartmentDto);
        Mockito.when(teacherMapper.toTeacherDto(teacher)).thenReturn(TeacherDto.builder().id(teacher.getId()).build());

        Mono<DepartmentDto> result = departmentService.findById(department.getId());

        StepVerifier.create(result)
                .assertNext(dto -> {
                    assertNotNull(dto);
                    assertEquals(expectedDepartmentDto.getId(), dto.getId());
                    assertEquals(expectedDepartmentDto.getName(), dto.getName());
                })
                .verifyComplete();

        Mockito.verify(departmentRepository).findById(department.getId());
        Mockito.verify(teacherRepository).findByDepartmentId(department.getId());
        Mockito.verify(departmentMapper).toDepartmentDto(department);
        Mockito.verify(teacherMapper).toTeacherDto(teacher);
    }

    @Test
    void findByIdShouldThrowExceptionWhenDepartmentNotFound() {
        Long departmentId = 999L;

        Mockito.when(departmentRepository.findById(departmentId)).thenReturn(Mono.empty());

        Mono<DepartmentDto> result = departmentService.findById(departmentId);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof EntityNotFoundException &&
                        throwable.getMessage().equals("Department with ID " + departmentId + " not found"))
                .verify();

        Mockito.verify(departmentRepository).findById(departmentId);
    }

    @Test
    void findAllShouldReturnListOfDepartments() {
        Mockito.when(departmentRepository.findAll()).thenReturn(Flux.just(department));
        Mockito.when(teacherRepository.findAllByDepartmentsIdList(List.of(department.getId()))).thenReturn(Flux.empty());
        Mockito.when(departmentMapper.toDepartmentDto(department)).thenReturn(expectedDepartmentDto);

        Flux<DepartmentDto> result = departmentService.findAll();

        StepVerifier.create(result)
                .assertNext(dto -> {
                    assertNotNull(dto);
                    assertEquals(expectedDepartmentDto.getId(), dto.getId());
                    assertEquals(expectedDepartmentDto.getName(), dto.getName());
                })
                .verifyComplete();

        Mockito.verify(departmentRepository).findAll();
        Mockito.verify(teacherRepository).findAllByDepartmentsIdList(List.of(department.getId()));
        Mockito.verify(departmentMapper).toDepartmentDto(department);
    }

    @Test
    void findAllShouldReturnEmptyList() {
        Mockito.when(departmentRepository.findAll()).thenReturn(Flux.empty());
        Mockito.when(teacherRepository.findAllByDepartmentsIdList(Mockito.anyList())).thenReturn(Flux.empty());

        Flux<DepartmentDto> result = departmentService.findAll();

        StepVerifier.create(result)
                .expectNextCount(0)
                .verifyComplete();

        Mockito.verify(departmentRepository).findAll();
        Mockito.verifyNoInteractions(departmentMapper);
    }

    @Test
    void deleteByIdShouldRemoveDepartment() {
        Mockito.when(departmentRepository.deleteById(department.getId())).thenReturn(Mono.empty());

        Mono<Void> result = departmentService.deleteById(department.getId());

        StepVerifier.create(result)
                .verifyComplete();

        Mockito.verify(departmentRepository).deleteById(department.getId());
    }

    @Test
    void deleteByIdShouldThrowExceptionWhenDeleteFails() {
        String errorMessage = "Failed to delete department";
        Mockito.when(departmentRepository.deleteById(department.getId()))
                .thenReturn(Mono.error(new RuntimeException(errorMessage)));

        Mono<Void> result = departmentService.deleteById(department.getId());

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException &&
                        throwable.getMessage().equals(errorMessage))
                .verify();

        Mockito.verify(departmentRepository).deleteById(department.getId());
    }

    @Test
    void setTeacherToDepartmentShouldAssignTeacher() {
        Long teacherId = 2L;
        Teacher teacher = new Teacher();
        teacher.setId(teacherId);

        Mockito.when(departmentRepository.findById(department.getId())).thenReturn(Mono.just(department));
        Mockito.when(teacherRepository.findById(teacherId)).thenReturn(Mono.just(teacher));
        Mockito.when(departmentRepository.update(department)).thenReturn(Mono.just(department));
        Mockito.when(teacherRepository.findByDepartmentId(department.getId())).thenReturn(Mono.just(teacher));
        Mockito.when(departmentMapper.toDepartmentDto(department)).thenReturn(expectedDepartmentDto);
        Mockito.when(teacherMapper.toTeacherDto(teacher)).thenReturn(TeacherDto.builder().id(teacherId).build());

        Mono<DepartmentDto> result = departmentService.setTeacherToDepartment(department.getId(), teacherId);

        StepVerifier.create(result)
                .assertNext(dto -> {
                    assertNotNull(dto);
                    assertEquals(expectedDepartmentDto.getId(), dto.getId());
                    assertEquals(expectedDepartmentDto.getName(), dto.getName());
                    assertNotNull(dto.getHeadOfDepartment());
                    assertEquals(teacherId, dto.getHeadOfDepartment().getId());
                })
                .verifyComplete();

        Mockito.verify(departmentRepository).findById(department.getId());
        Mockito.verify(teacherRepository).findById(teacherId);
        Mockito.verify(departmentRepository).update(department);
        Mockito.verify(teacherRepository).findByDepartmentId(department.getId());
        Mockito.verify(departmentMapper).toDepartmentDto(department);
        Mockito.verify(teacherMapper).toTeacherDto(teacher);
    }

    @Test
    void setTeacherToDepartmentShouldThrowExceptionWhenDepartmentNotFound() {
        Long teacherId = 2L;

        Mockito.when(departmentRepository.findById(department.getId()))
                .thenReturn(Mono.error(new EntityNotFoundException("Department with ID " + department.getId() + " not found")));
        Mockito.when(teacherRepository.findById(teacherId)).thenReturn(Mono.empty());

        Mono<DepartmentDto> result = departmentService.setTeacherToDepartment(department.getId(), teacherId);

        StepVerifier.create(result)
                .expectErrorSatisfies(throwable -> {
                    assertInstanceOf(EntityNotFoundException.class, throwable);
                    assertEquals("Department with ID " + department.getId() + " not found", throwable.getMessage());
                })
                .verify();

        Mockito.verify(departmentRepository).findById(department.getId());
        Mockito.verifyNoInteractions(departmentMapper, teacherMapper);
    }

}
