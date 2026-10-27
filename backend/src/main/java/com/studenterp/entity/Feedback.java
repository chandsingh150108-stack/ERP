package com.studenterp.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "feedbacks",
        uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "course_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    @JsonIgnoreProperties({"program", "user", "rollNumber", "registrationNumber", "dateOfBirth", "gender", "email", "phone", "admissionDate", "currentSemester", "address", "city", "state", "postalCode", "status", "createdAt", "updatedAt"})
    private Student student;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "course_id", nullable = false)
    @JsonIgnoreProperties({"department", "description", "credits", "courseType", "lectureHours", "tutorialHours", "practicalHours", "studyYear"})
    private Course course;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "faculty_id", nullable = false)
    @JsonIgnoreProperties({"department", "user", "email", "phone", "designation", "specialization", "joiningDate", "employmentStatus", "createdAt", "updatedAt"})
    private Faculty faculty;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academic_year_id")
    private AcademicYear academicYear;

    @Column(nullable = false)
    private Integer rating;

    @Column(length = 1000)
    private String comments;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String category = "COURSE";

    @Column(nullable = false)
    @Builder.Default
    private Boolean isAnonymous = false;

    @Column(length = 20)
    @Builder.Default
    private String status = "SUBMITTED";

    @Column(updatable = false)
    private LocalDateTime submittedAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        submittedAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
