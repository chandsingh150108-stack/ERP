package com.studenterp.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "programs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Program {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 50)
    private String degreeType;

    @Column(nullable = false)
    private Integer durationYears;

    @Column(nullable = false)
    private Integer totalCredits;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;
}
