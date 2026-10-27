package com.studenterp.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notehub_notes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoteHubNote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 10)
    private String noteType;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false, length = 255)
    private String fileName;

    @Column(nullable = false, length = 500)
    private String filePath;

    @Column(nullable = false)
    private Long fileSize;

    @Column(nullable = false, length = 100)
    private String fileType;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "uploader_id", nullable = false)
    @JsonIgnoreProperties({"program", "dateOfBirth", "gender", "admissionDate", "currentSemester", "address", "city", "state", "postalCode", "status", "createdAt", "updatedAt"})
    private Student uploader;

    private Integer studyYear;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "course_id")
    @JsonIgnoreProperties({"description", "credits", "courseType", "lectureHours", "tutorialHours", "practicalHours"})
    private Course course;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "subject_id")
    @JsonIgnoreProperties({"course", "createdAt", "updatedAt"})
    private NoteHubSubject subject;

    @Column(length = 10)
    private String unit;

    @Column(nullable = false)
    @Builder.Default
    private Integer voteCount = 0;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}