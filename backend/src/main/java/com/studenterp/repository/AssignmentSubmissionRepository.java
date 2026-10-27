package com.studenterp.repository;

import com.studenterp.entity.AssignmentSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssignmentSubmissionRepository extends JpaRepository<AssignmentSubmission, Long> {
    Optional<AssignmentSubmission> findByAssignmentIdAndStudentId(Long assignmentId, Long studentId);
    List<AssignmentSubmission> findByAssignmentIdOrderBySubmittedAtDesc(Long assignmentId);
    List<AssignmentSubmission> findByStudentIdOrderBySubmittedAtDesc(Long studentId);
    boolean existsByAssignmentIdAndStudentId(Long assignmentId, Long studentId);
    long countByAssignmentId(Long assignmentId);
    long countByAssignmentIdAndStatus(Long assignmentId, String status);
}
