package com.studenterp.repository;

import com.studenterp.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    List<Invoice> findByStudentIdOrderByDueDateAsc(Long studentId);
    List<Invoice> findByStatus(String status);
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
    long countByStatus(String status);
}
