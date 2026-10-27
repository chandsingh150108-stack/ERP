package com.studenterp.repository;

import com.studenterp.entity.Section;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SectionRepository extends JpaRepository<Section, Long> {
    List<Section> findByProgramIdAndSemester(Long programId, Integer semester);
    List<Section> findByAcademicYearId(Long academicYearId);
}
