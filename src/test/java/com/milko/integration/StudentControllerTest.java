package com.milko.integration;

import com.milko.dto.CourseDto;
import com.milko.dto.StudentDto;
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
public class StudentControllerTest {
    private final DatabaseCleaner cleaner;

    @Inject
    @Client("/")
    private ReactorHttpClient client;

    private final StudentDto student = StudentDto.builder()
            .name("name")
            .email("email")
            .build();

    private final StudentDto studentToUpdate = StudentDto.builder()
            .name("updated name")
            .email("updated email")
            .build();

    private final CourseDto course = CourseDto.builder()
            .title("title")
            .build();

    private final String studentsPath = "/api/v1/students";
    private final String coursesPath = "/api/v1/courses";

    @BeforeEach
    public void cleanDataBase() {
        cleaner.clearTables().block();
    }

    @Test
    void createTest() {
        HttpResponse<StudentDto> response = client.exchange(
                HttpRequest.POST(studentsPath, student),
                StudentDto.class
        ).block();

        assertNotNull(response);
        assertEquals(response.getStatus().getCode(), 201);

        StudentDto createdStudent = response.getBody().orElse(null);
        assertNotNull(createdStudent);
        assertNotNull(createdStudent.getId());
        assertEquals(student.getName(), createdStudent.getName());
        assertEquals(student.getEmail(), createdStudent.getEmail());
    }

