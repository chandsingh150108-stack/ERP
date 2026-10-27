package com.studenterp.repository;

import com.studenterp.entity.Faculty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface FacultyRepository extends JpaRepository<Faculty, Long> {
    Optional<Faculty> findByEmployeeNumber(String employeeNumber);
    Boolean existsByEmployeeNumber(String employeeNumber);
    List<Faculty> findByDepartmentId(Long departmentId);

    @Query("SELECT f FROM Faculty f WHERE LOWER(f.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(f.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(f.employeeNumber) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(f.email) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Faculty> search(@Param("keyword") String keyword);

    @Query("SELECT f FROM Faculty f WHERE f.user.username = :username")
    Optional<Faculty> findByUsername(@Param("username") String username);
}
