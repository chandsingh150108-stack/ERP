package com.studenterp.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "fee_payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeePayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    @JsonIgnoreProperties({"student", "feeStructure", "payments", "createdAt", "updatedAt"})
    private Invoice invoice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    @JsonIgnoreProperties({"program", "user", "rollNumber", "registrationNumber", "dateOfBirth", "gender", "email", "phone", "admissionDate", "currentSemester", "address", "city", "state", "postalCode", "status", "createdAt", "updatedAt"})
    private Student student;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDate paymentDate;

    @Column(length = 20)
    private String paymentMode;

    @Column(length = 100)
    private String transactionId;

    @Column(nullable = false, unique = true, length = 50)
    private String receiptNumber;

    @Column(length = 20)
    @Builder.Default
    private String status = "SUCCESS";

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (paymentDate == null) paymentDate = LocalDate.now();
        if (receiptNumber == null) receiptNumber = "RCP-" + System.nanoTime() % 10000000;
    }
}
