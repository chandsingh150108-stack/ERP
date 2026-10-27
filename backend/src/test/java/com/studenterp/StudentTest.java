package com.studenterp;

import com.studenterp.entity.*;
import com.studenterp.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class StudentTest {

    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private ProgramRepository programRepository;
    @Autowired
    private DepartmentRepository departmentRepository;

    @Test
    void testCreateAndFindStudent() {
        Department dept = departmentRepository.save(Department.builder().code("CS").name("CS Dept").active(true).build());
        Program program = programRepository.save(Program.builder()
                .department(dept).code("BCS").name("B.Sc CS")
                .degreeType("B.Sc").durationYears(3).totalCredits(120).active(true).build());

        Student student = Student.builder()
                .program(program).rollNumber("CS001").registrationNumber("R001")
                .firstName("John").lastName("Doe")
                .dateOfBirth(LocalDate.of(2002, 5, 15)).gender("Male")
                .email("john@example.com").phone("1234567890")
                .admissionDate(LocalDate.of(2023, 8, 1)).currentSemester(4)
                .status("ACTIVE").build();
        Student saved = studentRepository.save(student);
        assertNotNull(saved.getId());

        Student found = studentRepository.findById(saved.getId()).orElse(null);
        assertNotNull(found);
        assertEquals("John", found.getFirstName());
    }

    @Test
    void testSearchStudents() {
        Department dept = departmentRepository.save(Department.builder().code("CS").name("CS Dept").active(true).build());
        Program program = programRepository.save(Program.builder()
                .department(dept).code("BCS").name("B.Sc CS")
                .degreeType("B.Sc").durationYears(3).totalCredits(120).active(true).build());

        studentRepository.save(Student.builder()
                .program(program).rollNumber("S001").firstName("Alice").lastName("Smith")
                .status("ACTIVE").build());
        studentRepository.save(Student.builder()
                .program(program).rollNumber("S002").firstName("Bob").lastName("Jones")
                .status("ACTIVE").build());

        var results = studentRepository.search("alice");
        assertEquals(1, results.size());
        assertEquals("Alice", results.get(0).getFirstName());

        var results2 = studentRepository.search("S002");
        assertEquals(1, results2.size());
    }

    @Test
    void testDuplicateRollNumberFails() {
        Department dept = departmentRepository.save(Department.builder().code("CS").name("CS Dept").active(true).build());
        Program program = programRepository.save(Program.builder()
                .department(dept).code("BCS").name("B.Sc CS")
                .degreeType("B.Sc").durationYears(3).totalCredits(120).active(true).build());

        studentRepository.save(Student.builder()
                .program(program).rollNumber("DUP").firstName("A").lastName("B").status("ACTIVE").build());

        assertThrows(Exception.class, () ->
                studentRepository.save(Student.builder()
                        .program(program).rollNumber("DUP").firstName("C").lastName("D").status("ACTIVE").build()));
    }
}
