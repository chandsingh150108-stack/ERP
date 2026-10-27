package com.studenterp.config;

import com.studenterp.entity.*;
import com.studenterp.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final DepartmentRepository departmentRepository;
    private final ProgramRepository programRepository;
    private final AcademicYearRepository academicYearRepository;
    private final CourseRepository courseRepository;
    private final StudentRepository studentRepository;
    private final FacultyRepository facultyRepository;
    private final SectionRepository sectionRepository;
    private final StudentSectionRepository studentSectionRepository;
    private final FacultyCourseRepository facultyCourseRepository;
    private final VaultSubjectRepository vaultSubjectRepository;
    private final VaultItemRepository vaultItemRepository;
    private final EventRepository eventRepository;
    private final RoomRepository roomRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final AssignmentRepository assignmentRepository;
    private final ExamRepository examRepository;
    private final CertificateRepository certificateRepository;
    private final FeeStructureRepository feeStructureRepository;
    private final InvoiceRepository invoiceRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) return;

        // Roles
        Role adminRole = roleRepository.save(Role.builder().name(Role.RoleType.ADMIN).build());
        Role facultyRole = roleRepository.save(Role.builder().name(Role.RoleType.FACULTY).build());
        Role studentRole = roleRepository.save(Role.builder().name(Role.RoleType.STUDENT).build());

        // Admin user
        User adminUser = userRepository.save(User.builder()
                .username("admin").email("admin@college.edu")
                .passwordHash(passwordEncoder.encode("admin123")).active(true).build());
        userRoleRepository.save(UserRole.builder().user(adminUser).role(adminRole).build());

        // Departments
        Department cse = departmentRepository.save(Department.builder().code("CSE").name("Computer Science and Engineering").description("CSE Department").active(true).build());
        Department aiml = departmentRepository.save(Department.builder().code("AIML").name("Artificial Intelligence and Machine Learning").description("AIML Department").active(true).build());

        // Programs
        Program btechCse = programRepository.save(Program.builder().department(cse).code("BTECH-CSE").name("B.Tech Computer Science and Engineering").degreeType("B.Tech").durationYears(4).totalCredits(160).active(true).build());
        Program btechAiml = programRepository.save(Program.builder().department(aiml).code("BTECH-AIML").name("B.Tech AI and Machine Learning").degreeType("B.Tech").durationYears(4).totalCredits(160).active(true).build());

        // Academic Year
        AcademicYear ay = academicYearRepository.save(AcademicYear.builder().yearName("2025-2026").startDate(LocalDate.of(2025, 8, 1)).endDate(LocalDate.of(2026, 7, 31)).current(true).build());

        // Courses
        Course cs101 = courseRepository.save(Course.builder().department(cse).code("CS101").name("Programming Fundamentals").description("Intro to programming").credits(4).courseType("CORE").lectureHours(3).tutorialHours(1).practicalHours(2).build());
        Course cs102 = courseRepository.save(Course.builder().department(cse).code("CS102").name("Data Structures").description("Data structures and algorithms").credits(4).courseType("CORE").lectureHours(3).tutorialHours(1).practicalHours(2).build());
        Course cs201 = courseRepository.save(Course.builder().department(cse).code("CS201").name("Database Systems").description("RDBMS concepts").credits(3).courseType("CORE").lectureHours(3).tutorialHours(0).practicalHours(0).build());
        Course ai101 = courseRepository.save(Course.builder().department(aiml).code("AI101").name("Introduction to AI").description("AI fundamentals").credits(3).courseType("CORE").lectureHours(3).tutorialHours(0).practicalHours(0).build());

        // Faculty
        User f1User = userRepository.save(User.builder().username("faculty1").email("faculty1@college.edu").passwordHash(passwordEncoder.encode("faculty123")).active(true).build());
        userRoleRepository.save(UserRole.builder().user(f1User).role(facultyRole).build());
        Faculty fac1 = facultyRepository.save(Faculty.builder().user(f1User).department(cse).employeeNumber("EMP001").firstName("Rajesh").lastName("Kumar").email("rajesh@college.edu").phone("9876543210").designation("Professor").specialization("Computer Science").joiningDate(LocalDate.of(2015, 7, 1)).employmentStatus("ACTIVE").build());

        User f2User = userRepository.save(User.builder().username("faculty2").email("faculty2@college.edu").passwordHash(passwordEncoder.encode("faculty123")).active(true).build());
        userRoleRepository.save(UserRole.builder().user(f2User).role(facultyRole).build());
        Faculty fac2 = facultyRepository.save(Faculty.builder().user(f2User).department(aiml).employeeNumber("EMP002").firstName("Priya").lastName("Sharma").email("priya@college.edu").phone("9876543211").designation("Associate Professor").specialization("Machine Learning").joiningDate(LocalDate.of(2018, 1, 15)).employmentStatus("ACTIVE").build());

        // Sections
        Section secA = sectionRepository.save(Section.builder().program(btechCse).semester(2).academicYear(ay).name("CSE-A").capacity(60).build());
        Section secB = sectionRepository.save(Section.builder().program(btechCse).semester(2).academicYear(ay).name("CSE-B").capacity(60).build());

        // Students
        Program[] studentProgs = {btechAiml, btechAiml, btechCse, btechCse, btechAiml};
        String[] studentCourses = {"CSE AIML", "CSE AIML", "CSE", "CSE", "CSE AIML"};
        String[] studentYears = {"SY", "FY", "SY", "TY", "TY"};
        int[] studentSemesters = {3, 1, 3, 5, 5};
        int[] studentStudyYears = {2, 1, 2, 3, 3};

        for (int i = 1; i <= 5; i++) {
            User sUser = userRepository.save(User.builder()
                    .username("student" + i).email("student" + i + "@college.edu")
                    .passwordHash(passwordEncoder.encode("student123")).active(true).build());
            userRoleRepository.save(UserRole.builder().user(sUser).role(studentRole).build());
            Student student = studentRepository.save(Student.builder()
                    .user(sUser).program(studentProgs[i - 1])
                    .course(studentCourses[i - 1])
                    .year(studentYears[i - 1])
                    .studyYear(studentStudyYears[i - 1])
                    .currentSemester(studentSemesters[i - 1])
                    .rollNumber("STU2025" + String.format("%03d", i))
                    .registrationNumber("REG2025" + String.format("%03d", i))
                    .firstName("Student" + i).lastName("User")
                    .dateOfBirth(LocalDate.of(2003, 1, i))
                    .gender(i % 2 == 0 ? "Male" : "Female")
                    .email("student" + i + "@college.edu").phone("987654321" + i)
                    .admissionDate(LocalDate.of(2025, 8, 1))
                    .status("ACTIVE").build());
            studentSectionRepository.save(StudentSection.builder().student(student).section(secA).build());
        }

        // Faculty Courses
        facultyCourseRepository.save(FacultyCourse.builder().faculty(fac1).course(cs101).section(secA).academicYear(ay).build());
        facultyCourseRepository.save(FacultyCourse.builder().faculty(fac1).course(cs102).section(secA).academicYear(ay).build());
        facultyCourseRepository.save(FacultyCourse.builder().faculty(fac2).course(ai101).section(secA).academicYear(ay).build());

        // Vault - Sample data for first student (optional, non-critical)
        try {
            Student firstStudent = studentRepository.findByRollNumber("STU2025001").orElse(null);
            if (firstStudent != null) {
                VaultSubject math = vaultSubjectRepository.save(VaultSubject.builder()
                        .name("Mathematics").description("Calculus and Linear Algebra").color("#3b82f6").student(firstStudent).course(cs101).build());
                VaultSubject physics = vaultSubjectRepository.save(VaultSubject.builder()
                        .name("Physics").description("Mechanics and Thermodynamics").color("#ef4444").student(firstStudent).build());
                VaultSubject programming = vaultSubjectRepository.save(VaultSubject.builder()
                        .name("Programming").description("Java and Data Structures").color("#22c55e").student(firstStudent).course(cs102).build());

                vaultItemRepository.save(VaultItem.builder()
                        .title("Calculus Chapter 1 Notes").description("Derivatives and limits")
                        .fileName("calc_ch1.pdf").filePath("./uploads/vault/sample1.pdf")
                        .fileSize(1024000L).fileType("application/pdf").category("notes")
                        .student(firstStudent).subject(math).build());
                vaultItemRepository.save(VaultItem.builder()
                        .title("Physics Lab Report").description("Pendulum experiment")
                        .fileName("physics_lab1.pdf").filePath("./uploads/vault/sample2.pdf")
                        .fileSize(512000L).fileType("application/pdf").category("assignments")
                        .student(firstStudent).subject(physics).build());
                vaultItemRepository.save(VaultItem.builder()
                        .title("Java Collections Cheat Sheet").description("HashMap, ArrayList, LinkedList")
                        .fileName("java_collections.pdf").filePath("./uploads/vault/sample3.pdf")
                        .fileSize(256000L).fileType("application/pdf").category("reference")
                        .student(firstStudent).subject(programming).build());
            }
        } catch (Exception e) {
            System.out.println("Vault sample data skipped: " + e.getMessage());
        }


        // --- Events ---
        eventRepository.save(Event.builder()
                .name("Annual Tech Symposium")
                .description("Inter-college technical festival with hackathons, workshops, and guest lectures")
                .startDate(LocalDate.of(2026, 3, 15))
                .endDate(LocalDate.of(2026, 3, 17))
                .venue("Main Auditorium")
                .active(true).build());
        eventRepository.save(Event.builder()
                .name("Industrial Visit - TISCO")
                .description("Visit to TISCO plant, Jamshedpur for industrial exposure")
                .startDate(LocalDate.of(2026, 4, 10))
                .endDate(LocalDate.of(2026, 4, 12))
                .venue("Jamshedpur, Jharkhand")
                .active(true).build());
        eventRepository.save(Event.builder()
                .name("AI & ML Workshop")
                .description("Hands-on workshop on Large Language Models and Transformers")
                .startDate(LocalDate.of(2026, 5, 5))
                .endDate(LocalDate.of(2026, 5, 5))
                .venue("Online - Zoom")
                .active(true).build());

        // --- Rooms ---
        Room room1 = roomRepository.save(Room.builder()
                .name("LT-101").building("Academic Block A").capacity(60).type("LECTURE").build());
        Room room2 = roomRepository.save(Room.builder()
                .name("LAB-201").building("Academic Block B").capacity(30).type("LAB").build());
        Room room3 = roomRepository.save(Room.builder()
                .name("LT-102").building("Academic Block A").capacity(60).type("LECTURE").build());

        // --- TimeSlots ---
        timeSlotRepository.save(TimeSlot.builder()
                .dayOfWeek("MON").startTime(LocalTime.of(9, 0)).endTime(LocalTime.of(10, 0))
                .slotNumber(1).course(cs101).faculty(fac1).room(room1).section(secA).type("LECTURE").build());
        timeSlotRepository.save(TimeSlot.builder()
                .dayOfWeek("MON").startTime(LocalTime.of(10, 0)).endTime(LocalTime.of(11, 0))
                .slotNumber(2).course(cs102).faculty(fac1).room(room1).section(secA).type("LECTURE").build());
        timeSlotRepository.save(TimeSlot.builder()
                .dayOfWeek("TUE").startTime(LocalTime.of(9, 0)).endTime(LocalTime.of(10, 0))
                .slotNumber(1).course(cs101).faculty(fac1).room(room2).section(secA).type("PRACTICAL").build());
        timeSlotRepository.save(TimeSlot.builder()
                .dayOfWeek("WED").startTime(LocalTime.of(10, 0)).endTime(LocalTime.of(11, 0))
                .slotNumber(2).course(ai101).faculty(fac2).room(room3).section(secA).type("LECTURE").build());
        timeSlotRepository.save(TimeSlot.builder()
                .dayOfWeek("THU").startTime(LocalTime.of(11, 0)).endTime(LocalTime.of(12, 0))
                .slotNumber(3).course(cs102).faculty(fac1).room(room2).section(secA).type("PRACTICAL").build());

        // --- Assignments ---
        assignmentRepository.save(Assignment.builder()
                .title("Assignment 1: Linked Lists")
                .description("Implement singly and doubly linked list with insert, delete, and traverse operations")
                .course(cs101).section(secA).faculty(fac1)
                .assignedDate(LocalDate.of(2025, 9, 15))
                .dueDate(LocalDate.of(2025, 10, 1))
                .maxMarks(20).status("PUBLISHED").assignmentType("THEORY").build());
        assignmentRepository.save(Assignment.builder()
                .title("Assignment 2: Binary Trees")
                .description("Implement binary tree with recursive traversals")
                .course(cs102).section(secA).faculty(fac1)
                .assignedDate(LocalDate.of(2025, 10, 1))
                .dueDate(LocalDate.of(2025, 10, 20))
                .maxMarks(25).status("PUBLISHED").assignmentType("THEORY").build());

        // --- Exams ---
        examRepository.save(Exam.builder()
                .name("Mid-Term Examination - CS101").examType("MID_TERM")
                .course(cs101).section(secA).academicYear(ay)
                .examDate(LocalDate.of(2026, 3, 15))
                .startTime(LocalTime.of(10, 0)).endTime(LocalTime.of(13, 0))
                .venue("Main Block - Hall A").maxMarks(100).status("SCHEDULED").build());
        examRepository.save(Exam.builder()
                .name("End-Term Examination - CS102").examType("END_TERM")
                .course(cs102).section(secA).academicYear(ay)
                .examDate(LocalDate.of(2026, 5, 10))
                .startTime(LocalTime.of(10, 0)).endTime(LocalTime.of(13, 0))
                .venue("Main Block - Hall B").maxMarks(100).status("SCHEDULED").build());

        // --- Certificates ---
        Student firstStu = studentRepository.findByRollNumber("STU2025001").orElse(null);
        if (firstStu != null) {
            certificateRepository.save(Certificate.builder()
                    .student(firstStu).program(btechCse).certificateType("BONAFIDE")
                    .certificateNumber("VIT-2025-000001")
                    .issueDate(LocalDate.of(2025, 9, 1))
                    .purpose("Bank Loan Application")
                    .status("ISSUED").build());
        }

        // --- Fee Structures ---
        FeeStructure feeStruct = feeStructureRepository.save(FeeStructure.builder()
                .program(btechCse).academicYear(ay)
                .tuitionFee(new BigDecimal("120000.00"))
                .hostelFee(new BigDecimal("60000.00"))
                .examinationFee(new BigDecimal("5000.00"))
                .libraryFee(new BigDecimal("2000.00"))
                .dueDate(LocalDate.of(2026, 2, 28))
                .status("ACTIVE").feeCategory("REGULAR").build());

        // --- Invoices for student1 ---
        if (firstStu != null && feeStruct.getId() != null) {
            invoiceRepository.save(Invoice.builder()
                    .student(firstStu).feeStructure(feeStruct)
                    .invoiceNumber("INV-2025-000001")
                    .amount(feeStruct.getTotalAmount())
                    .amountPaid(BigDecimal.ZERO)
                    .dueDate(LocalDate.of(2026, 2, 28))
                    .status("PENDING").build());
        }

        System.out.println("Sample data loaded successfully! (with new entities)");
    }
}
