package com.studenterp.repository;

import com.studenterp.entity.FacultyCourse;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FacultyCourseRepository extends JpaRepository<FacultyCourse, Long> {
    List<FacultyCourse> findByFacultyId(Long facultyId);
    List<FacultyCourse> findByCourseId(Long courseId);
    List<FacultyCourse> findBySectionId(Long sectionId);
}
