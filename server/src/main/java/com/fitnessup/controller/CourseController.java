package com.fitnessup.controller;

import com.fitnessup.common.ApiResult;
import com.fitnessup.entity.Course;
import com.fitnessup.service.CourseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping
    public ResponseEntity<ApiResult<?>> getCourses(@RequestParam(required = false) String category) {
        List<Course> courses;
        if (category != null && !category.isEmpty()) {
            courses = courseService.getCoursesByCategory(category);
        } else {
            courses = courseService.getAllCourses();
        }
        return ResponseEntity.ok(ApiResult.success(courses));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResult<?>> getCourseById(@PathVariable Long id) {
        Optional<Course> course = courseService.getCourseById(id);
        if (course.isEmpty()) {
            return ResponseEntity.status(404).body(ApiResult.error(404, "课程不存在"));
        }
        return ResponseEntity.ok(ApiResult.success(course.get()));
    }
}
