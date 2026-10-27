package com.studenterp.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "invoices")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "student_id", nullable = false)
    @JsonIgnoreProperties({"program", "user", "rollNumber", "registrationNumber", "dateOfBirth", "gender", "email", "phone", "admissionDate", "currentSemester", "address", "city", "state", "postalCode", "status", "createdAt", "updatedAt"})
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fee_structure_id", nullable = false)
    private FeeStructure feeStructure;

    @Column(nullable = false, unique = true, length = 50)
    private String invoiceNumber;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal amountPaid = BigDecimal.ZERO;

    private LocalDate dueDate;

    private LocalDate paidDate;

    @Column(length = 20)
    @Builder.Default
    private String status = "PENDING";

    @Column(length = 20)
    @Builder.Default
    private String paymentMode = "ONLINE";

    @Column(length = 200)
    private String remarks;

    @OneToMany(mappedBy = "invoice", cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("invoice")
    private List<FeePayment> payments = new ArrayList<>();

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        updateStatus();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        updateStatus();
    }

    private void updateStatus() {
        if (amountPaid.compareTo(amount) >= 0) {
            status = "PAID";
            if (paidDate == null) paidDate = LocalDate.now();
        } else if (amountPaid.compareTo(BigDecimal.ZERO) > 0) {
            status = "PARTIAL";
        } else if (dueDate != null && LocalDate.now().isAfter(dueDate)) {
            status = "OVERDUE";
        }
    }
}
