package com.studenterp.repository;

import com.studenterp.entity.Exam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamRepository extends JpaRepository<Exam, Long> {
    List<Exam> findBySectionIdOrderByExamDateAsc(Long sectionId);
    List<Exam> findByCourseIdOrderByExamDateAsc(Long courseId);
    List<Exam> findByStatusOrderByExamDateAsc(String status);
    List<Exam> findBySectionIdAndStatusOrderByExamDateAsc(Long sectionId, String status);
}
