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
@RequestMapping("/api/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackRepository feedbackRepository;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final FacultyRepository facultyRepository;
    private final CourseRepository courseRepository;
    private final FacultyCourseRepository facultyCourseRepository;

    @GetMapping
    public ResponseEntity<List<Feedback>> list(Authentication auth) {
        String username = auth.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
        Faculty faculty = facultyRepository.findByUsername(username).orElse(null);
        Student student = studentRepository.findByUser(user).orElse(null);

        if (faculty != null) {
            return ResponseEntity.ok(feedbackRepository.findByFacultyIdOrderBySubmittedAtDesc(faculty.getId()));
        } else if (student != null) {
            return ResponseEntity.ok(feedbackRepository.findByStudentIdOrderBySubmittedAtDesc(student.getId()));
        }
        return ResponseEntity.ok(feedbackRepository.findAll());
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<Feedback>> getMyFeedback(Authentication auth) {
        Long studentId = resolveStudentId(auth);
        return ResponseEntity.ok(feedbackRepository.findByStudentIdOrderBySubmittedAtDesc(studentId));
    }

    @GetMapping("/stats/{facultyId}")
    public ResponseEntity<Map<String, Object>> getStats(@PathVariable Long facultyId) {
        Map<String, Object> stats = new HashMap<>();
        Double avgRating = feedbackRepository.findAvgRatingByFacultyId(facultyId);
        List<Feedback> feedbacks = feedbackRepository.findByFacultyIdOrderBySubmittedAtDesc(facultyId);
        stats.put("avgRating", avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : 0);
        stats.put("totalFeedbacks", feedbacks.size());

        Map<Integer, Long> distribution = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            final int r = i;
            distribution.put(i, feedbacks.stream().filter(f -> f.getRating() == r).count());
        }
        stats.put("distribution", distribution);
        return ResponseEntity.ok(stats);
    }

    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> submit(@RequestBody Map<String, Object> payload, Authentication auth) {
        Long studentId = resolveStudentId(auth);
        Long courseId = Long.valueOf(payload.get("courseId").toString());

        if (feedbackRepository.existsByStudentIdAndCourseId(studentId, courseId)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Feedback already submitted for this course"));
        }

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", studentId));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));

        // Find faculty for this course
        List<FacultyCourse> fcList = facultyCourseRepository.findByCourseId(courseId);
        Faculty faculty = !fcList.isEmpty() ? fcList.get(0).getFaculty() : null;

        Feedback feedback = Feedback.builder()
                .student(student)
                .course(course)
                .faculty(faculty)
                .rating((Integer) payload.get("rating"))
                .comments((String) payload.getOrDefault("comments", ""))
                .category((String) payload.getOrDefault("category", "COURSE"))
                .isAnonymous(Boolean.TRUE.equals(payload.get("isAnonymous")))
                .status("SUBMITTED")
                .build();

        return ResponseEntity.ok(feedbackRepository.save(feedback));
    }

    @PutMapping("/{id}/review")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Feedback> markReviewed(@PathVariable Long id) {
        Feedback feedback = feedbackRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Feedback", "id", id));
        feedback.setStatus("REVIEWED");
        return ResponseEntity.ok(feedbackRepository.save(feedback));
    }

    private Long resolveStudentId(Authentication auth) {
        String username = auth.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
        return studentRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "user", username))
                .getId();
    }
}
