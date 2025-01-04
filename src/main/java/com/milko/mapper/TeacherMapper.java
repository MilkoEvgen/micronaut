package com.milko.mapper;

import com.milko.dto.TeacherDto;
import com.milko.model.Teacher;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "jsr330")
public interface TeacherMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "department", ignore = true)
    @Mapping(target = "courses", ignore = true)
    void updateFromDto(TeacherDto dto, @MappingTarget Teacher teacher);

    Teacher toTeacher(TeacherDto dto);

    @Mapping(target = "department", ignore = true)
    @Mapping(target = "courses", ignore = true)
    TeacherDto toTeacherDto(Teacher teacher);
}

