package com.milko.integration;

import com.milko.dto.CourseDto;
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
public class CourseControllerTest {
    private final DatabaseCleaner cleaner;

    @Inject
    @Client("/")
    private ReactorHttpClient client;

    private final CourseDto course = CourseDto.builder()
            .title("title")
            .build();

    private final CourseDto courseToUpdate = CourseDto.builder()
            .title("updated title")
            .build();

    private final TeacherDto teacher = TeacherDto.builder()
            .name("name")
            .build();

    private final String coursesPath = "/api/v1/courses";
    private final String teachersPath = "/api/v1/teachers";

    @BeforeEach
    public void cleanDataBase() {
        cleaner.clearTables().block();
    }

    @Test
    void createTest() {
        HttpResponse<CourseDto> response = client.exchange(
                HttpRequest.POST(coursesPath, course),
                CourseDto.class
        ).block();

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED.getCode(), response.getStatus().getCode());

        CourseDto createdCourse = response.getBody().orElse(null);
        assertNotNull(createdCourse);
        assertNotNull(createdCourse.getId());
        assertEquals(course.getTitle(), createdCourse.getTitle());
    }

    @Test
    void createShouldReturnDataIntegrityViolationExceptionTest() {
        client.exchange(HttpRequest.POST(coursesPath, course), CourseDto.class).block();

        HttpClientResponseException ex = assertThrows(HttpClientResponseException.class, () -> {
            client.toBlocking().exchange(
                    HttpRequest.POST(coursesPath, course),
                    ErrorResponse.class
            );
        });
        assertEquals(HttpStatus.CONFLICT, ex.getStatus());

        HttpResponse<?> response = ex.getResponse();
        Optional<ErrorResponse> body = response.getBody(ErrorResponse.class);
        assertTrue(body.isPresent());
        assertNotNull(body.get().getTimestamp());
        assertEquals("409", body.get().getStatus());
        assertEquals("R2dbcDataIntegrityViolationException", body.get().getError());
        assertEquals(coursesPath, body.get().getPath());
    }

    @Test
    void getAllTest() {
        client.exchange(HttpRequest.POST(coursesPath, course), CourseDto.class).block();

        HttpResponse<List<CourseDto>> response = client.exchange(
                HttpRequest.GET(coursesPath),
                Argument.listOf(CourseDto.class)
        ).block();

        assertNotNull(response);
        assertEquals(HttpStatus.OK.getCode(), response.getStatus().getCode());

        List<CourseDto> courses = response.getBody().orElse(Collections.emptyList());
        assertNotNull(courses);
        assertFalse(courses.isEmpty());
        CourseDto fetchedCourse = courses.getFirst();
        assertEquals(course.getTitle(), fetchedCourse.getTitle());
    }

    @Test
    void getAllShouldReturnEmptyListTest() {
        HttpResponse<List<CourseDto>> response = client.exchange(
                HttpRequest.GET(coursesPath),
                Argument.listOf(CourseDto.class)
        ).block();

        assertNotNull(response);
        assertEquals(HttpStatus.OK.getCode(), response.getStatus().getCode());

        List<CourseDto> courses = response.getBody().orElse(Collections.emptyList());
        assertNotNull(courses);
        assertTrue(courses.isEmpty());
    }

    @Test
    void getByIdTest() {
        HttpResponse<CourseDto> created = client.exchange(
                HttpRequest.POST(coursesPath, course),
                CourseDto.class
        ).block();

        assertNotNull(created);
        Long courseId = created.body().getId();

        HttpResponse<CourseDto> response = client.exchange(
                HttpRequest.GET(coursesPath + "/" + courseId),
                CourseDto.class
        ).block();

        assertNotNull(response);
        assertEquals(HttpStatus.OK.getCode(), response.getStatus().getCode());

        CourseDto fetchedCourse = response.getBody().orElse(null);
        assertNotNull(fetchedCourse);
        assertNotNull(fetchedCourse.getId());
        assertEquals(course.getTitle(), fetchedCourse.getTitle());
    }

    @Test
    void getByIdShouldThrowEntityNotFoundExceptionTest() {
        long wrongId = 999L;

        HttpClientResponseException ex = assertThrows(HttpClientResponseException.class, () -> {
            client.toBlocking().exchange(
                    HttpRequest.GET(coursesPath + "/" + wrongId),
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
        assertEquals("Course with ID " + wrongId + " not found", body.get().getMessage());
        assertEquals(coursesPath + "/" + wrongId, body.get().getPath());
    }

    @Test
    void updateTest() {
        HttpResponse<CourseDto> created = client.exchange(
                HttpRequest.POST(coursesPath, course),
                CourseDto.class
        ).block();

        assertNotNull(created);
        Long courseId = created.body().getId();

        HttpResponse<CourseDto> response = client.exchange(
                HttpRequest.PATCH(coursesPath + "/" + courseId, courseToUpdate),
                CourseDto.class
        ).block();

        assertNotNull(response);
        assertEquals(HttpStatus.OK.getCode(), response.getStatus().getCode());

        CourseDto updatedCourse = response.body();
        assertNotNull(updatedCourse);
        assertEquals(courseToUpdate.getTitle(), updatedCourse.getTitle());
    }

    @Test
    void updateShouldThrowEntityNotFoundExceptionTest() {
        long wrongId = 999L;

        HttpClientResponseException ex = assertThrows(HttpClientResponseException.class, () -> {
            client.toBlocking().exchange(
                    HttpRequest.PATCH(coursesPath + "/" + wrongId, courseToUpdate),
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
        assertEquals("Course with ID " + wrongId + " not found", body.get().getMessage());
        assertEquals(coursesPath + "/" + wrongId, body.get().getPath());
    }

    @Test
    void deleteTest() {
        HttpResponse<CourseDto> created = client.exchange(
                HttpRequest.POST(coursesPath, course),
                CourseDto.class
        ).block();

        assertNotNull(created);
        Long courseId = created.getBody().orElseThrow().getId();

        HttpResponse<Void> response = client.exchange(
                HttpRequest.DELETE(coursesPath + "/" + courseId),
                Void.class
        ).block();

        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT.getCode(), response.getStatus().getCode());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void deleteShouldReturnNoContentIfEntityNotExistsTest() {
        long wrongId = 999L;

        HttpResponse<Void> response = client.exchange(
                HttpRequest.DELETE(coursesPath + "/" + wrongId),
                Void.class
        ).block();

        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT.getCode(), response.getStatus().getCode());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void setTeacherToCourseTest() {
        HttpResponse<CourseDto> courseResponse = client.exchange(
                HttpRequest.POST(coursesPath, course),
                CourseDto.class
        ).block();
        assertNotNull(courseResponse);
        Long courseId = courseResponse.getBody().orElseThrow().getId();

        HttpResponse<TeacherDto> teacherResponse = client.exchange(
                HttpRequest.POST(teachersPath, teacher),
                TeacherDto.class
        ).block();
        assertNotNull(teacherResponse);
        Long teacherId = teacherResponse.getBody().orElseThrow().getId();

        HttpResponse<CourseDto> response = client.exchange(
                HttpRequest.POST(coursesPath + "/" + courseId + "/teacher/" + teacherId, null),
                CourseDto.class
        ).block();

        assertNotNull(response);
        assertEquals(HttpStatus.OK.getCode(), response.getStatus().getCode());

        CourseDto courseWithTeacher = response.getBody().orElse(null);
        assertNotNull(courseWithTeacher);
        assertEquals(course.getTitle(), courseWithTeacher.getTitle());
        assertEquals(teacher.getName(), courseWithTeacher.getTeacher().getName());
    }

    @Test
    void setTeacherToCourseShouldThrowEntityNotFoundExceptionTest() {
        long wrongCourseId = 999L;
        long wrongTeacherId = 888L;

        HttpClientResponseException ex = assertThrows(HttpClientResponseException.class, () -> {
            client.toBlocking().exchange(
                    HttpRequest.POST(coursesPath + "/" + wrongCourseId + "/teacher/" + wrongTeacherId, null),
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
    }


}
