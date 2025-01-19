package com.milko.integration;

import com.milko.dto.DepartmentDto;
import com.milko.dto.TeacherDto;
import com.milko.exceptionhandling.ErrorResponse;
import com.milko.integration.utils.DatabaseCleaner;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.reactor.http.client.ReactorHttpClient;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@RequiredArgsConstructor
@MicronautTest(environments = "test")
public class DepartmentControllerTest {
    private final DatabaseCleaner cleaner;

    @Inject
    @Client("/")
    private ReactorHttpClient client;

    private final DepartmentDto department = DepartmentDto.builder()
            .name("name")
            .build();

    private final DepartmentDto departmentToUpdate = DepartmentDto.builder()
            .name("updated name")
            .build();

    private final TeacherDto teacher = TeacherDto.builder()
            .name("name")
            .build();

    private final String departmentsPath = "/api/v1/departments";
    private final String teachersPath = "/api/v1/teachers";

    @BeforeEach
    public void cleanDataBase() {
        cleaner.clearTables().block();
    }

    @Test
    void createDepartmentTest() {
        HttpResponse<DepartmentDto> response = client.exchange(
                HttpRequest.POST(departmentsPath, department),
                DepartmentDto.class
        ).block();

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED.getCode(), response.getStatus().getCode());

        DepartmentDto createdDepartment = response.getBody().orElse(null);
        assertNotNull(createdDepartment);
        assertNotNull(createdDepartment.getId());
        assertEquals(department.getName(), createdDepartment.getName());
    }

    @Test
    void getAllDepartmentsTest() {
        client.exchange(HttpRequest.POST(departmentsPath, department), DepartmentDto.class).block();

        HttpResponse<List<DepartmentDto>> response = client.exchange(
                HttpRequest.GET(departmentsPath),
                Argument.listOf(DepartmentDto.class)
        ).block();

        assertNotNull(response);
        assertEquals(HttpStatus.OK.getCode(), response.getStatus().getCode());

        List<DepartmentDto> departments = response.getBody().orElse(Collections.emptyList());
        assertNotNull(departments);
        assertFalse(departments.isEmpty());
        DepartmentDto fetchedDepartment = departments.getFirst();
        assertNotNull(fetchedDepartment.getId());
        assertEquals(department.getName(), fetchedDepartment.getName());
    }

    @Test
    void getAllDepartmentsShouldReturnEmptyListTest() {
        HttpResponse<List<DepartmentDto>> response = client.exchange(
                HttpRequest.GET(departmentsPath),
                Argument.listOf(DepartmentDto.class)
        ).block();

        assertNotNull(response);
        assertEquals(HttpStatus.OK.getCode(), response.getStatus().getCode());

        List<DepartmentDto> departments = response.getBody().orElse(Collections.emptyList());
        assertNotNull(departments);
        assertTrue(departments.isEmpty());
    }

    @Test
    void getDepartmentByIdTest() {
        HttpResponse<DepartmentDto> created = client.exchange(
                HttpRequest.POST(departmentsPath, department),
                DepartmentDto.class
        ).block();

        assertNotNull(created);
        Long departmentId = created.getBody().orElseThrow().getId();

        HttpResponse<DepartmentDto> response = client.exchange(
                HttpRequest.GET(departmentsPath + "/" + departmentId),
                DepartmentDto.class
        ).block();

        assertNotNull(response);
        assertEquals(HttpStatus.OK.getCode(), response.getStatus().getCode());

        DepartmentDto fetchedDepartment = response.getBody().orElse(null);
        assertNotNull(fetchedDepartment);
        assertNotNull(fetchedDepartment.getId());
        assertEquals(department.getName(), fetchedDepartment.getName());
    }

    @Test
    void getDepartmentByIdShouldThrowEntityNotFoundExceptionTest() {
        long wrongId = 999L;

        HttpClientResponseException ex = assertThrows(HttpClientResponseException.class, () -> {
            client.toBlocking().exchange(
                    HttpRequest.GET(departmentsPath + "/" + wrongId),
                    ErrorResponse.class
            );
        });
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());

        HttpResponse<?> response = ex.getResponse();
        Optional<ErrorResponse> body = response.getBody(ErrorResponse.class);
        assertTrue(body.isPresent());
        assertNotNull(body.get().getTimestamp());
        assertEquals("404", body.get().getStatus());
        assertEquals("EntityNotFoundException", body.get().getError());
        assertEquals("Department with ID " + wrongId + " not found", body.get().getMessage());
        assertEquals(departmentsPath + "/" + wrongId, body.get().getPath());
    }

    @Test
    void updateDepartmentTest() {
        HttpResponse<DepartmentDto> created = client.exchange(
                HttpRequest.POST(departmentsPath, department),
                DepartmentDto.class
        ).block();

        assertNotNull(created);
        Long departmentId = created.getBody().orElseThrow().getId();

        HttpResponse<DepartmentDto> response = client.exchange(
                HttpRequest.PATCH(departmentsPath + "/" + departmentId, departmentToUpdate),
                DepartmentDto.class
        ).block();

        assertNotNull(response);
        assertEquals(HttpStatus.OK.getCode(), response.getStatus().getCode());

        DepartmentDto updatedDepartment = response.getBody().orElse(null);
        assertNotNull(updatedDepartment);
        assertNotNull(updatedDepartment.getId());
        assertEquals(departmentToUpdate.getName(), updatedDepartment.getName());
    }

    @Test
    void updateDepartmentShouldThrowEntityNotFoundExceptionTest() {
        long wrongId = 999L;

        HttpClientResponseException ex = assertThrows(HttpClientResponseException.class, () -> {
            client.toBlocking().exchange(
                    HttpRequest.PATCH(departmentsPath + "/" + wrongId, departmentToUpdate),
                    ErrorResponse.class
            );
        });
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());

        HttpResponse<?> response = ex.getResponse();
        Optional<ErrorResponse> body = response.getBody(ErrorResponse.class);
        assertTrue(body.isPresent());
        assertNotNull(body.get().getTimestamp());
        assertEquals("404", body.get().getStatus());
        assertEquals("EntityNotFoundException", body.get().getError());
        assertEquals("Department with ID " + wrongId + " not found", body.get().getMessage());
        assertEquals(departmentsPath + "/" + wrongId, body.get().getPath());
    }

    @Test
    void deleteDepartmentTest() {
        HttpResponse<DepartmentDto> created = client.exchange(
                HttpRequest.POST(departmentsPath, department),
                DepartmentDto.class
        ).block();

        assertNotNull(created);
        Long departmentId = created.getBody().orElseThrow().getId();

        HttpResponse<Void> response = client.exchange(
                HttpRequest.DELETE(departmentsPath + "/" + departmentId),
                Void.class
        ).block();

        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT.getCode(), response.getStatus().getCode());
    }

    @Test
    void deleteDepartmentShouldReturnNoContentIfEntityNotExistsTest() {
        long wrongId = 999L;

        HttpResponse<Void> response = client.exchange(
                HttpRequest.DELETE(departmentsPath + "/" + wrongId),
                Void.class
        ).block();

        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT.getCode(), response.getStatus().getCode());
    }

    @Test
    void setTeacherToDepartmentTest() {
        HttpResponse<DepartmentDto> departmentResponse = client.exchange(
                HttpRequest.POST(departmentsPath, department),
                DepartmentDto.class
        ).block();
        assertNotNull(departmentResponse);
        Long departmentId = departmentResponse.getBody().orElseThrow().getId();

        HttpResponse<TeacherDto> teacherResponse = client.exchange(
                HttpRequest.POST(teachersPath, teacher),
                TeacherDto.class
        ).block();
        assertNotNull(teacherResponse);
        Long teacherId = teacherResponse.getBody().orElseThrow().getId();

        HttpResponse<DepartmentDto> response = client.exchange(
                HttpRequest.POST(departmentsPath + "/" + departmentId + "/teacher/" + teacherId, null),
                DepartmentDto.class
        ).block();

        assertNotNull(response);
        assertEquals(HttpStatus.OK.getCode(), response.getStatus().getCode());

        DepartmentDto departmentWithTeacher = response.getBody().orElse(null);
        assertNotNull(departmentWithTeacher);
        assertNotNull(departmentWithTeacher.getId());
        assertEquals(department.getName(), departmentWithTeacher.getName());
        assertEquals(teacher.getName(), departmentWithTeacher.getHeadOfDepartment().getName());
    }

    @Test
    void setTeacherToDepartmentShouldThrowEntityNotFoundExceptionTest() {
        long wrongDepartmentId = 999L;
        long wrongTeacherId = 999L;

        HttpClientResponseException ex = assertThrows(HttpClientResponseException.class, () -> {
            client.toBlocking().exchange(
                    HttpRequest.POST(departmentsPath + "/" + wrongDepartmentId + "/teacher/" + wrongTeacherId, null),
                    ErrorResponse.class
            );
        });
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());

        HttpResponse<?> response = ex.getResponse();
        Optional<ErrorResponse> body = response.getBody(ErrorResponse.class);
        assertTrue(body.isPresent());
        assertNotNull(body.get().getTimestamp());
        assertEquals("404", body.get().getStatus());
        assertEquals("EntityNotFoundException", body.get().getError());
        assertEquals("Department with ID " + wrongDepartmentId + " not found", body.get().getMessage());
        assertEquals(departmentsPath + "/" + wrongDepartmentId + "/teacher/" + wrongTeacherId, body.get().getPath());
    }

}
