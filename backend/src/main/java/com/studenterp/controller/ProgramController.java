package com.studenterp.controller;

import com.studenterp.entity.Program;
import com.studenterp.service.ProgramService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/programs")
@RequiredArgsConstructor
public class ProgramController {

    private final ProgramService programService;

    @GetMapping
    public ResponseEntity<List<Program>> findAll() {
        return ResponseEntity.ok(programService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Program> findById(@PathVariable Long id) {
        return ResponseEntity.ok(programService.findById(id));
    }

    @GetMapping("/department/{departmentId}")
    public ResponseEntity<List<Program>> findByDepartment(@PathVariable Long departmentId) {
        return ResponseEntity.ok(programService.findByDepartment(departmentId));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Program> create(@RequestBody Program program) {
        return ResponseEntity.ok(programService.create(program));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Program> update(@PathVariable Long id, @RequestBody Program program) {
        return ResponseEntity.ok(programService.update(id, program));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        programService.delete(id);
        return ResponseEntity.ok().build();
    }
}
