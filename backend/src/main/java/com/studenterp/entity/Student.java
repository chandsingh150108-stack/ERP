package com.studenterp.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "students")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties({"passwordHash", "active", "createdAt", "updatedAt"})
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "program_id", nullable = false)
    private Program program;

    @Column(length = 100)
    private String course;

    @Column(name = "academic_year", length = 20)
    @Builder.Default
    private String year = "FY";

    @Column
    @Builder.Default
    private Integer studyYear = 1;

    @Column(nullable = false, unique = true, length = 30)
    private String rollNumber;

    @Column(unique = true, length = 30)
    private String registrationNumber;

    @Column(nullable = false, length = 50)
    private String firstName;

    @Column(nullable = false, length = 50)
    private String lastName;

    private LocalDate dateOfBirth;

    @Column(length = 10)
    private String gender;

    @Column(length = 100)
    private String email;

    @Column(length = 20)
    private String phone;

    private LocalDate admissionDate;

    @Column
    @Builder.Default
    private Integer currentSemester = 1;

    @Column(length = 200)
    private String address;

    @Column(length = 50)
    private String city;

    @Column(length = 50)
    private String state;

    @Column(length = 10)
    private String postalCode;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "ACTIVE";

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public void syncAcademicFields() {
        if (year != null && !year.isBlank()) {
            String y = year.trim().toUpperCase();
            if (y.startsWith("FY") || y.equals("1")) {
                this.studyYear = 1;
                this.year = "FY";
                if (this.currentSemester == null || this.currentSemester < 1 || this.currentSemester > 2) {
                    this.currentSemester = 1;
                }
            } else if (y.startsWith("SY") || y.equals("2")) {
                this.studyYear = 2;
                this.year = "SY";
                if (this.currentSemester == null || this.currentSemester < 3 || this.currentSemester > 4) {
                    this.currentSemester = 3;
                }
            } else if (y.startsWith("TY") || y.equals("3")) {
                this.studyYear = 3;
                this.year = "TY";
                if (this.currentSemester == null || this.currentSemester < 5 || this.currentSemester > 6) {
                    this.currentSemester = 5;
                }
            } else if (y.startsWith("FINAL") || y.startsWith("BTECH") || y.equals("4")) {
                this.studyYear = 4;
                this.year = "FINAL";
                if (this.currentSemester == null || this.currentSemester < 7) {
                    this.currentSemester = 7;
                }
            }
        } else if (studyYear != null) {
            if (studyYear == 1) { this.year = "FY"; if (currentSemester == null) currentSemester = 1; }
            else if (studyYear == 2) { this.year = "SY"; if (currentSemester == null) currentSemester = 3; }
            else if (studyYear == 3) { this.year = "TY"; if (currentSemester == null) currentSemester = 5; }
            else if (studyYear >= 4) { this.year = "FINAL"; if (currentSemester == null) currentSemester = 7; }
        } else if (currentSemester != null) {
            this.studyYear = (currentSemester + 1) / 2;
            if (this.studyYear == 1) this.year = "FY";
            else if (this.studyYear == 2) this.year = "SY";
            else if (this.studyYear == 3) this.year = "TY";
            else this.year = "FINAL";
        } else {
            this.studyYear = 1;
            this.year = "FY";
            this.currentSemester = 1;
        }

        if (program != null) {
            if (this.course == null || this.course.isBlank()) {
                String pCode = program.getCode() != null ? program.getCode().toUpperCase() : "";
                if (pCode.contains("AIML")) {
                    this.course = "CSE AIML";
                } else if (pCode.contains("CSE")) {
                    this.course = "CSE";
                } else if (pCode.contains("IT")) {
                    this.course = "IT";
                } else if (pCode.contains("AIDS")) {
                    this.course = "AIDS";
                } else if (pCode.contains("ENTC")) {
                    this.course = "ENTC";
                } else if (pCode.contains("MECH")) {
                    this.course = "MECH";
                } else if (pCode.contains("CHEM")) {
                    this.course = "CHEM";
                } else if (pCode.contains("INST")) {
                    this.course = "INST";
                } else {
                    this.course = program.getName();
                }
            }
        }
    }

    @PrePersist
    protected void onCreate() {
        syncAcademicFields();
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        syncAcademicFields();
        updatedAt = LocalDateTime.now();
    }
}
