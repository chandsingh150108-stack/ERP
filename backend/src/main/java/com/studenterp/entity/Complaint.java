package com.studenterp.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "complaints")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Complaint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String subject;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "OPEN";

    private LocalDateTime submissionDate;
    private LocalDateTime resolvedDate;

    @Column(length = 500)
    private String resolution;

    @PrePersist
    protected void onCreate() {
        submissionDate = LocalDateTime.now();
    }
}
