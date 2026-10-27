package com.studenterp.repository;

import com.studenterp.entity.VaultSubject;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface VaultSubjectRepository extends JpaRepository<VaultSubject, Long> {
    List<VaultSubject> findByStudentId(Long studentId);
    List<VaultSubject> findByStudentIdAndStudyYear(Long studentId, Integer studyYear);
    List<VaultSubject> findByStudentIdAndYear(Long studentId, String year);
    Optional<VaultSubject> findByIdAndStudentId(Long id, Long studentId);
    boolean existsByStudentIdAndName(Long studentId, String name);
    boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);
}
