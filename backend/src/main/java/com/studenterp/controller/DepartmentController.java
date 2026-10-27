package com.studenterp.controller;

import com.studenterp.entity.Department;
import com.studenterp.exception.ResourceNotFoundException;
import com.studenterp.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentRepository departmentRepository;

    @GetMapping
    public ResponseEntity<List<Department>> findAll() {
        return ResponseEntity.ok(departmentRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Department> findById(@PathVariable Long id) {
        return ResponseEntity.ok(departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Department> create(@RequestBody Department dept) {
        return ResponseEntity.ok(departmentRepository.save(dept));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Department> update(@PathVariable Long id, @RequestBody Department updated) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));
        dept.setCode(updated.getCode());
        dept.setName(updated.getName());
        dept.setDescription(updated.getDescription());
        dept.setActive(updated.getActive());
        return ResponseEntity.ok(departmentRepository.save(dept));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        departmentRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
