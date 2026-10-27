package com.studenterp.service;

import com.studenterp.entity.NoteHubNote;
import com.studenterp.entity.NoteHubVote;
import com.studenterp.entity.Student;
import com.studenterp.exception.ResourceNotFoundException;
import com.studenterp.repository.NoteHubNoteRepository;
import com.studenterp.repository.NoteHubVoteRepository;
import com.studenterp.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NoteHubVoteService {

    private final NoteHubVoteRepository noteHubVoteRepository;
    private final NoteHubNoteRepository noteHubNoteRepository;
    private final StudentRepository studentRepository;

    @Transactional
    public Map<String, Object> toggle(Long noteId, Long studentId) {
        NoteHubNote note = noteHubNoteRepository.findById(noteId)
                .orElseThrow(() -> new ResourceNotFoundException("NoteHubNote", "id", noteId));
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", studentId));

        boolean exists = noteHubVoteRepository.existsByNoteIdAndStudentId(noteId, studentId);
        if (exists) {
            noteHubVoteRepository.deleteByNoteIdAndStudentId(noteId, studentId);
            note.setVoteCount(Math.max(0, note.getVoteCount() - 1));
            noteHubNoteRepository.save(note);
            return Map.of("voted", false, "voteCount", note.getVoteCount());
        } else {
            NoteHubVote vote = NoteHubVote.builder()
                    .note(note)
                    .student(student)
                    .build();
            noteHubVoteRepository.save(vote);
            note.setVoteCount(note.getVoteCount() + 1);
            noteHubNoteRepository.save(note);
            return Map.of("voted", true, "voteCount", note.getVoteCount());
        }
    }
}