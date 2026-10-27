package com.studenterp.repository;

import com.studenterp.entity.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CertificateRepository extends JpaRepository<Certificate, Long> {
    List<Certificate> findByStudentIdOrderByIssueDateDesc(Long studentId);
    Optional<Certificate> findByCertificateNumber(String certificateNumber);
    boolean existsByCertificateNumber(String certificateNumber);
    List<Certificate> findByStatusAndStudentIdOrderByIssueDateDesc(String status, Long studentId);
}
