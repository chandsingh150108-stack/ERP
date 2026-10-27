package com.studenterp.repository;

import com.studenterp.entity.StudentSection;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StudentSectionRepository extends JpaRepository<StudentSection, Long> {
    List<StudentSection> findByStudentId(Long studentId);
    List<StudentSection> findBySectionId(Long sectionId);
}
