package com.studenterp;

import com.studenterp.entity.*;
import com.studenterp.repository.*;
import com.studenterp.service.VaultSubjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class VaultTest {

    @Autowired private VaultSubjectRepository vaultSubjectRepository;
    @Autowired private VaultItemRepository vaultItemRepository;
    @Autowired private StudentRepository studentRepository;
    @Autowired private ProgramRepository programRepository;
    @Autowired private DepartmentRepository departmentRepository;
    @Autowired private VaultSubjectService vaultSubjectService;

    private Student student;

    @BeforeEach
    void setup() {
        Department dept = departmentRepository.save(Department.builder().code("CS").name("CS Dept").active(true).build());
        Program program = programRepository.save(Program.builder()
                .department(dept).code("BCS").name("B.Sc CS")
                .degreeType("B.Sc").durationYears(3).totalCredits(120).active(true).build());
        student = studentRepository.save(Student.builder()
                .program(program).rollNumber("VAULT001").firstName("Vault").lastName("Tester")
                .status("ACTIVE").build());
    }

    @Test
    void testCreateAndFindVaultSubject() {
        VaultSubject subject = vaultSubjectRepository.save(VaultSubject.builder()
                .name("Mathematics").description("Calculus").color("#3b82f6").student(student).build());
        assertNotNull(subject.getId());

        List<VaultSubject> found = vaultSubjectRepository.findByStudentId(student.getId());
        assertEquals(1, found.size());
        assertEquals("Mathematics", found.get(0).getName());
    }

    @Test
    void testDuplicateSubjectNameFails() {
        vaultSubjectRepository.save(VaultSubject.builder()
                .name("Physics").student(student).build());

        assertThrows(Exception.class, () ->
                vaultSubjectRepository.save(VaultSubject.builder()
                        .name("Physics").student(student).build()));
    }

    @Test
    void testCreateVaultItem() {
        VaultSubject subject = vaultSubjectRepository.save(VaultSubject.builder()
                .name("Chemistry").student(student).build());

        VaultItem item = vaultItemRepository.save(VaultItem.builder()
                .title("Lab Report 1").description("Acid-base titration")
                .fileName("lab1.pdf").filePath("/tmp/lab1.pdf")
                .fileSize(1024L).fileType("application/pdf").category("assignments")
                .student(student).subject(subject).build());
        assertNotNull(item.getId());

        List<VaultItem> items = vaultItemRepository.findByStudentId(student.getId());
        assertEquals(1, items.size());
        assertEquals("Lab Report 1", items.get(0).getTitle());
    }

    @Test
    void testFilterByCategory() {
        vaultItemRepository.save(VaultItem.builder()
                .title("Notes").fileName("n.pdf").filePath("/tmp/n.pdf")
                .fileSize(100L).fileType("application/pdf").category("notes")
                .student(student).build());
        vaultItemRepository.save(VaultItem.builder()
                .title("Assignment").fileName("a.pdf").filePath("/tmp/a.pdf")
                .fileSize(200L).fileType("application/pdf").category("assignments")
                .student(student).build());

        List<VaultItem> notes = vaultItemRepository.findByStudentIdAndCategory(student.getId(), "notes");
        assertEquals(1, notes.size());
        assertEquals("Notes", notes.get(0).getTitle());
    }

    @Test
    void testFilterBySubject() {
        VaultSubject sub = vaultSubjectRepository.save(VaultSubject.builder()
                .name("Biology").student(student).build());
        vaultItemRepository.save(VaultItem.builder()
                .title("Bio Notes").fileName("b.pdf").filePath("/tmp/b.pdf")
                .fileSize(100L).fileType("application/pdf").category("notes")
                .student(student).subject(sub).build());
        vaultItemRepository.save(VaultItem.builder()
                .title("Math Notes").fileName("m.pdf").filePath("/tmp/m.pdf")
                .fileSize(100L).fileType("application/pdf").category("notes")
                .student(student).build());

        List<VaultItem> bioItems = vaultItemRepository.findByStudentIdAndSubjectId(student.getId(), sub.getId());
        assertEquals(1, bioItems.size());
        assertEquals("Bio Notes", bioItems.get(0).getTitle());
    }

    @Test
    void testDeleteSubjectCascades() {
        VaultSubject subject = vaultSubjectRepository.save(VaultSubject.builder()
                .name("ToDelete").student(student).build());
        vaultItemRepository.save(VaultItem.builder()
                .title("Item1").fileName("f.pdf").filePath("/tmp/f.pdf")
                .fileSize(100L).fileType("application/pdf").category("notes")
                .student(student).subject(subject).build());

        vaultSubjectService.delete(subject.getId(), student.getId());

        List<VaultItem> items = vaultItemRepository.findByStudentId(student.getId());
        assertEquals(1, items.size());
        assertNull(items.get(0).getSubject());
    }

    @Test
    void testStudentIsolation() {
        Department dept = departmentRepository.findByCode("CS").orElseThrow();
        Program program = programRepository.findByCode("BCS").orElseThrow();
        Student otherStudent = studentRepository.save(Student.builder()
                .program(program).rollNumber("VAULT002").firstName("Other").lastName("Student")
                .status("ACTIVE").build());

        vaultSubjectRepository.save(VaultSubject.builder().name("MySubject").student(student).build());
        vaultSubjectRepository.save(VaultSubject.builder().name("OtherSubject").student(otherStudent).build());

        List<VaultSubject> mySubjects = vaultSubjectRepository.findByStudentId(student.getId());
        assertEquals(1, mySubjects.size());
        assertEquals("MySubject", mySubjects.get(0).getName());
    }
}
