package com.studenterp.controller;

import com.studenterp.entity.NoteHubNote;
import com.studenterp.entity.User;
import com.studenterp.exception.ResourceNotFoundException;
import com.studenterp.repository.UserRepository;
import com.studenterp.repository.StudentRepository;
import com.studenterp.service.NoteHubService;
import com.studenterp.service.NoteHubVoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notehub/notes")
@RequiredArgsConstructor
public class NoteHubController {

    private final NoteHubService noteHubService;
    private final NoteHubVoteService noteHubVoteService;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;

    @GetMapping
    public ResponseEntity<List<NoteHubNote>> findCourseNotes(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) String unit,
            @RequestParam(required = false) String department,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        Page<NoteHubNote> notesPage = noteHubService.findCourseNotes(year, courseId, subjectId, unit, department, page, size);
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(notesPage.getTotalElements()))
                .body(notesPage.getContent());
    }

    @GetMapping("/search")
    public ResponseEntity<List<NoteHubNote>> searchExtraNotes(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        Page<NoteHubNote> notesPage = noteHubService.searchExtraNotes(keyword, page, size);
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(notesPage.getTotalElements()))
                .body(notesPage.getContent());
    }

    @GetMapping("/{id}")
    public ResponseEntity<NoteHubNote> findById(@PathVariable Long id) {
        return ResponseEntity.ok(noteHubService.findById(id));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadFile(@PathVariable Long id) throws IOException {
        byte[] data = noteHubService.downloadFile(id);
        String contentType = noteHubService.getFileContentType(id);
        String fileName = noteHubService.getFileName(id);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(data);
    }

    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<NoteHubNote> create(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("noteType") String noteType,
            @RequestParam(value = "studyYear", required = false) Integer studyYear,
            @RequestParam(value = "courseId", required = false) Long courseId,
            @RequestParam(value = "subjectId", required = false) Long subjectId,
            @RequestParam(value = "unit", required = false) String unit,
            Authentication authentication) throws IOException {
        Long studentId = resolveStudentId(authentication);
        return ResponseEntity.ok(noteHubService.create(file, title, description, noteType, studyYear, courseId, subjectId, unit, studentId));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('STUDENT') or hasRole('ADMIN') or hasRole('FACULTY')")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) {
        boolean isAdminOrFaculty = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_FACULTY"));
        Long studentId = null;
        if (!isAdminOrFaculty) {
            studentId = resolveStudentId(authentication);
        }
        noteHubService.delete(id, studentId, isAdminOrFaculty);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/upvote")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Map<String, Object>> upvote(@PathVariable Long id, Authentication authentication) {
        Long studentId = resolveStudentId(authentication);
        return ResponseEntity.ok(noteHubVoteService.toggle(id, studentId));
    }

    private Long resolveStudentId(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
        return studentRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "user", username))
                .getId();
    }
}