package com.milko.mapper;

import com.milko.dto.CourseDto;
import com.milko.model.Course;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "jsr330")
public interface CourseMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "teacher", ignore = true)
    @Mapping(target = "students", ignore = true)
    void updateFromDto(CourseDto dto, @MappingTarget Course course);

    Course toCourse(CourseDto dto);

    @Mapping(target = "teacher", ignore = true)
    @Mapping(target = "students", ignore = true)
    CourseDto toCourseDto(Course course);
}

