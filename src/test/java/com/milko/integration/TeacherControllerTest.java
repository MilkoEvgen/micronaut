package com.milko.integration;

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
public class TeacherControllerTest {
    private final DatabaseCleaner cleaner;

    @Inject
    @Client("/")
    private ReactorHttpClient client;

    private final TeacherDto teacher = TeacherDto.builder()
            .name("name")
            .build();

    private final TeacherDto teacherToUpdate = TeacherDto.builder()
            .name("updated name")
            .build();

    private final String teachersPath = "/api/v1/teachers";

    @BeforeEach
    public void cleanDataBase() {
        cleaner.clearTables().block();
    }

    @Test
    void createTest() {
        HttpResponse<TeacherDto> response = client.exchange(
                HttpRequest.POST(teachersPath, teacher),
                TeacherDto.class
        ).block();

        assertNotNull(response);
        assertEquals(201, response.getStatus().getCode());

        TeacherDto createdTeacher = response.getBody().orElse(null);
        assertNotNull(createdTeacher);
        assertNotNull(createdTeacher.getId());
        assertEquals(teacher.getName(), createdTeacher.getName());
    }

    @Test
    void getAllTest() {
        client.exchange(HttpRequest.POST(teachersPath, teacher), TeacherDto.class).block();

        HttpResponse<List<TeacherDto>> response = client.exchange(
                HttpRequest.GET(teachersPath),
                Argument.listOf(TeacherDto.class)
        ).block();

        assertNotNull(response);
        assertEquals(200, response.getStatus().getCode());

        List<TeacherDto> teachers = response.getBody().orElse(Collections.emptyList());
        assertNotNull(teachers);
        assertFalse(teachers.isEmpty());
        TeacherDto fetchedTeacher = teachers.getFirst();
        assertNotNull(fetchedTeacher.getId());
        assertEquals(teacher.getName(), fetchedTeacher.getName());
    }

    @Test
    void getAllShouldReturnEmptyListTest() {
        HttpResponse<List<TeacherDto>> response = client.exchange(
                HttpRequest.GET(teachersPath),
                Argument.listOf(TeacherDto.class)
        ).block();

        assertNotNull(response);
        assertEquals(200, response.getStatus().getCode());

        List<TeacherDto> teachers = response.getBody().orElse(Collections.emptyList());
        assertNotNull(teachers);
        assertTrue(teachers.isEmpty());
    }

    @Test
    void getByIdTest() {
        HttpResponse<TeacherDto> created = client.exchange(
                HttpRequest.POST(teachersPath, teacher),
                TeacherDto.class
        ).block();

        assertNotNull(created);
        Long teacherId = created.body().getId();

        HttpResponse<TeacherDto> response = client.exchange(
                HttpRequest.GET(teachersPath + "/" + teacherId),
                TeacherDto.class
        ).block();

        assertNotNull(response);
        assertEquals(200, response.getStatus().getCode());

        TeacherDto fetchedTeacher = response.getBody().orElse(null);
        assertNotNull(fetchedTeacher);
        assertNotNull(fetchedTeacher.getId());
        assertEquals(teacher.getName(), fetchedTeacher.getName());
    }

    @Test
    void getByIdShouldThrowEntityNotFoundExceptionTest() {
        long wrongId = 999L;

        HttpClientResponseException ex = assertThrows(HttpClientResponseException.class, () -> {
            client.toBlocking().exchange(
                    HttpRequest.GET(teachersPath + "/" + wrongId),
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
        assertEquals("Teacher with ID " + wrongId + " not found", body.get().getMessage());
        assertEquals(teachersPath + "/" + wrongId, body.get().getPath());
    }

    @Test
    void updateTest() {
        HttpResponse<TeacherDto> created = client.exchange(
                HttpRequest.POST(teachersPath, teacher),
                TeacherDto.class
        ).block();

        assertNotNull(created);
        Long teacherId = created.body().getId();

        HttpResponse<TeacherDto> response = client.exchange(
                HttpRequest.PATCH(teachersPath + "/" + teacherId, teacherToUpdate),
                TeacherDto.class
        ).block();

        assertNotNull(response);
        assertEquals(200, response.getStatus().getCode());

        TeacherDto updatedTeacher = response.getBody().orElse(null);
        assertNotNull(updatedTeacher);
        assertNotNull(updatedTeacher.getId());
        assertEquals(teacherToUpdate.getName(), updatedTeacher.getName());
    }

    @Test
    void updateShouldThrowEntityNotFoundExceptionTest() {
        long wrongId = 999L;

        HttpClientResponseException ex = assertThrows(HttpClientResponseException.class, () -> {
            client.toBlocking().exchange(
                    HttpRequest.PATCH(teachersPath + "/" + wrongId, teacherToUpdate),
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
        assertEquals("Teacher with ID " + wrongId + " not found", body.get().getMessage());
        assertEquals(teachersPath + "/" + wrongId, body.get().getPath());
    }

    @Test
    void deleteTest() {
        HttpResponse<TeacherDto> created = client.exchange(
                HttpRequest.POST(teachersPath, teacher),
                TeacherDto.class
        ).block();

        assertNotNull(created);
        Long teacherId = created.body().getId();

        HttpResponse<Void> response = client.exchange(
                HttpRequest.DELETE(teachersPath + "/" + teacherId),
                Void.class
        ).block();

        assertNotNull(response);
        assertEquals(204, response.getStatus().getCode());

        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void deleteShouldReturnNoContentIfEntityNotExistsTest() {
        long wrongId = 999L;

        HttpResponse<Void> response = client.exchange(
                HttpRequest.DELETE(teachersPath + "/" + wrongId),
                Void.class
        ).block();

        assertNotNull(response);
        assertEquals(204, response.getStatus().getCode());

        assertTrue(response.getBody().isEmpty());
    }
}
