package com.studenterp.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "semesters")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Semester {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer semesterNumber;

    @Column(length = 50)
    private String name;

    @Column
    @Builder.Default
    private Boolean active = true;
}
