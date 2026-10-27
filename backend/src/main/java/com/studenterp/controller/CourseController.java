package com.studenterp.controller;

import com.studenterp.entity.Course;
import com.studenterp.exception.ResourceNotFoundException;
import com.studenterp.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseRepository courseRepository;

    @GetMapping
    public ResponseEntity<List<Course>> findAll() {
        return ResponseEntity.ok(courseRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Course> findById(@PathVariable Long id) {
        return ResponseEntity.ok(courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Course> create(@RequestBody Course course) {
        return ResponseEntity.ok(courseRepository.save(course));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Course> update(@PathVariable Long id, @RequestBody Course updated) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", id));
        course.setCode(updated.getCode());
        course.setName(updated.getName());
        course.setDescription(updated.getDescription());
        course.setCredits(updated.getCredits());
        course.setCourseType(updated.getCourseType());
        course.setDepartment(updated.getDepartment());
        course.setLectureHours(updated.getLectureHours());
        course.setTutorialHours(updated.getTutorialHours());
        course.setPracticalHours(updated.getPracticalHours());
        course.setStudyYear(updated.getStudyYear());
        return ResponseEntity.ok(courseRepository.save(course));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        courseRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
