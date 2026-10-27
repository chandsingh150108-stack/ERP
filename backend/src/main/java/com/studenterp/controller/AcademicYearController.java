package com.studenterp.controller;

import com.studenterp.entity.AcademicYear;
import com.studenterp.exception.ResourceNotFoundException;
import com.studenterp.repository.AcademicYearRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/academic-years")
@RequiredArgsConstructor
public class AcademicYearController {

    private final AcademicYearRepository academicYearRepository;

    @GetMapping
    public ResponseEntity<List<AcademicYear>> findAll() {
        return ResponseEntity.ok(academicYearRepository.findAll());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AcademicYear> create(@RequestBody AcademicYear year) {
        return ResponseEntity.ok(academicYearRepository.save(year));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AcademicYear> update(@PathVariable Long id, @RequestBody AcademicYear updated) {
        AcademicYear year = academicYearRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AcademicYear", "id", id));
        year.setYearName(updated.getYearName());
        year.setStartDate(updated.getStartDate());
        year.setEndDate(updated.getEndDate());
        year.setCurrent(updated.getCurrent());
        return ResponseEntity.ok(academicYearRepository.save(year));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        academicYearRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
