package com.studenterp.controller;

import com.studenterp.dto.DashboardStats;
import com.studenterp.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final StudentRepository studentRepository;
    private final FacultyRepository facultyRepository;
    private final DepartmentRepository departmentRepository;
    private final ProgramRepository programRepository;
    private final CourseRepository courseRepository;

    @GetMapping
    public ResponseEntity<DashboardStats> getStats() {
        DashboardStats stats = new DashboardStats();
        stats.setTotalStudents(studentRepository.count());
        stats.setTotalFaculty(facultyRepository.count());
        stats.setTotalDepartments(departmentRepository.count());
        stats.setTotalPrograms(programRepository.count());
        stats.setActiveCourses(courseRepository.count());
        stats.setPendingFees(0);
        stats.setOpenComplaints(0);
        stats.setUpcomingEvents(0);
        stats.setTotalBooks(0);
        stats.setIssuedBooks(0);
        return ResponseEntity.ok(stats);
    }
}
