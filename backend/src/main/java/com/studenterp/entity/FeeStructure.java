package com.studenterp.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "fee_structures")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeeStructure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "program_id", nullable = false)
    @JsonIgnoreProperties({"department", "degreeType", "durationYears", "totalCredits", "active"})
    private Program program;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academic_year_id")
    private AcademicYear academicYear;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal tuitionFee;

    @Column(precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal hostelFee = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal examinationFee = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal libraryFee = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal otherFees = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    private LocalDate dueDate;

    @Column(length = 20)
    @Builder.Default
    private String status = "ACTIVE";

    @Column(length = 50)
    @Builder.Default
    private String feeCategory = "REGULAR";

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        calculateTotal();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        calculateTotal();
    }

    private void calculateTotal() {
        totalAmount = tuitionFee
                .add(hostelFee != null ? hostelFee : BigDecimal.ZERO)
                .add(examinationFee != null ? examinationFee : BigDecimal.ZERO)
                .add(libraryFee != null ? libraryFee : BigDecimal.ZERO)
                .add(otherFees != null ? otherFees : BigDecimal.ZERO);
    }
}
