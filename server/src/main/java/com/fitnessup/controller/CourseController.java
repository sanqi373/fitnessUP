package com.fitnessup.controller;

import com.fitnessup.common.ApiResult;
import com.fitnessup.entity.Course;
import com.fitnessup.service.CourseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
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
        List<Course> courses = category == null || category.isBlank()
                ? courseService.getAllCourses() : courseService.getCoursesByCategory(category);
        return ResponseEntity.ok(ApiResult.success(Map.of("list", courses, "total", courses.size())));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResult<?>> getCourseById(@PathVariable Long id) {
        Optional<Course> course = courseService.getCourseById(id);
        if (course.isEmpty()) return ResponseEntity.status(404).body(ApiResult.error(404, "课程不存在"));
        return ResponseEntity.ok(ApiResult.success(course.get()));
    }

    @GetMapping("/{id}/comments")
    public ResponseEntity<ApiResult<?>> getComments(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResult.success(Map.of("list", List.of(), "total", 0)));
    }
}
