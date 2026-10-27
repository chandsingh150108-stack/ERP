package com.studenterp.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "vault_subjects",
       uniqueConstraints = {
           @UniqueConstraint(columnNames = {"student_id", "name"})
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VaultSubject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(length = 7)
    private String color;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    @JsonIgnoreProperties({"program", "user", "rollNumber", "registrationNumber", "dateOfBirth", "gender", "email", "phone", "admissionDate", "currentSemester", "address", "city", "state", "postalCode", "status", "createdAt", "updatedAt"})
    private Student student;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "course_id")
    @JsonIgnoreProperties({"department", "description", "credits", "courseType", "lectureHours", "tutorialHours", "practicalHours"})
    private Course course;

    @Column(name = "academic_year", length = 20)
    private String year;

    @Column
    private Integer studyYear;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public void syncAcademicYear() {
        if (studyYear == null && course != null && course.getStudyYear() != null) {
            studyYear = course.getStudyYear();
        }
        if (year == null && studyYear != null) {
            year = studyYear == 1 ? "FY" : (studyYear == 2 ? "SY" : (studyYear == 3 ? "TY" : "FINAL"));
        } else if (studyYear == null && year != null) {
            String y = year.trim().toUpperCase();
            if (y.startsWith("FY")) studyYear = 1;
            else if (y.startsWith("SY")) studyYear = 2;
            else if (y.startsWith("TY")) studyYear = 3;
            else studyYear = 4;
        }
    }

    @PrePersist
    protected void onCreate() {
        syncAcademicYear();
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        syncAcademicYear();
        updatedAt = LocalDateTime.now();
    }
}
