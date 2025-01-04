package com.milko.rest;

import com.milko.dto.CourseDto;
import com.milko.dto.StudentDto;
import com.milko.service.StudentService;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Patch;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Controller("/api/v1/students")
@RequiredArgsConstructor
public class StudentController {
    private final StudentService service;

    @Post
    public Mono<HttpResponse<StudentDto>> create(@Body StudentDto dto){
        return service.create(dto)
                .map(student -> HttpResponse.status(HttpStatus.CREATED).body(student));
    }

    @Get
    public Flux<StudentDto> getAll(){
        return service.findAll();
    }

    @Get("{id}")
    public Mono<StudentDto> getById(@PathVariable Long id){
        return service.findById(id);
    }

    @Get("{id}/courses")
    public Flux<CourseDto> getAllCoursesByStudentId(@PathVariable Long id){
        return service.findAllCoursesByStudentId(id);
    }

    @Patch("{id}")
    public Mono<StudentDto> update(@PathVariable Long id, @Body StudentDto dto){
        dto.setId(id);
        return service.update(dto);
    }

    @Delete("{id}")
    public Mono<HttpResponse<Void>> deleteById(@PathVariable Long id){
        return service.deleteById(id).then(Mono.just(HttpResponse.noContent()));
    }

    @Post("{studentId}/courses/{courseId}")
    public Mono<StudentDto> addCourseToStudent(@PathVariable Long studentId, @PathVariable Long courseId){
        return service.addCourseToStudent(studentId, courseId);
    }
}
