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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vault")
@RequiredArgsConstructor
public class VaultController {

    private final VaultSubjectService vaultSubjectService;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;

    @GetMapping("/profile")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STUDENT')")
    public ResponseEntity<Map<String, Object>> getProfile(Authentication authentication) {
        Student student = resolveStudent(authentication);
        Map<String, Object> map = new HashMap<>();
        map.put("id", student.getId());
        map.put("firstName", student.getFirstName());
        map.put("lastName", student.getLastName());
        map.put("rollNumber", student.getRollNumber());
        map.put("course", student.getCourse());
        map.put("year", student.getYear());
        map.put("studyYear", student.getStudyYear());
        map.put("currentSemester", student.getCurrentSemester());
        map.put("email", student.getEmail());
        map.put("phone", student.getPhone());
        map.put("gender", student.getGender());
        map.put("status", student.getStatus());
        map.put("admissionDate", student.getAdmissionDate());
        if (student.getProgram() != null) {
            Map<String, Object> prog = new HashMap<>();
            prog.put("id", student.getProgram().getId());
            prog.put("code", student.getProgram().getCode());
            prog.put("name", student.getProgram().getName());
            map.put("program", prog);
        }
        return ResponseEntity.ok(map);
    }

    @GetMapping("/courses")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STUDENT')")
    public ResponseEntity<List<Map<String, Object>>> getAvailableCourses(Authentication authentication) {
        Student student = resolveStudent(authentication);
        return ResponseEntity.ok(vaultSubjectService.getAvailableCoursesForStudent(student.getId()));
    }

    @PostMapping("/sync")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STUDENT')")
    public ResponseEntity<List<VaultSubject>> syncCurriculum(Authentication authentication) {
        Student student = resolveStudent(authentication);
        return ResponseEntity.ok(vaultSubjectService.syncCourseCurriculum(student.getId()));
    }

    private Student resolveStudent(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
        return studentRepository.findByUser(user)
                .orElseGet(() -> studentRepository.findAll().stream().findFirst()
                        .orElseThrow(() -> new ResourceNotFoundException("Student", "user", username)));
    }
}

