package com.studenterp.controller;

import com.studenterp.entity.Student;
import com.studenterp.entity.User;
import com.studenterp.entity.VaultItem;
import com.studenterp.exception.ResourceNotFoundException;
import com.studenterp.repository.StudentRepository;
import com.studenterp.repository.UserRepository;
import com.studenterp.service.VaultItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/vault/items")
@RequiredArgsConstructor
public class VaultItemController {

    private final VaultItemService vaultItemService;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('STUDENT')")
    public ResponseEntity<List<VaultItem>> findAll(
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) String category,
            Authentication authentication) {
        Long studentId = resolveStudentId(authentication);
        if (subjectId != null) {
            return ResponseEntity.ok(vaultItemService.findBySubject(studentId, subjectId));
        }
        if (category != null) {
            return ResponseEntity.ok(vaultItemService.findByCategory(studentId, category));
        }
        return ResponseEntity.ok(vaultItemService.findAllByStudent(studentId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STUDENT')")
    public ResponseEntity<VaultItem> findById(@PathVariable Long id, Authentication authentication) {
        Long studentId = resolveStudentId(authentication);
        return ResponseEntity.ok(vaultItemService.findById(id, studentId));
    }

    @GetMapping("/{id}/download")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STUDENT')")
    public ResponseEntity<byte[]> downloadFile(@PathVariable Long id, Authentication authentication) throws IOException {
        Long studentId = resolveStudentId(authentication);
        byte[] data = vaultItemService.downloadFile(id, studentId);
        String contentType = vaultItemService.getFileContentType(id, studentId);
        String fileName = vaultItemService.getFileName(id, studentId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(data);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('STUDENT')")
    public ResponseEntity<VaultItem> create(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "category", defaultValue = "other") String category,
            @RequestParam(value = "subjectId", required = false) Long subjectId,
            Authentication authentication) throws IOException {
        Long studentId = resolveStudentId(authentication);
        return ResponseEntity.ok(vaultItemService.create(file, title, description, category, subjectId, studentId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STUDENT')")
    public ResponseEntity<VaultItem> update(
            @PathVariable Long id,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "subjectId", required = false) Long subjectId,
            Authentication authentication) {
        Long studentId = resolveStudentId(authentication);
        return ResponseEntity.ok(vaultItemService.update(id, title, description, category, subjectId, studentId));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STUDENT')")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) {
        Long studentId = resolveStudentId(authentication);
        vaultItemService.delete(id, studentId);
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
