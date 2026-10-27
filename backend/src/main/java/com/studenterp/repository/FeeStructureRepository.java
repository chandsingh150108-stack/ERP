package com.studenterp.repository;

import com.studenterp.entity.FeeStructure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeeStructureRepository extends JpaRepository<FeeStructure, Long> {
    List<FeeStructure> findByProgramIdAndStatus(Long programId, String status);
    Optional<FeeStructure> findByProgramIdAndAcademicYearIdAndFeeCategory(Long programId, Long academicYearId, String feeCategory);
}