    @Test
    void createShouldReturnDataIntegrityViolationExceptionTest() {
        client.exchange(HttpRequest.POST(studentsPath, student), StudentDto.class).block();

        HttpClientResponseException ex = assertThrows(HttpClientResponseException.class, () -> {
            client.toBlocking().exchange(
                    HttpRequest.POST(studentsPath, student),
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
        assertEquals(studentsPath, body.get().getPath());
    }

    @Test
    void getAllTest() {
        client.exchange(HttpRequest.POST(studentsPath, student), StudentDto.class).block();

        HttpResponse<List<StudentDto>> response = client.exchange(
                HttpRequest.GET(studentsPath),
                Argument.listOf(StudentDto.class)
        ).block();

        assertNotNull(response);
        assertEquals(200, response.getStatus().getCode());

        List<StudentDto> students = response.getBody().orElse(Collections.emptyList());
        assertNotNull(students);
        assertFalse(students.isEmpty());
        StudentDto fetchedStudent = students.getFirst();
        assertNotNull(fetchedStudent.getId());
        assertEquals(student.getName(), fetchedStudent.getName());
        assertEquals(student.getEmail(), fetchedStudent.getEmail());
    }


    @Test
    void getAllTestShouldReturnEmptyList() {
        HttpResponse<List<StudentDto>> response = client.exchange(
                HttpRequest.GET(studentsPath),
                Argument.listOf(StudentDto.class)
        ).block();

        assertNotNull(response);
        assertEquals(200, response.getStatus().getCode());

        List<StudentDto> students = response.getBody().orElse(Collections.emptyList());
        assertNotNull(students);
        assertTrue(students.isEmpty());
    }

    @Test
    void getByIdTest() {
        HttpResponse<StudentDto> created = client.exchange(
                HttpRequest.POST(studentsPath, student),
                StudentDto.class
        ).block();

        assertNotNull(created);
        Long studentId = created.body().getId();

        HttpResponse<StudentDto> response = client.exchange(
                HttpRequest.GET(studentsPath + "/" + studentId),
                StudentDto.class
        ).block();

        assertNotNull(response);
        assertEquals(200, response.getStatus().getCode());

        StudentDto fetchedStudent = response.getBody().orElse(null);
        assertNotNull(fetchedStudent);
        assertNotNull(fetchedStudent.getId());
        assertEquals(student.getName(), fetchedStudent.getName());
        assertEquals(student.getEmail(), fetchedStudent.getEmail());
    }

    @Test
    void getByIdShouldThrowEntityNotFoundExceptionTest() {
        long wrongId = 999L;

        HttpClientResponseException ex = assertThrows(HttpClientResponseException.class, () -> {
            client.toBlocking().exchange(
                    HttpRequest.GET(studentsPath + "/" + wrongId),
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
        assertEquals("Student with ID " + wrongId + " not found", body.get().getMessage());
        assertEquals(studentsPath + "/" + wrongId, body.get().getPath());
    }

    @Test
    void getAllCoursesByStudentIdTest() {
        HttpResponse<StudentDto> studentResponse = client.exchange(
                HttpRequest.POST(studentsPath, student),
                StudentDto.class
        ).block();
        assertNotNull(studentResponse);
        Long studentId = studentResponse.body().getId();

        HttpResponse<CourseDto> courseResponse = client.exchange(
                HttpRequest.POST(coursesPath, course),
                CourseDto.class
        ).block();
        assertNotNull(courseResponse);
        Long courseId = courseResponse.body().getId();

        client.exchange(
                HttpRequest.POST(studentsPath + "/" + studentId + "/courses/" + courseId, null),
                StudentDto.class
        ).block();

        HttpResponse<List<CourseDto>> response = client.exchange(
                HttpRequest.GET(studentsPath + "/" + studentId + "/courses"),
                Argument.listOf(CourseDto.class)
        ).block();

        assertNotNull(response);
        assertEquals(200, response.getStatus().getCode());

        List<CourseDto> courses = response.getBody().orElse(Collections.emptyList());
        assertNotNull(courses);
        assertFalse(courses.isEmpty());
        CourseDto fetchedCourse = courses.getFirst();
        assertNotNull(fetchedCourse.getId());
        assertEquals(course.getTitle(), fetchedCourse.getTitle());
    }

    @Test
    void getAllCoursesByStudentIdShouldReturnEmptyListTest() {
        HttpResponse<StudentDto> studentResponse = client.exchange(
                HttpRequest.POST(studentsPath, student),
                StudentDto.class
        ).block();
        assertNotNull(studentResponse);
        Long studentId = studentResponse.body().getId();

        HttpResponse<List<CourseDto>> response = client.exchange(
                HttpRequest.GET(studentsPath + "/" + studentId + "/courses"),
                Argument.listOf(CourseDto.class)
        ).block();

        assertNotNull(response);
        assertEquals(200, response.getStatus().getCode());

        List<CourseDto> courses = response.getBody().orElse(Collections.emptyList());
        assertNotNull(courses);
        assertTrue(courses.isEmpty());
    }

    @Test
    void updateTest() {
        HttpResponse<StudentDto> created = client.exchange(
                HttpRequest.POST(studentsPath, student),
                StudentDto.class
        ).block();

        assertNotNull(created);
        Long studentId = created.body().getId();

        HttpResponse<StudentDto> response = client.exchange(
                HttpRequest.PATCH(studentsPath + "/" + studentId, studentToUpdate),
                StudentDto.class
        ).block();

        assertNotNull(response);
        assertEquals(200, response.getStatus().getCode());

        StudentDto updatedStudent = response.getBody().orElse(null);
        assertNotNull(updatedStudent);
        assertNotNull(updatedStudent.getId());
        assertEquals(studentToUpdate.getName(), updatedStudent.getName());
        assertEquals(studentToUpdate.getEmail(), updatedStudent.getEmail());
    }

    @Test
    void updateShouldThrowEntityNotFoundExceptionTest() {
        long wrongId = 999L;

        HttpClientResponseException ex = assertThrows(HttpClientResponseException.class, () -> {
            client.toBlocking().exchange(
                    HttpRequest.PATCH(studentsPath + "/" + wrongId, studentToUpdate),
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
        assertEquals("Student with ID " + wrongId + " not found", body.get().getMessage());
        assertEquals(studentsPath + "/" + wrongId, body.get().getPath());
    }

    @Test
    void deleteTest() {
        HttpResponse<StudentDto> created = client.exchange(
                HttpRequest.POST(studentsPath, student),
                StudentDto.class
        ).block();

        assertNotNull(created);
        Long studentId = created.body().getId();

        HttpResponse<StudentDto> response = client.exchange(
                HttpRequest.DELETE(studentsPath + "/" + studentId),
                StudentDto.class
        ).block();

        assertNotNull(response);
        assertEquals(204, response.getStatus().getCode());

        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void deleteShouldReturnNoContentIfEntityNotExistsTest() {
        long wrongId = 999L;

        HttpResponse<StudentDto> response = client.exchange(
                HttpRequest.DELETE(studentsPath + "/" + wrongId),
                StudentDto.class
        ).block();

        assertNotNull(response);
        assertEquals(204, response.getStatus().getCode());

        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void addCourseToStudentTest() {
        HttpResponse<StudentDto> studentResponse = client.exchange(
                HttpRequest.POST(studentsPath, student),
                StudentDto.class
        ).block();
        assertNotNull(studentResponse);
        Long studentId = studentResponse.body().getId();

        HttpResponse<CourseDto> courseResponse = client.exchange(
                HttpRequest.POST(coursesPath, course),
                CourseDto.class
        ).block();
        assertNotNull(courseResponse);
        Long courseId = courseResponse.body().getId();

        HttpResponse<StudentDto> response = client.exchange(
                HttpRequest.POST(studentsPath + "/" + studentId + "/courses/" + courseId, null),
                StudentDto.class
        ).block();

        assertNotNull(response);
        assertEquals(200, response.getStatus().getCode());

        StudentDto studentWithCourse = response.getBody().orElse(null);
        assertNotNull(studentWithCourse);
        assertNotNull(studentWithCourse.getId());
        assertEquals(student.getName(), studentWithCourse.getName());
        assertEquals(student.getEmail(), studentWithCourse.getEmail());
        assertEquals(course.getTitle(), studentWithCourse.getCourses().getFirst().getTitle());
    }

    @Test
    void addCourseToStudentShouldThrowEntityNotFoundExceptionTest() {
        long wrongId = 999L;

        HttpClientResponseException ex = assertThrows(HttpClientResponseException.class, () -> {
            client.toBlocking().exchange(
                    HttpRequest.POST(studentsPath + "/" + wrongId + "/courses/" + wrongId, null),
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
        assertEquals("Student with ID " + wrongId + " not found", body.get().getMessage());
        assertEquals(studentsPath + "/" + wrongId + "/courses/" + wrongId, body.get().getPath());
    }
}
