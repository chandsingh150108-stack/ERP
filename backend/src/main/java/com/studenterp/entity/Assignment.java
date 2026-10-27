package com.studenterp.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "assignments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Assignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 2000)
    private String description;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "course_id", nullable = false)
    @JsonIgnoreProperties({"department", "description", "credits", "courseType", "lectureHours", "tutorialHours", "practicalHours", "studyYear"})
    private Course course;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "section_id", nullable = false)
    @JsonIgnoreProperties({"program", "academicYear", "capacity"})
    private Section section;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "faculty_id", nullable = false)
    @JsonIgnoreProperties({"department", "user", "email", "phone", "designation", "specialization", "assignedSubjects", "joiningDate", "employmentStatus", "createdAt", "updatedAt"})
    private Faculty faculty;

    private LocalDate assignedDate;

    @Column(nullable = false)
    private LocalDate dueDate;

    private Integer maxMarks;

    @Column(length = 500)
    private String attachmentPath;

    @Column(length = 500)
    private String attachmentName;

    @Column(length = 50)
    @Builder.Default
    private String status = "PUBLISHED";

    @Column(length = 20)
    @Builder.Default
    private String assignmentType = "THEORY";

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (assignedDate == null) assignedDate = LocalDate.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
