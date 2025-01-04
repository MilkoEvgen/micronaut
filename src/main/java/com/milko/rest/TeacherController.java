package com.milko.rest;

import com.milko.dto.TeacherDto;
import com.milko.service.TeacherService;
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

@Controller("/api/v1/teachers")
@RequiredArgsConstructor
public class TeacherController {
    private final TeacherService service;

    @Post
    public Mono<HttpResponse<TeacherDto>> create(@Body TeacherDto dto){
        return service.create(dto)
                .map(teacher -> HttpResponse.status(HttpStatus.CREATED).body(teacher));
    }

    @Get
    public Flux<TeacherDto> getAll(){
        return service.findAll();
    }

    @Get("{id}")
    public Mono<TeacherDto> getById(@PathVariable Long id){
        return service.findById(id);
    }

    @Patch("{id}")
    public Mono<TeacherDto> update(@PathVariable Long id, @Body TeacherDto dto){
        dto.setId(id);
        return service.update(dto);
    }

    @Delete("{id}")
    public Mono<HttpResponse<Void>> deleteById(@PathVariable Long id){
        return service.deleteById(id).then(Mono.just(HttpResponse.noContent()));
    }

}
