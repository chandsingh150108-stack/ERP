package com.studenterp.controller;

import com.studenterp.entity.Faculty;
import com.studenterp.entity.FacultyCourse;
import com.studenterp.service.FacultyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/faculty")
@RequiredArgsConstructor
public class FacultyController {

    private final FacultyService facultyService;

    @GetMapping
    public ResponseEntity<List<Faculty>> findAll() {
        return ResponseEntity.ok(facultyService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Faculty> findById(@PathVariable Long id) {
        return ResponseEntity.ok(facultyService.findById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Faculty>> search(@RequestParam String keyword) {
        return ResponseEntity.ok(facultyService.search(keyword));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Faculty> create(@RequestBody Faculty faculty) {
        return ResponseEntity.ok(facultyService.create(faculty));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Faculty> update(@PathVariable Long id, @RequestBody Faculty faculty) {
        return ResponseEntity.ok(facultyService.update(id, faculty));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        facultyService.delete(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/courses")
    public ResponseEntity<List<FacultyCourse>> getCourses(@PathVariable Long id) {
        return ResponseEntity.ok(facultyService.getFacultyCourses(id));
    }

    @PostMapping("/{id}/courses")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FacultyCourse> assignCourse(
            @PathVariable Long id,
            @RequestParam Long courseId,
            @RequestParam Long sectionId,
            @RequestParam Long academicYearId) {
        return ResponseEntity.ok(facultyService.assignCourse(id, courseId, sectionId, academicYearId));
    }
}
