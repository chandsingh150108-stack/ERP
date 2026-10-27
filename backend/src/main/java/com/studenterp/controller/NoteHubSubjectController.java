package com.studenterp.controller;

import com.studenterp.entity.NoteHubSubject;
import com.studenterp.exception.ResourceNotFoundException;
import com.studenterp.service.NoteHubSubjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notehub/subjects")
@RequiredArgsConstructor
public class NoteHubSubjectController {

    private final NoteHubSubjectService noteHubSubjectService;

    @GetMapping
    public ResponseEntity<List<NoteHubSubject>> findAll(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) String department) {
        return ResponseEntity.ok(noteHubSubjectService.findAll(year, courseId, department));
    }

    @GetMapping("/{id}")
    public ResponseEntity<NoteHubSubject> findById(@PathVariable Long id) {
        return ResponseEntity.ok(noteHubSubjectService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NoteHubSubject> create(@RequestBody NoteHubSubject subject) {
        return ResponseEntity.ok(noteHubSubjectService.create(subject));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NoteHubSubject> update(@PathVariable Long id, @RequestBody NoteHubSubject updated) {
        return ResponseEntity.ok(noteHubSubjectService.update(id, updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        noteHubSubjectService.delete(id);
        return ResponseEntity.ok().build();
    }
}