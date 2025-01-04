package com.milko.mapper;

import com.milko.dto.DepartmentDto;
import com.milko.model.Department;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "jsr330")
public interface DepartmentMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "headOfDepartment", ignore = true)
    void updateFromDto(DepartmentDto dto, @MappingTarget Department department);

    Department toDepartment(DepartmentDto dto);

    @Mapping(target = "headOfDepartment", ignore = true)
    DepartmentDto toDepartmentDto(Department department);
}

