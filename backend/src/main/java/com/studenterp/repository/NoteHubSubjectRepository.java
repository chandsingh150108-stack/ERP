package com.studenterp.repository;

import com.studenterp.entity.NoteHubSubject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface NoteHubSubjectRepository extends JpaRepository<NoteHubSubject, Long> {
    List<NoteHubSubject> findByStudyYear(Integer studyYear);
    List<NoteHubSubject> findByCourseId(Long courseId);
    List<NoteHubSubject> findByCourseIdAndStudyYear(Long courseId, Integer studyYear);
    Optional<NoteHubSubject> findByCode(String code);
    @Query("SELECT COUNT(n) > 0 FROM NoteHubSubject n WHERE n.course.id = :courseId AND n.code = :code")
    boolean existsByCourseIdAndCode(@Param("courseId") Long courseId, @Param("code") String code);

    @Query("SELECT s FROM NoteHubSubject s LEFT JOIN FETCH s.course c LEFT JOIN FETCH c.department WHERE " +
           "(:year IS NULL OR s.studyYear = :year) AND " +
           "(:courseId IS NULL OR s.course.id = :courseId) AND " +
           "(:department IS NULL OR :department = '' OR " +
           " UPPER(s.course.department.code) = UPPER(:department) OR " +
           " (s.studyYear >= 2 AND (s.code LIKE 'HS%' OR s.code LIKE 'SH%' OR s.code LIKE 'MD%' OR s.code LIKE 'MM%')) " +
           ") " +
           "ORDER BY s.code ASC")
    List<NoteHubSubject> findFiltered(
            @Param("year") Integer year,
            @Param("courseId") Long courseId,
            @Param("department") String department);

    @Query("SELECT s FROM NoteHubSubject s LEFT JOIN FETCH s.course c LEFT JOIN FETCH c.department WHERE " +
           "(s.studyYear = 1 OR UPPER(s.course.department.code) = UPPER(:department) OR (s.studyYear >= 2 AND s.code LIKE 'MM%')) " +
           "ORDER BY s.studyYear ASC, s.code ASC")
    List<NoteHubSubject> findForStudentCourse(@Param("department") String department);
}