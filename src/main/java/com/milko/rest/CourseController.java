package com.milko.rest;

import com.milko.dto.CourseDto;
import com.milko.service.CourseService;
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


@Controller("/api/v1/courses")
@RequiredArgsConstructor
public class CourseController {
    private final CourseService service;

    @Post
    public Mono<HttpResponse<CourseDto>> create(@Body CourseDto dto) {
        return service.create(dto)
                .map(savedCourse -> HttpResponse.status(HttpStatus.CREATED).body(savedCourse));
    }

    @Get
    public Flux<CourseDto> getAll(){
        return service.findAll();
    }

    @Get("{id}")
    public Mono<CourseDto> getById(@PathVariable Long id){
        return service.findById(id);
    }

    @Patch("{id}")
    public Mono<CourseDto> update(@PathVariable Long id, @Body CourseDto dto){
        dto.setId(id);
        return service.update(dto);
    }

    @Delete("{id}")
    public Mono<HttpResponse<Void>> deleteById(@PathVariable Long id){
        return service.deleteById(id).then(Mono.just(HttpResponse.noContent()));
    }

    @Post("{courseId}/teacher/{teacherId}")
    public Mono<CourseDto> setTeacherToCourse(@PathVariable Long courseId, @PathVariable Long teacherId){
        return service.setTeacherToCourse(courseId, teacherId);
    }
}
