package com.studenterp.repository;

import com.studenterp.entity.ExamResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExamResultRepository extends JpaRepository<ExamResult, Long> {
    Optional<ExamResult> findByExamIdAndStudentId(Long examId, Long studentId);
    List<ExamResult> findByExamIdOrderByMarksObtainedDesc(Long examId);
    List<ExamResult> findByStudentIdOrderByCreatedAtDesc(Long studentId);
    boolean existsByExamIdAndStudentId(Long examId, Long studentId);
}
