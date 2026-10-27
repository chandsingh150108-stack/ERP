package com.studenterp.controller;

import com.studenterp.entity.Scholarship;
import com.studenterp.repository.ScholarshipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/scholarships")
@RequiredArgsConstructor
public class ScholarshipController {

    private final ScholarshipRepository scholarshipRepository;

    @GetMapping
    public ResponseEntity<List<Scholarship>> findAll() {
        return ResponseEntity.ok(scholarshipRepository.findAll());
    }

    @GetMapping("/active")
    public ResponseEntity<List<Scholarship>> findActive() {
        return ResponseEntity.ok(scholarshipRepository.findByActiveTrue());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Scholarship> create(@RequestBody Scholarship scholarship) {
        return ResponseEntity.ok(scholarshipRepository.save(scholarship));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        scholarshipRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
