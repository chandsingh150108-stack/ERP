package com.studenterp.repository;

import com.studenterp.entity.NoteHubVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface NoteHubVoteRepository extends JpaRepository<NoteHubVote, Long> {
    boolean existsByNoteIdAndStudentId(Long noteId, Long studentId);

    @Modifying
    @Transactional
    @Query("DELETE FROM NoteHubVote nv WHERE nv.note.id = :noteId AND nv.student.id = :studentId")
    void deleteByNoteIdAndStudentId(@Param("noteId") Long noteId, @Param("studentId") Long studentId);

    long countByNoteId(Long noteId);

    @Modifying
    @Transactional
    @Query("DELETE FROM NoteHubVote nv WHERE nv.note.id = :noteId")
    void deleteByNoteId(@Param("noteId") Long noteId);
}