package com.studenterp.repository;

import com.studenterp.entity.VaultItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface VaultItemRepository extends JpaRepository<VaultItem, Long> {
    List<VaultItem> findByStudentId(Long studentId);
    List<VaultItem> findBySubjectId(Long subjectId);
    List<VaultItem> findByStudentIdAndCategory(Long studentId, String category);
    List<VaultItem> findByStudentIdAndSubjectId(Long studentId, Long subjectId);
    Optional<VaultItem> findByIdAndStudentId(Long id, Long studentId);
}
