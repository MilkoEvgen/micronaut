package com.milko.repository;

import com.milko.model.Department;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.r2dbc.annotation.R2dbcRepository;
import io.micronaut.data.repository.reactive.ReactorCrudRepository;
import lombok.NonNull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@R2dbcRepository(dialect = Dialect.POSTGRES)
public interface DepartmentRepository extends ReactorCrudRepository<Department, Long> {

    @Query("""
                SELECT *
                FROM departments d
                WHERE d.head_of_department_id = :headOfDepartmentId
            """)
    Mono<Department> findByHeadOfDepartmentId(@NonNull Long headOfDepartmentId);

    @Query("""
                SELECT DISTINCT d.id, d.name, d.head_of_department_id
                FROM departments d
                WHERE d.head_of_department_id IN (:headOfDepartmentIds)
            """)
    Flux<Department> findAllByHeadOfDepartmentIds(@NonNull List<Long> headOfDepartmentIds);

}
