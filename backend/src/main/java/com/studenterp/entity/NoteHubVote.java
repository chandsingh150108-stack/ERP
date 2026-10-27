package com.studenterp.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notehub_votes",
        uniqueConstraints = @UniqueConstraint(columnNames = {"note_id", "student_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoteHubVote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "note_id", nullable = false)
    @JsonIgnoreProperties({"uploader", "course", "subject", "voteCount", "createdAt", "updatedAt"})
    private NoteHubNote note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    @JsonIgnoreProperties({"program", "user", "rollNumber", "registrationNumber", "dateOfBirth", "gender", "email", "phone", "admissionDate", "currentSemester", "address", "city", "state", "postalCode", "status", "createdAt", "updatedAt"})
    private Student student;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}