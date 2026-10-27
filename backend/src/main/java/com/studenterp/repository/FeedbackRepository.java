package com.studenterp.repository;

import com.studenterp.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    List<Feedback> findByFacultyIdOrderBySubmittedAtDesc(Long facultyId);
    List<Feedback> findByCourseIdOrderBySubmittedAtDesc(Long courseId);
    List<Feedback> findByStudentIdOrderBySubmittedAtDesc(Long studentId);
    Boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);

    @Query("SELECT AVG(f.rating) FROM Feedback f WHERE f.faculty.id = :facultyId")
    Double findAvgRatingByFacultyId(@Param("facultyId") Long facultyId);

    @Query("SELECT AVG(f.rating) FROM Feedback f WHERE f.course.id = :courseId")
    Double findAvgRatingByCourseId(@Param("courseId") Long courseId);
}
