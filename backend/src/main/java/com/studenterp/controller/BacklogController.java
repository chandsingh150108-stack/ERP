package com.studenterp.controller;

import com.studenterp.entity.Backlog;
import com.studenterp.repository.BacklogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/backlogs")
@RequiredArgsConstructor
public class BacklogController {

    private final BacklogRepository backlogRepository;

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<Backlog>> findByStudent(@PathVariable Long studentId) {
        return ResponseEntity.ok(backlogRepository.findByStudentId(studentId));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Backlog> create(@RequestBody Backlog backlog) {
        return ResponseEntity.ok(backlogRepository.save(backlog));
    }
}
