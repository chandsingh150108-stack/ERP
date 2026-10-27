package com.studenterp.repository;

import com.studenterp.entity.Backlog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BacklogRepository extends JpaRepository<Backlog, Long> {
    List<Backlog> findByStudentId(Long studentId);
}
