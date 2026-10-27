package com.studenterp.repository;

import com.studenterp.entity.FeePayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeePaymentRepository extends JpaRepository<FeePayment, Long> {
    List<FeePayment> findByStudentIdOrderByPaymentDateDesc(Long studentId);
    List<FeePayment> findByInvoiceIdOrderByPaymentDateDesc(Long invoiceId);
    Optional<FeePayment> findByTransactionId(String transactionId);
}
