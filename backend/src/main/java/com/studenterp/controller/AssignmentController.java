package com.studenterp.controller;

import com.studenterp.entity.*;
import com.studenterp.exception.ResourceNotFoundException;
import com.studenterp.repository.*;
import com.studenterp.service.AssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@RestController
@RequestMapping("/api/assignments")
@RequiredArgsConstructor
public class AssignmentController {

    private final AssignmentService assignmentService;
    private final StudentSectionRepository studentSectionRepository;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final FacultyRepository facultyRepository;
    private final CourseRepository courseRepository;
    private final FacultyCourseRepository facultyCourseRepository;

    @GetMapping("/courses/for-student")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<Course>> getCoursesForStudent(Authentication authentication) {
        Long studentId = resolveStudentId(authentication);
        Student student = studentRepository.findById(studentId).orElse(null);
        if (student == null) return ResponseEntity.ok(new ArrayList<>());

        List<Course> allCourses = courseRepository.findAll();
        Integer studyYear = student.getStudyYear();

        // Exact branch only: "CSE AIML" -> AIML, not CSE. Check multi-token codes first.
        String branch = resolveExactBranch(student);

        // FY (studyYear == 1) is served by DESH foundation courses.
        if (studyYear != null && studyYear == 1) {
            List<Course> desh = allCourses.stream().filter(c ->
                    c.getStudyYear() != null && c.getStudyYear().equals(1)
                            && c.getDepartment() != null
                            && "DESH".equalsIgnoreCase(c.getDepartment().getCode())).toList();
            if (!desh.isEmpty()) return ResponseEntity.ok(desh);
        }

        List<Course> filtered = allCourses.stream().filter(c -> {
            if (studyYear != null && c.getStudyYear() != null && !c.getStudyYear().equals(studyYear)) return false;
            if (branch != null && c.getDepartment() != null && c.getDepartment().getCode() != null) {
                if (!c.getDepartment().getCode().equalsIgnoreCase(branch)) return false;
            }
            return true;
        }).toList();
        if (!filtered.isEmpty()) return ResponseEntity.ok(filtered);

        // Fallback: year-wide scope (any branch, matching year).
        List<Course> yearWide = allCourses.stream().filter(c ->
                studyYear == null || c.getStudyYear() == null || c.getStudyYear().equals(studyYear)).toList();
        if (!yearWide.isEmpty()) return ResponseEntity.ok(yearWide);

        return ResponseEntity.ok(allCourses);
    }

    private String resolveExactBranch(Student student) {
        String courseStr = student.getCourse();
        if (courseStr != null && !courseStr.isBlank()) {
            String upper = courseStr.trim().toUpperCase();
            // Multi-word / compound codes take precedence over substring matching.
            if (upper.contains("AIML")) return "AIML";
            if (upper.contains("AIDS")) return "AIDS";
            if (upper.equals("CSE") || upper.endsWith(" CSE") || upper.startsWith("CSE ")) return "CSE";
            if (upper.contains("CSE") && !upper.contains(" ")) return "CSE";
            if (upper.equals("IT") || upper.contains(" IT") || upper.contains("IT ")) return "IT";
            if (upper.contains("ENTC")) return "ENTC";
            if (upper.contains("MECH")) return "MECH";
            if (upper.contains("CHEM")) return "CHEM";
            if (upper.contains("INST")) return "INST";
            if (upper.contains("DESH")) return "DESH";
            return courseStr.trim().split("\\s+")[courseStr.trim().split("\\s+").length - 1].toUpperCase();
        }
        if (student.getProgram() != null && student.getProgram().getDepartment() != null) {
            return student.getProgram().getDepartment().getCode();
        }
        return null;
    }

    @GetMapping
    public ResponseEntity<List<Assignment>> list(Authentication authentication) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (isAdmin) {
            return ResponseEntity.ok(assignmentService.findAll());
        }
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
        Student student = studentRepository.findByUser(user).orElse(null);
        Faculty faculty = facultyRepository.findByUsername(username).orElse(null);

