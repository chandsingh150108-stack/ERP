package com.studenterp.repository;

import com.studenterp.entity.Student;
import com.studenterp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByRollNumber(String rollNumber);
    Optional<Student> findByRegistrationNumber(String registrationNumber);
    Boolean existsByRollNumber(String rollNumber);
    List<Student> findByProgramId(Long programId);
    List<Student> findByCurrentSemester(Integer semester);
    Optional<Student> findByUser(User user);

    @Query("SELECT s FROM Student s WHERE LOWER(s.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(s.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(s.rollNumber) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(s.email) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Student> search(@Param("keyword") String keyword);
}
