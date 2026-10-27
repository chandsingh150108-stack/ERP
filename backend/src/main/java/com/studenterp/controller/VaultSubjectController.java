package com.studenterp.controller;

import com.studenterp.entity.Student;
import com.studenterp.entity.User;
import com.studenterp.entity.VaultSubject;
import com.studenterp.exception.ResourceNotFoundException;
import com.studenterp.repository.StudentRepository;
import com.studenterp.repository.UserRepository;
import com.studenterp.service.VaultSubjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vault/subjects")
@RequiredArgsConstructor
public class VaultSubjectController {

    private final VaultSubjectService vaultSubjectService;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('STUDENT')")
    public ResponseEntity<List<VaultSubject>> findAll(
            @RequestParam(required = false) String year,
            Authentication authentication) {
        Long studentId = resolveStudentId(authentication);
        return ResponseEntity.ok(vaultSubjectService.findAllByStudent(studentId, year));
    }

    @PostMapping("/sync")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STUDENT')")
    public ResponseEntity<List<VaultSubject>> syncCurriculum(Authentication authentication) {
        Long studentId = resolveStudentId(authentication);
        return ResponseEntity.ok(vaultSubjectService.syncCourseCurriculum(studentId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STUDENT')")
    public ResponseEntity<VaultSubject> findById(@PathVariable Long id, Authentication authentication) {
        Long studentId = resolveStudentId(authentication);
        return ResponseEntity.ok(vaultSubjectService.findById(id, studentId));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('STUDENT')")
    public ResponseEntity<VaultSubject> create(@RequestBody VaultSubject subject, Authentication authentication) {
        Long studentId = resolveStudentId(authentication);
        return ResponseEntity.ok(vaultSubjectService.create(subject, studentId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STUDENT')")
    public ResponseEntity<VaultSubject> update(@PathVariable Long id, @RequestBody VaultSubject subject,
                                               Authentication authentication) {
        Long studentId = resolveStudentId(authentication);
        return ResponseEntity.ok(vaultSubjectService.update(id, subject, studentId));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STUDENT')")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) {
        Long studentId = resolveStudentId(authentication);
        vaultSubjectService.delete(id, studentId);
        return ResponseEntity.ok().build();
    }

    private Long resolveStudentId(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
        Student student = studentRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "user", username));
        return student.getId();
    }
}
