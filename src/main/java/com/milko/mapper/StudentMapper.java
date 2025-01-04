package com.milko.mapper;

import com.milko.dto.StudentDto;
import com.milko.model.Student;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "jsr330", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface StudentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "courses", ignore = true)
    void updateFromDto(StudentDto dto, @MappingTarget Student student);

    Student toStudent(StudentDto dto);

    @Mapping(target = "courses", ignore = true)
    StudentDto toStudentDto(Student student);
}

