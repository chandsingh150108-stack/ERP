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
@RequestMapping("/api/academics")
@RequiredArgsConstructor
public class AcademicsController {

    private final StudentRepository studentRepository;
    private final StudentSectionRepository studentSectionRepository;
    private final SectionRepository sectionRepository;
    private final FacultyCourseRepository facultyCourseRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final ProgramRepository programRepository;

    @GetMapping("/me")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Map<String, Object>> getMyAcademics(Authentication auth) {
        String username = auth.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
        Student student = studentRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "user", username));

        List<StudentSection> enrollments = studentSectionRepository.findByStudentId(student.getId());

        List<Map<String, Object>> courses = new ArrayList<>();
        int totalCredits = 0;

        for (StudentSection ss : enrollments) {
            Section sec = ss.getSection();
            Map<String, Object> courseInfo = new HashMap<>();
            courseInfo.put("sectionName", sec.getName());
            courseInfo.put("semester", sec.getSemester());
            courseInfo.put("programName", sec.getProgram().getName());
            courseInfo.put("programCode", sec.getProgram().getCode());

            // Get faculty courses for this section
            List<FacultyCourse> facultyCourses = new ArrayList<>();
            if (sec.getProgram() != null) {
                facultyCourses = facultyCourseRepository.findByFacultyId(student.getId());
            }

            // Get courses from the program's department
            List<Course> deptCourses = courseRepository.findByDepartmentId(
                    sec.getProgram().getDepartment().getId());
            courseInfo.put("availableCourses", deptCourses.size());
            totalCredits = sec.getProgram().getTotalCredits();

            courses.add(courseInfo);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("student", student);
        result.put("enrolledSections", courses.size());
        result.put("enrolledCourses", courses);
        result.put("totalProgramCredits", totalCredits);
        result.put("programName", student.getProgram() != null ? student.getProgram().getName() : "N/A");
        result.put("programCode", student.getProgram() != null ? student.getProgram().getCode() : "N/A");
        result.put("departmentName", student.getProgram() != null && student.getProgram().getDepartment() != null
                ? student.getProgram().getDepartment().getName() : "N/A");

        return ResponseEntity.ok(result);
    }

    @GetMapping("/me/transcript")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<Map<String, Object>>> getTranscript(Authentication auth) {
        // Placeholder - full transcript requires exam results
        return ResponseEntity.ok(new ArrayList<>());
    }

    @GetMapping("/programs")
    public ResponseEntity<List<Program>> getPrograms() {
        return ResponseEntity.ok(programRepository.findAll());
    }

    @GetMapping("/courses")
    public ResponseEntity<List<Course>> getCourses() {
        return ResponseEntity.ok(courseRepository.findAll());
    }
}
