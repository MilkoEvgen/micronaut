package com.milko.rest;

import com.milko.dto.DepartmentDto;
import com.milko.service.DepartmentService;
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


@Controller("/api/v1/departments")
@RequiredArgsConstructor
public class DepartmentController {
    private final DepartmentService service;

    @Post
    public Mono<HttpResponse<DepartmentDto>> create(@Body DepartmentDto dto){
        return service.create(dto)
                .map(department -> HttpResponse.status(HttpStatus.CREATED).body(department));
    }

    @Get
    public Flux<DepartmentDto> getAll(){
        return service.findAll();
    }

    @Get("{id}")
    public Mono<DepartmentDto> getById(@PathVariable Long id){
        return service.findById(id);
    }

    @Patch("{id}")
    public Mono<DepartmentDto> update(@PathVariable Long id, @Body DepartmentDto dto){
        dto.setId(id);
        return service.update(dto);
    }

    @Delete("{id}")
    public Mono<HttpResponse<Void>> deleteById(@PathVariable Long id){
        return service.deleteById(id).then(Mono.just(HttpResponse.noContent()));
    }

    @Post("{departmentId}/teacher/{teacherId}")
    public Mono<DepartmentDto> setTeacherToDepartment(@PathVariable Long departmentId, @PathVariable Long teacherId){
        return service.setTeacherToDepartment(departmentId, teacherId);
    }
}
