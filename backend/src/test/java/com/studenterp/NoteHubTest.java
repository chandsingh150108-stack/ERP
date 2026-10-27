package com.studenterp;

import com.studenterp.entity.Course;
import com.studenterp.entity.Department;
import com.studenterp.entity.NoteHubNote;
import com.studenterp.entity.NoteHubSubject;
import com.studenterp.entity.NoteHubVote;
import com.studenterp.entity.Program;
import com.studenterp.entity.Student;
import com.studenterp.entity.User;
import com.studenterp.entity.UserRole;
import com.studenterp.repository.CourseRepository;
import com.studenterp.repository.DepartmentRepository;
import com.studenterp.repository.NoteHubNoteRepository;
import com.studenterp.repository.NoteHubSubjectRepository;
import com.studenterp.repository.NoteHubVoteRepository;
import com.studenterp.repository.ProgramRepository;
import com.studenterp.repository.RoleRepository;
import com.studenterp.repository.StudentRepository;
import com.studenterp.repository.UserRepository;
import com.studenterp.repository.UserRoleRepository;
import com.studenterp.service.NoteHubService;
import com.studenterp.service.NoteHubSubjectService;
import com.studenterp.service.NoteHubVoteService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class NoteHubTest {

    @Autowired private NoteHubSubjectRepository subjectRepository;
    @Autowired private NoteHubNoteRepository noteRepository;
    @Autowired private NoteHubVoteRepository voteRepository;
    @Autowired private NoteHubSubjectService subjectService;
    @Autowired private NoteHubService noteService;
    @Autowired private NoteHubVoteService voteService;
    @Autowired private DepartmentRepository departmentRepository;
    @Autowired private CourseRepository courseRepository;
    @Autowired private ProgramRepository programRepository;
    @Autowired private StudentRepository studentRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private UserRoleRepository userRoleRepository;
    @Autowired private EntityManager entityManager;

    private Department cseDept;
    private Course ai1302;
    private Program btechCse;
    private Student student1;
    private Student student2;
    private NoteHubSubject subject;

    @BeforeEach
    void setup() {
        cseDept = departmentRepository.findByCode("CSE").orElseGet(() -> {
            Department dept = Department.builder().code("CSE").name("Computer Science").active(true).build();
            return departmentRepository.save(dept);
        });

        ai1302 = courseRepository.findByCode("AI1302").orElseGet(() -> {
            Course course = Course.builder().code("AI1302").name("Data Structures and Algorithms").department(cseDept).credits(4).build();
            return courseRepository.save(course);
        });

        btechCse = programRepository.findByCode("BTECH-CSE").orElseGet(() -> {
            Program prog = Program.builder().code("BTECH-CSE").name("B.Tech CSE").department(cseDept).degreeType("B.Tech").durationYears(4).totalCredits(160).active(true).build();
            return programRepository.save(prog);
        });

        User u1 = User.builder().username("notehub_student1").email("s1@test.com").passwordHash("hash").active(true).build();
        u1 = userRepository.save(u1);
        com.studenterp.entity.Role studentRole = roleRepository.findByName(com.studenterp.entity.Role.RoleType.STUDENT).orElseThrow();
        UserRole ur1 = UserRole.builder().user(u1).role(studentRole).build();
        userRoleRepository.save(ur1);
        student1 = Student.builder().user(u1).program(btechCse).rollNumber("NH2025001").firstName("Alice").lastName("Test").email("s1@test.com").admissionDate(java.time.LocalDate.now()).currentSemester(2).status("ACTIVE").build();
        student1 = studentRepository.save(student1);

        User u2 = User.builder().username("notehub_student2").email("s2@test.com").passwordHash("hash").active(true).build();
        u2 = userRepository.save(u2);
        com.studenterp.entity.Role studentRole2 = roleRepository.findByName(com.studenterp.entity.Role.RoleType.STUDENT).orElseThrow();
        UserRole ur2 = UserRole.builder().user(u2).role(studentRole2).build();
        userRoleRepository.save(ur2);
        student2 = Student.builder().user(u2).program(btechCse).rollNumber("NH2025002").firstName("Bob").lastName("Test").email("s2@test.com").admissionDate(java.time.LocalDate.now()).currentSemester(2).status("ACTIVE").build();
        student2 = studentRepository.save(student2);

        subject = NoteHubSubject.builder().code("AI1302").name("Data Structures and Algorithms").studyYear(2).course(ai1302).build();
        subject = subjectService.create(subject);
    }

    @Test
    void testCreateSubject_duplicateCodeFails() {
        assertThrows(IllegalArgumentException.class, () -> {
            NoteHubSubject dup = NoteHubSubject.builder().code("AI1302").name("Duplicate Subject").studyYear(2).course(ai1302).build();
            subjectService.create(dup);
        });
    }

    @Test
    void testUploadPdfNote_success() throws Exception {
        MockMultipartFile pdf = new MockMultipartFile("file", "test.pdf", "application/pdf", "PDF content".getBytes());
        NoteHubNote note = noteService.create(pdf, "Lecture 1", "Intro to DSA", "COURSE", 2, ai1302.getId(), subject.getId(), "I", student1.getId());

        assertNotNull(note.getId());
        assertEquals("COURSE", note.getNoteType());
        assertEquals("Lecture 1", note.getTitle());
        assertEquals(2, note.getStudyYear());
        assertEquals(ai1302.getId(), note.getCourse().getId());
        assertEquals(subject.getId(), note.getSubject().getId());
        assertEquals("I", note.getUnit());
        assertEquals(0, note.getVoteCount());
        assertTrue(note.getFileName().endsWith(".pdf"));
    }

    @Test
    void testUploadNonPdf_rejected() {
        MockMultipartFile txt = new MockMultipartFile("file", "test.txt", "text/plain", "Not a PDF".getBytes());
        assertThrows(IllegalArgumentException.class, () ->
            noteService.create(txt, "Bad", "desc", "COURSE", 2, ai1302.getId(), subject.getId(), "I", student1.getId())
        );
    }

    @Test
    void testUploadCourseNote_mismatchedSubjectCourse_fails() {
        Course otherCourse = Course.builder().code("CS999").name("Other").department(cseDept).credits(3).build();
        otherCourse = courseRepository.save(otherCourse);
        NoteHubSubject otherSubject = NoteHubSubject.builder().code("CS999").name("Other Subject").studyYear(2).course(otherCourse).build();
        otherSubject = subjectService.create(otherSubject);

        MockMultipartFile pdf = new MockMultipartFile("file", "test.pdf", "application/pdf", "PDF".getBytes());
        Long courseId = ai1302.getId();
        Long otherSubjectId = otherSubject.getId();
        assertThrows(IllegalArgumentException.class, () ->
            noteService.create(pdf, "Bad", "desc", "COURSE", 2, courseId, otherSubjectId, "I", student1.getId())
        );
    }

    @Test
    void testGlobalVisibility_student2SeesStudent1Note() throws Exception {
        MockMultipartFile pdf = new MockMultipartFile("file", "s1.pdf", "application/pdf", "S1".getBytes());
        NoteHubNote note = noteService.create(pdf, "S1 Note", "desc", "COURSE", 2, ai1302.getId(), subject.getId(), "I", student1.getId());

        List<NoteHubNote> notes = noteRepository.findAll();
        assertEquals(1, notes.size());
        NoteHubNote found = noteService.findById(note.getId());
        assertEquals(student1.getId(), found.getUploader().getId());
    }

    @Test
    void testUpvote_toggleOnOff() throws Exception {
        MockMultipartFile pdf = new MockMultipartFile("file", "vote.pdf", "application/pdf", "PDF".getBytes());
        NoteHubNote note = noteService.create(pdf, "Vote Me", "desc", "COURSE", 2, ai1302.getId(), subject.getId(), "I", student1.getId());

        var r1 = voteService.toggle(note.getId(), student1.getId());
        assertEquals(true, r1.get("voted"));
        assertEquals(1, r1.get("voteCount"));

        var r2 = voteService.toggle(note.getId(), student1.getId());
        assertEquals(false, r2.get("voted"));
        assertEquals(0, r2.get("voteCount"));

        assertFalse(voteRepository.existsByNoteIdAndStudentId(note.getId(), student1.getId()));
    }

    @Test
    void testUpvote_duplicatePreventedByConstraint() throws Exception {
        MockMultipartFile pdf = new MockMultipartFile("file", "vote.pdf", "application/pdf", "PDF".getBytes());
        NoteHubNote note = noteService.create(pdf, "Vote Me", "desc", "COURSE", 2, ai1302.getId(), subject.getId(), "I", student1.getId());

        NoteHubVote v1 = NoteHubVote.builder().note(note).student(student1).build();
        voteRepository.saveAndFlush(v1);

        assertThrows(Exception.class, () -> {
            NoteHubVote v2 = NoteHubVote.builder().note(note).student(student1).build();
            voteRepository.saveAndFlush(v2);
        });
    }

    @Test
    void testTop5AndLoadMore() throws Exception {
        for (int i = 0; i < 7; i++) {
            MockMultipartFile pdf = new MockMultipartFile("file", "n" + i + ".pdf", "application/pdf", ("N" + i).getBytes());
            NoteHubNote n = noteService.create(pdf, "Note " + i, "desc", "COURSE", 2, ai1302.getId(), subject.getId(), "I", student1.getId());
            if (i < 3) { voteService.toggle(n.getId(), student1.getId()); }
        }

        var page0 = noteRepository.findByNoteTypeAndStudyYearAndCourseIdAndSubjectIdAndUnitOrderByVoteCountDescCreatedAtDesc(
                "COURSE", 2, ai1302.getId(), subject.getId(), "I", org.springframework.data.domain.PageRequest.of(0, 5, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "voteCount", "createdAt")));
        assertEquals(5, page0.getContent().size());
        assertEquals(3, page0.getContent().get(0).getVoteCount());

        var page1 = noteRepository.findByNoteTypeAndStudyYearAndCourseIdAndSubjectIdAndUnitOrderByVoteCountDescCreatedAtDesc(
                "COURSE", 2, ai1302.getId(), subject.getId(), "I", org.springframework.data.domain.PageRequest.of(1, 5, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "voteCount", "createdAt")));
        assertEquals(2, page1.getContent().size());
    }

    @Test
    void testExtraSearch_titleAndDescription() throws Exception {
        MockMultipartFile pdf1 = new MockMultipartFile("file", "ai.pdf", "application/pdf", "AI".getBytes());
        noteService.create(pdf1, "How AI Works", "Complete guide to artificial intelligence", "EXTRA", null, null, null, null, student1.getId());

        MockMultipartFile pdf2 = new MockMultipartFile("file", "ml.pdf", "application/pdf", "ML".getBytes());
        noteService.create(pdf2, "Machine Learning Basics", "What is AI and how does it work", "EXTRA", null, null, null, null, student2.getId());

        MockMultipartFile pdf3 = new MockMultipartFile("file", "other.pdf", "application/pdf", "Other".getBytes());
        noteService.create(pdf3, "Unrelated Topic", "Nothing about AI here", "EXTRA", null, null, null, null, student1.getId());

        var results = noteRepository.searchExtra("AI", org.springframework.data.domain.PageRequest.of(0, 10));
        assertEquals(2, results.getContent().size());
        assertTrue(results.getContent().stream().anyMatch(n -> n.getTitle().contains("How AI Works")));
        assertTrue(results.getContent().stream().anyMatch(n -> n.getDescription().contains("What is AI")));
    }

    @Test
    void testDelete_ownerOk() throws Exception {
        MockMultipartFile pdf = new MockMultipartFile("file", "del.pdf", "application/pdf", "PDF".getBytes());
        NoteHubNote note = noteService.create(pdf, "To Delete", "desc", "COURSE", 2, ai1302.getId(), subject.getId(), "I", student1.getId());

        noteService.delete(note.getId(), student1.getId(), false);
        assertFalse(noteRepository.existsById(note.getId()));
        assertEquals(0, voteRepository.countByNoteId(note.getId()));
    }

    @Test
    void testDelete_strangerStudentForbidden() throws Exception {
        MockMultipartFile pdf = new MockMultipartFile("file", "del.pdf", "application/pdf", "PDF".getBytes());
        NoteHubNote note = noteService.create(pdf, "To Delete", "desc", "COURSE", 2, ai1302.getId(), subject.getId(), "I", student1.getId());

        assertThrows(SecurityException.class, () -> noteService.delete(note.getId(), student2.getId(), false));
    }

    @Test
    void testDelete_adminAllowedAndVotesCleaned() throws Exception {
        MockMultipartFile pdf = new MockMultipartFile("file", "del_admin.pdf", "application/pdf", "PDF".getBytes());
        NoteHubNote note = noteService.create(pdf, "To Delete Admin", "desc", "COURSE", 2, ai1302.getId(), subject.getId(), "I", student1.getId());
        voteService.toggle(note.getId(), student1.getId());
        voteService.toggle(note.getId(), student2.getId());
        assertEquals(2, voteRepository.countByNoteId(note.getId()));

        noteService.delete(note.getId(), null, true);
        assertFalse(noteRepository.existsById(note.getId()));
        assertEquals(0, voteRepository.countByNoteId(note.getId()));
    }
}