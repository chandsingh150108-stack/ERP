package com.studenterp.controller;

import com.studenterp.entity.*;
import com.studenterp.exception.ResourceNotFoundException;
import com.studenterp.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/exams")
@RequiredArgsConstructor
public class ExamController {

    private final ExamRepository examRepository;
    private final ExamResultRepository examResultRepository;
    private final StudentSectionRepository studentSectionRepository;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;

    @GetMapping
    public ResponseEntity<List<Exam>> list(Authentication auth) {
        Long studentId = resolveStudentIdSafe(auth);
        if (studentId != null) {
            List<StudentSection> enrollments = studentSectionRepository.findByStudentId(studentId);
            List<Exam> exams = new ArrayList<>();
            for (StudentSection ss : enrollments) {
                exams.addAll(examRepository.findBySectionIdOrderByExamDateAsc(ss.getSection().getId()));
            }
            return ResponseEntity.ok(exams);
        }
        return ResponseEntity.ok(examRepository.findAll());
    }

    @GetMapping("/schedule")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<Exam>> getSchedule(Authentication auth) {
        Long studentId = resolveStudentId(auth);
        List<StudentSection> enrollments = studentSectionRepository.findByStudentId(studentId);
        List<Exam> exams = new ArrayList<>();
        for (StudentSection ss : enrollments) {
            exams.addAll(examRepository.findBySectionIdAndStatusOrderByExamDateAsc(
                    ss.getSection().getId(), "SCHEDULED"));
        }
        return ResponseEntity.ok(exams);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Exam> findById(@PathVariable Long id) {
        Exam exam = examRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exam", "id", id));
        return ResponseEntity.ok(exam);
    }

    @GetMapping("/{id}/results")
    @PreAuthorize("hasRole('FACULTY') or hasRole('ADMIN')")
    public ResponseEntity<List<ExamResult>> getResults(@PathVariable Long id) {
        return ResponseEntity.ok(examResultRepository.findByExamIdOrderByMarksObtainedDesc(id));
    }

    @GetMapping("/{id}/my-result")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ExamResult> getMyResult(@PathVariable Long id, Authentication auth) {
        Long studentId = resolveStudentId(auth);
        ExamResult result = examResultRepository.findByExamIdAndStudentId(id, studentId)
                .orElse(null);
        return ResponseEntity.ok(result);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Exam> create(@RequestBody Exam exam) {
        return ResponseEntity.ok(examRepository.save(exam));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Exam> update(@PathVariable Long id, @RequestBody Exam updated) {
        Exam existing = examRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exam", "id", id));
        if (updated.getName() != null) existing.setName(updated.getName());
        if (updated.getExamType() != null) existing.setExamType(updated.getExamType());
        if (updated.getExamDate() != null) existing.setExamDate(updated.getExamDate());
        if (updated.getStartTime() != null) existing.setStartTime(updated.getStartTime());
        if (updated.getEndTime() != null) existing.setEndTime(updated.getEndTime());
        if (updated.getVenue() != null) existing.setVenue(updated.getVenue());
        if (updated.getMaxMarks() != null) existing.setMaxMarks(updated.getMaxMarks());
        if (updated.getStatus() != null) existing.setStatus(updated.getStatus());
        return ResponseEntity.ok(examRepository.save(existing));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        examRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/results/bulk")
    @PreAuthorize("hasRole('ADMIN') or hasRole('FACULTY')")
    public ResponseEntity<List<ExamResult>> bulkUploadResults(
            @PathVariable Long id,
            @RequestBody List<Map<String, Object>> results) {
        Exam exam = examRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exam", "id", id));
        List<ExamResult> saved = new ArrayList<>();
        for (Map<String, Object> r : results) {
            Long studentId = Long.valueOf(r.get("studentId").toString());
            Student student = studentRepository.findById(studentId).orElse(null);
            if (student == null) continue;

            ExamResult result = examResultRepository.findByExamIdAndStudentId(id, studentId)
                    .orElse(ExamResult.builder().exam(exam).student(student).build());
            result.setMarksObtained((Integer) r.get("marksObtained"));
            result.setGrade(calculateGrade((Integer) r.get("marksObtained"), exam.getMaxMarks()));
            result.setStatus("PUBLISHED");
            saved.add(examResultRepository.save(result));
        }
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/results/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('FACULTY')")
    public ResponseEntity<ExamResult> updateResult(
            @PathVariable Long id,
            @RequestParam Integer marksObtained,
            @RequestParam(required = false) String grade) {
        ExamResult result = examResultRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ExamResult", "id", id));
        result.setMarksObtained(marksObtained);
        if (grade != null) result.setGrade(grade);
        else result.setGrade(calculateGrade(marksObtained, result.getExam().getMaxMarks()));
        result.setStatus("PUBLISHED");
        return ResponseEntity.ok(examResultRepository.save(result));
    }

    @PutMapping("/{id}/publish")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> publishResults(@PathVariable Long id) {
        List<ExamResult> results = examResultRepository.findByExamIdOrderByMarksObtainedDesc(id);
        for (ExamResult r : results) {
            r.setStatus("PUBLISHED");
            examResultRepository.save(r);
        }
        return ResponseEntity.ok().build();
    }

    private String calculateGrade(Integer marks, Integer maxMarks) {
        if (marks == null || maxMarks == null || maxMarks == 0) return "N/A";
        double pct = (marks * 100.0) / maxMarks;
        if (pct >= 90) return "AA";
        if (pct >= 80) return "AB";
        if (pct >= 70) return "BB";
        if (pct >= 60) return "BC";
        if (pct >= 50) return "CD";
        if (pct >= 40) return "DD";
        return "FF";
    }

    private Long resolveStudentId(Authentication auth) {
        String username = auth.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
        return studentRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "user", username))
                .getId();
    }

    private Long resolveStudentIdSafe(Authentication auth) {
        try { return resolveStudentId(auth); } catch (Exception e) { return null; }
    }
}
