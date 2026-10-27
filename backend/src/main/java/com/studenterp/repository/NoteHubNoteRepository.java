package com.studenterp.repository;

import com.studenterp.entity.NoteHubNote;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface NoteHubNoteRepository extends JpaRepository<NoteHubNote, Long> {
    Page<NoteHubNote> findByNoteTypeOrderByVoteCountDescCreatedAtDesc(String noteType, Pageable pageable);

    Page<NoteHubNote> findByNoteTypeAndStudyYearAndCourseIdAndSubjectIdAndUnitOrderByVoteCountDescCreatedAtDesc(
            String noteType,
            Integer studyYear,
            Long courseId,
            Long subjectId,
            String unit,
            Pageable pageable);

    @Query("SELECT n FROM NoteHubNote n WHERE n.noteType = 'EXTRA' AND (LOWER(n.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(n.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<NoteHubNote> searchExtra(@Param("keyword") String keyword, Pageable pageable);

    @Query(value = "SELECT n FROM NoteHubNote n WHERE n.noteType = 'COURSE' " +
                   "AND (:studyYear IS NULL OR n.studyYear = :studyYear) " +
                   "AND (:courseId IS NULL OR n.course.id = :courseId) " +
                   "AND (:subjectId IS NULL OR n.subject.id = :subjectId) " +
                   "AND (:unit IS NULL OR :unit = '' OR n.unit = :unit) " +
                    "AND (:department IS NULL OR :department = '' OR " +
                   " UPPER(n.course.department.code) = UPPER(:department) OR " +
                   " (n.studyYear >= 2 AND (n.subject.code LIKE 'HS%' OR n.subject.code LIKE 'SH%' OR n.subject.code LIKE 'MD%' OR n.subject.code LIKE 'MM%')) " +
                   ") " +
                   "ORDER BY n.voteCount DESC, n.createdAt DESC",
           countQuery = "SELECT COUNT(n) FROM NoteHubNote n WHERE n.noteType = 'COURSE' " +
                   "AND (:studyYear IS NULL OR n.studyYear = :studyYear) " +
                   "AND (:courseId IS NULL OR n.course.id = :courseId) " +
                   "AND (:subjectId IS NULL OR n.subject.id = :subjectId) " +
                   "AND (:unit IS NULL OR :unit = '' OR n.unit = :unit) " +
                   "AND (:department IS NULL OR :department = '' OR " +
                   " UPPER(n.course.department.code) = UPPER(:department) OR " +
                   " (n.studyYear >= 2 AND (n.subject.code LIKE 'HS%' OR n.subject.code LIKE 'SH%' OR n.subject.code LIKE 'MD%' OR n.subject.code LIKE 'MM%')) " +
                   ")")
    Page<NoteHubNote> findFilteredCourseNotes(
            @Param("studyYear") Integer studyYear,
            @Param("courseId") Long courseId,
            @Param("subjectId") Long subjectId,
            @Param("unit") String unit,
            @Param("department") String department,
            Pageable pageable);

    boolean existsBySubjectId(Long subjectId);
}