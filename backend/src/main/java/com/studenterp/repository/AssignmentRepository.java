package com.studenterp.repository;

import com.studenterp.entity.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    List<Assignment> findBySectionIdOrderByDueDateDesc(Long sectionId);
    List<Assignment> findByFacultyIdOrderByDueDateDesc(Long facultyId);
    List<Assignment> findByCourseIdOrderByDueDateDesc(Long courseId);
    List<Assignment> findBySectionIdAndStatusOrderByDueDateDesc(Long sectionId, String status);
    List<Assignment> findByFacultyIdAndStatusOrderByDueDateDesc(Long facultyId, String status);
}
