package com.studenterp.service;

import com.studenterp.entity.*;
import com.studenterp.exception.ResourceNotFoundException;
import com.studenterp.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FacultyService {

    private final FacultyRepository facultyRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final FacultyCourseRepository facultyCourseRepository;
    private final CourseRepository courseRepository;
    private final SectionRepository sectionRepository;
    private final AcademicYearRepository academicYearRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    public List<Faculty> findAll() {
        return facultyRepository.findAll();
    }

    public Faculty findById(Long id) {
        return facultyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty", "id", id));
    }

    public List<Faculty> search(String keyword) {
        return facultyRepository.search(keyword);
    }

    public Faculty create(Faculty faculty) {
        if (facultyRepository.existsByEmployeeNumber(faculty.getEmployeeNumber())) {
            throw new IllegalArgumentException("Employee number already exists");
        }
        Department dept = departmentRepository.findById(faculty.getDepartment().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", faculty.getDepartment().getId()));
        faculty.setDepartment(dept);

        if (faculty.getUser() == null || faculty.getUser().getId() == null) {
            String initialPass = (faculty.getEmployeeNumber() != null && !faculty.getEmployeeNumber().isBlank())
                    ? faculty.getEmployeeNumber()
                    : "faculty123";
            User user = User.builder()
                    .username(faculty.getEmployeeNumber())
                    .email(faculty.getEmail() != null ? faculty.getEmail() : faculty.getEmployeeNumber().toLowerCase() + "@college.edu")
                    .passwordHash(passwordEncoder.encode(initialPass))
                    .active(true)
                    .build();
            user = userRepository.save(user);

            Role facultyRole = roleRepository.findByName(Role.RoleType.FACULTY).orElse(null);
            if (facultyRole != null) {
                userRoleRepository.save(UserRole.builder().user(user).role(facultyRole).build());
            }

            faculty.setUser(user);
        }

        return facultyRepository.save(faculty);
    }

    public Faculty update(Long id, Faculty updated) {
        Faculty faculty = findById(id);
        Department dept = departmentRepository.findById(updated.getDepartment().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", updated.getDepartment().getId()));
        faculty.setFirstName(updated.getFirstName());
        faculty.setLastName(updated.getLastName());
        faculty.setEmployeeNumber(updated.getEmployeeNumber());
        faculty.setDepartment(dept);
        faculty.setEmail(updated.getEmail());
        faculty.setPhone(updated.getPhone());
        faculty.setDesignation(updated.getDesignation());
        faculty.setSpecialization(updated.getSpecialization());
        faculty.setAssignedSubjects(updated.getAssignedSubjects());
        faculty.setJoiningDate(updated.getJoiningDate());
        faculty.setEmploymentStatus(updated.getEmploymentStatus());
        return facultyRepository.save(faculty);
    }

    public void delete(Long id) {
        Faculty faculty = findById(id);
        facultyRepository.delete(faculty);
    }

    public List<FacultyCourse> getFacultyCourses(Long facultyId) {
        return facultyCourseRepository.findByFacultyId(facultyId);
    }

    public FacultyCourse assignCourse(Long facultyId, Long courseId, Long sectionId, Long academicYearId) {
        List<FacultyCourse> existing = facultyCourseRepository.findByFacultyId(facultyId);
        for (FacultyCourse fc : existing) {
            if (fc.getCourse() != null && fc.getCourse().getId().equals(courseId)
                    && fc.getSection() != null && fc.getSection().getId().equals(sectionId)) {
                return fc;
            }
        }
        Faculty faculty = findById(facultyId);
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Section", "id", sectionId));
        AcademicYear year = academicYearRepository.findById(academicYearId)
                .orElseThrow(() -> new ResourceNotFoundException("AcademicYear", "id", academicYearId));

        FacultyCourse fc = FacultyCourse.builder()
                .faculty(faculty)
                .course(course)
                .section(section)
                .academicYear(year)
                .build();
        return facultyCourseRepository.save(fc);
    }
}
