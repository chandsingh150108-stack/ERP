package com.studenterp.service;

import com.studenterp.entity.*;
import com.studenterp.exception.ResourceNotFoundException;
import com.studenterp.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;
    private final ProgramRepository programRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final StudentSectionRepository studentSectionRepository;
    private final SectionRepository sectionRepository;
    private final PasswordEncoder passwordEncoder;

    public List<Student> findAll() {
        return studentRepository.findAll();
    }

    public Student findById(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", id));
    }

    public List<Student> search(String keyword) {
        return studentRepository.search(keyword);
    }

    public Student create(Student student) {
        if (studentRepository.existsByRollNumber(student.getRollNumber())) {
            throw new IllegalArgumentException("Roll number already exists");
        }
        Program program = resolveProgram(student);
        student.setProgram(program);
        student.syncAcademicFields();

        if (student.getUser() == null || student.getUser().getId() == null) {
            String initialPassword = (student.getRollNumber() != null && !student.getRollNumber().isBlank())
                    ? student.getRollNumber()
                    : "student123";
            User user = User.builder()
                    .username(student.getRollNumber())
                    .email(student.getEmail() != null ? student.getEmail() : student.getRollNumber().toLowerCase() + "@college.edu")
                    .passwordHash(passwordEncoder.encode(initialPassword))
                    .active(true)
                    .build();
            user = userRepository.save(user);

            Role studentRole = roleRepository.findByName(Role.RoleType.STUDENT)
                    .orElseThrow(() -> new IllegalArgumentException("Student role not found"));
            userRoleRepository.save(UserRole.builder().user(user).role(studentRole).build());

            student.setUser(user);
        }

        return studentRepository.save(student);
    }

    public Student findByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
        return studentRepository.findByUser(user)
                .orElseGet(() -> studentRepository.findAll().stream().findFirst()
                        .orElseThrow(() -> new ResourceNotFoundException("Student", "username", username)));
    }

    public Student update(Long id, Student updated) {
        Student student = findById(id);
        if (updated.getProgram() != null || updated.getCourse() != null) {
            Program program = resolveProgram(updated);
            student.setProgram(program);
        }
        if (updated.getCourse() != null) student.setCourse(updated.getCourse());
        if (updated.getYear() != null) student.setYear(updated.getYear());
        if (updated.getStudyYear() != null) student.setStudyYear(updated.getStudyYear());
        if (updated.getCurrentSemester() != null) student.setCurrentSemester(updated.getCurrentSemester());
        student.syncAcademicFields();

        student.setFirstName(updated.getFirstName());
        student.setLastName(updated.getLastName());
        student.setRollNumber(updated.getRollNumber());
        student.setRegistrationNumber(updated.getRegistrationNumber());
        student.setDateOfBirth(updated.getDateOfBirth());
        student.setGender(updated.getGender());
        student.setEmail(updated.getEmail());
        student.setPhone(updated.getPhone());
        student.setAdmissionDate(updated.getAdmissionDate());
        student.setAddress(updated.getAddress());
        student.setCity(updated.getCity());
        student.setState(updated.getState());
        student.setPostalCode(updated.getPostalCode());
        student.setStatus(updated.getStatus());
        return studentRepository.save(student);
    }

    private Program resolveProgram(Student student) {
        if (student.getProgram() != null && student.getProgram().getId() != null && student.getProgram().getId() > 0) {
            return programRepository.findById(student.getProgram().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Program", "id", student.getProgram().getId()));
        }

        String query = student.getCourse();
        if (query == null && student.getProgram() != null) {
            query = student.getProgram().getCode() != null ? student.getProgram().getCode() : student.getProgram().getName();
        }

        List<Program> all = programRepository.findAll();
        if (query != null && !query.isBlank()) {
            String q = query.trim().toUpperCase();
            for (Program p : all) {
                String pCode = p.getCode() != null ? p.getCode().toUpperCase() : "";
                String pName = p.getName() != null ? p.getName().toUpperCase() : "";
                String deptCode = p.getDepartment() != null && p.getDepartment().getCode() != null ? p.getDepartment().getCode().toUpperCase() : "";

                if (q.contains("AIML") && (pCode.contains("AIML") || deptCode.equals("AIML") || pName.contains("MACHINE LEARNING"))) return p;
                if ((q.equals("CSE") || q.equals("CS") || q.contains("COMPUTER")) && !q.contains("AIML") && !q.contains("AIDS")) {
                    if (pCode.equals("BTECH-CSE") || deptCode.equals("CSE")) return p;
                }
                if (q.contains("AIDS") && (pCode.contains("AIDS") || deptCode.equals("AIDS") || pName.contains("DATA SCIENCE"))) return p;
                if (q.contains("IT") && (pCode.contains("IT") || deptCode.equals("IT") || pName.contains("INFORMATION"))) return p;
                if (q.contains("ENTC") && (pCode.contains("ENTC") || deptCode.equals("ENTC") || pName.contains("TELECOMMUNICATION"))) return p;
                if (q.contains("MECH") && (pCode.contains("MECH") || deptCode.equals("MECH") || pName.contains("MECHANICAL"))) return p;
                if (q.contains("CHEM") && (pCode.contains("CHEM") || deptCode.equals("CHEM") || pName.contains("CHEMICAL"))) return p;
                if (q.contains("INST") && (pCode.contains("INST") || deptCode.equals("INST") || pName.contains("INSTRUMENTATION"))) return p;
                if (pCode.equalsIgnoreCase(query.trim()) || pName.equalsIgnoreCase(query.trim())) return p;
            }
        }

        if (!all.isEmpty()) return all.get(0);
        throw new IllegalArgumentException("No degree program registered in ERP");
    }

    public void delete(Long id) {
        Student student = findById(id);
        studentRepository.delete(student);
    }

    public List<StudentSection> getStudentSections(Long studentId) {
        return studentSectionRepository.findByStudentId(studentId);
    }

    public StudentSection enrollInSection(Long studentId, Long sectionId) {
        return studentSectionRepository.findByStudentId(studentId).stream()
                .filter(e -> e.getSection() != null && e.getSection().getId().equals(sectionId))
                .findFirst()
                .orElseGet(() -> {
                    Student student = findById(studentId);
                    Section section = sectionRepository.findById(sectionId)
                            .orElseThrow(() -> new ResourceNotFoundException("Section", "id", sectionId));
                    StudentSection ss = StudentSection.builder().student(student).section(section).build();
                    return studentSectionRepository.save(ss);
                });
    }
}
