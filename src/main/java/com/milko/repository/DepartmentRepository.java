package com.milko.repository;

import com.milko.model.Department;
import com.milko.model.Teacher;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.reactive.ReactorCrudRepository;
import jakarta.annotation.Nullable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Repository
public interface DepartmentRepository extends ReactorCrudRepository<Department, Long> {

    @Query("""
                SELECT d FROM Department d
                LEFT JOIN d.headOfDepartment h
                WHERE h.id = :headOfDepartmentId
            """)
    Mono<Department> findByHeadOfDepartmentId(Long headOfDepartmentId);

    @Query("""
                SELECT d FROM Department d
                LEFT JOIN d.headOfDepartment h
                WHERE h.id IN :headOfDepartmentIds
            """)
    Flux<Department> findAllByHeadOfDepartmentIds(List<Long> headOfDepartmentIds);

    @Query("""
                UPDATE Department d
                SET
                    d.name = COALESCE(:name, d.name)
                WHERE d.id = :departmentId
            """)
    Mono<Void> updateDepartment(Long departmentId, @Nullable String name);

    @Query("""
                UPDATE Department d
                SET d.headOfDepartment = :teacher
                WHERE d.id = :departmentId
            """)
    Mono<Void> setTeacherToDepartment(Long departmentId, Teacher teacher);
}
