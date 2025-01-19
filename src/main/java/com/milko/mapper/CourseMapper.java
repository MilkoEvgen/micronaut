package com.milko.mapper;

import com.milko.dto.CourseDto;
import com.milko.dto.records.StudentCoursesView;
import com.milko.model.Course;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "jsr330")
public interface CourseMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "teacherId", ignore = true)
    void updateFromDto(CourseDto dto, @MappingTarget Course course);

    @Mapping(target = "teacherId", ignore = true)
    Course toCourse(CourseDto dto);

    @Mapping(target = "teacher", ignore = true)
    CourseDto toCourseDto(Course course);

    @Mapping(target = "id", source = "courseId")
    @Mapping(target = "title", source = "courseTitle")
    @Mapping(target = "teacherId", source = "courseTeacherId")
    Course toCourse(StudentCoursesView coursesView);
}

