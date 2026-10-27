package com.studenterp.controller;

import com.studenterp.entity.Semester;
import com.studenterp.exception.ResourceNotFoundException;
import com.studenterp.repository.SemesterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/semesters")
@RequiredArgsConstructor
public class SemesterController {

    private final SemesterRepository semesterRepository;

    @GetMapping
    public ResponseEntity<List<Semester>> findAll() {
        return ResponseEntity.ok(semesterRepository.findAll());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Semester> create(@RequestBody Semester semester) {
        return ResponseEntity.ok(semesterRepository.save(semester));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Semester> update(@PathVariable Long id, @RequestBody Semester updated) {
        Semester semester = semesterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Semester", "id", id));
        semester.setSemesterNumber(updated.getSemesterNumber());
        semester.setName(updated.getName());
        semester.setActive(updated.getActive());
        return ResponseEntity.ok(semesterRepository.save(semester));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        semesterRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
