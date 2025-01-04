package com.milko.integration;

import com.milko.model.Student;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@MicronautTest(environments = "test")
public class StudentControllerTest{
    @Inject
    @Client("/")
    HttpClient client;

    @Test
    void createTest() {
        Student student = Student.builder()
                .name("John")
                .email("mail")
                .build();
        HttpRequest<Student> request = HttpRequest.POST("/api/v1/students", student);
        HttpResponse<Student> response = client.toBlocking().exchange(request, Student.class);

        assertEquals(201, response.getStatus().getCode());
        assertEquals("John", response.body().getName());
    }
}