        if (student != null) {
            return ResponseEntity.ok(assignmentService.findByStudentId(student.getId()));
        } else if (faculty != null) {
            return ResponseEntity.ok(assignmentService.findByFacultyId(faculty.getId()));
        }
        return ResponseEntity.ok(assignmentService.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('STUDENT') or hasRole('FACULTY') or hasRole('ADMIN')")
    public ResponseEntity<Assignment> findById(@PathVariable Long id) {
        return ResponseEntity.ok(assignmentService.findById(id));
    }

    @GetMapping("/{id}/submissions")
    @PreAuthorize("hasRole('FACULTY') or hasRole('ADMIN')")
    public ResponseEntity<List<AssignmentSubmission>> getSubmissions(@PathVariable Long id) {
        return ResponseEntity.ok(assignmentService.getSubmissions(id));
    }

    @GetMapping("/{id}/my-submission")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<AssignmentSubmission> getMySubmission(@PathVariable Long id, Authentication authentication) {
        Long studentId = resolveStudentId(authentication);
        AssignmentSubmission submission = assignmentService.getStudentSubmission(id, studentId);
        return ResponseEntity.ok(submission);
    }

    @PostMapping
    @PreAuthorize("hasRole('FACULTY') or hasRole('ADMIN')")
    public ResponseEntity<Assignment> create(
            @ModelAttribute Assignment assignment,
            @RequestParam(value = "file", required = false) MultipartFile file,
            Authentication authentication) {
        String username = authentication.getName();
        Faculty faculty = facultyRepository.findByUsername(username).orElse(null);
        Long facultyId = faculty != null ? faculty.getId() : resolveFacultyFor(assignment);
        return ResponseEntity.ok(assignmentService.create(assignment, file, facultyId));
    }

    /** Admin calls carry no faculty identity: resolve the assigned faculty for the section+course. */
    private Long resolveFacultyFor(Assignment assignment) {
        if (assignment.getSection() == null || assignment.getSection().getId() == null
                || assignment.getCourse() == null || assignment.getCourse().getId() == null) {
            throw new IllegalArgumentException("Course and section are required");
        }
        return facultyCourseRepository.findBySectionId(assignment.getSection().getId()).stream()
                .filter(fc -> fc.getCourse() != null && fc.getCourse().getId().equals(assignment.getCourse().getId()))
                .map(fc -> fc.getFaculty().getId())
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "No faculty assigned to this course/section; assign a faculty first"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('FACULTY') or hasRole('ADMIN')")
    public ResponseEntity<Assignment> update(
            @PathVariable Long id,
            @ModelAttribute Assignment assignment,
            @RequestParam(value = "file", required = false) MultipartFile file,
            Authentication authentication) {
        String username = authentication.getName();
        Faculty faculty = facultyRepository.findByUsername(username).orElse(null);
        Long facultyId = faculty != null ? faculty.getId() : null;
        return ResponseEntity.ok(assignmentService.update(id, assignment, file, facultyId));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('FACULTY') or hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        assignmentService.delete(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<AssignmentSubmission> submit(
            @PathVariable Long id,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "textSubmission", required = false) String textSubmission,
            Authentication authentication) {
        Long studentId = resolveStudentId(authentication);
        return ResponseEntity.ok(assignmentService.submit(id, studentId, file, textSubmission));
    }

    @PutMapping("/submissions/{id}/grade")
    @PreAuthorize("hasRole('FACULTY') or hasRole('ADMIN')")
    public ResponseEntity<AssignmentSubmission> grade(
            @PathVariable Long id,
            @RequestParam Integer marks,
            @RequestParam(required = false) String feedback) {
        return ResponseEntity.ok(assignmentService.gradeSubmission(id, marks, feedback));
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
