package com.milko.mapper;

import com.milko.dto.DepartmentDto;
import com.milko.model.Department;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "jsr330")
public interface DepartmentMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "headOfDepartmentId", ignore = true)
    void updateFromDto(DepartmentDto dto, @MappingTarget Department department);

    @Mapping(target = "headOfDepartmentId", ignore = true)
    Department toDepartment(DepartmentDto dto);

    @Mapping(target = "headOfDepartment", ignore = true)
    DepartmentDto toDepartmentDto(Department department);
}

