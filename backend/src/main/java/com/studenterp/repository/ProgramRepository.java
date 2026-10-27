package com.studenterp.repository;

import com.studenterp.entity.Program;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ProgramRepository extends JpaRepository<Program, Long> {
    List<Program> findByDepartmentId(Long departmentId);
    Optional<Program> findByCode(String code);
    Boolean existsByCode(String code);
}
