package com.studenterp.service;

import com.studenterp.entity.Course;
import com.studenterp.entity.NoteHubSubject;
import com.studenterp.exception.ResourceNotFoundException;
import com.studenterp.repository.CourseRepository;
import com.studenterp.repository.NoteHubNoteRepository;
import com.studenterp.repository.NoteHubSubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NoteHubSubjectService {

    private final NoteHubSubjectRepository noteHubSubjectRepository;
    private final CourseRepository courseRepository;
    private final NoteHubNoteRepository noteHubNoteRepository;

    public NoteHubSubject create(NoteHubSubject subject) {
        if (subject.getStudyYear() == null || subject.getStudyYear() < 1 || subject.getStudyYear() > 4) {
            throw new IllegalArgumentException("Study year must be between 1 and 4");
        }
        Course course = courseRepository.findById(subject.getCourse().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", subject.getCourse().getId()));
        subject.setCourse(course);
        if (noteHubSubjectRepository.existsByCourseIdAndCode(course.getId(), subject.getCode())) {
            throw new IllegalArgumentException("Subject with this code already exists for the course");
        }
        return noteHubSubjectRepository.save(subject);
    }

    public NoteHubSubject update(Long id, NoteHubSubject updated) {
        NoteHubSubject existing = noteHubSubjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("NoteHubSubject", "id", id));
        if (updated.getStudyYear() != null) {
            if (updated.getStudyYear() < 1 || updated.getStudyYear() > 4) {
                throw new IllegalArgumentException("Study year must be between 1 and 4");
            }
            existing.setStudyYear(updated.getStudyYear());
        }
        if (updated.getName() != null) {
            existing.setName(updated.getName());
        }
        if (updated.getCode() != null && !updated.getCode().equals(existing.getCode())) {
            if (noteHubSubjectRepository.existsByCourseIdAndCode(existing.getCourse().getId(), updated.getCode())) {
                throw new IllegalArgumentException("Subject with this code already exists for the course");
            }
            existing.setCode(updated.getCode());
        }
        return noteHubSubjectRepository.save(existing);
    }

    public void delete(Long id) {
        NoteHubSubject subject = noteHubSubjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("NoteHubSubject", "id", id));
        if (noteHubNoteRepository.existsBySubjectId(id)) {
            throw new IllegalArgumentException("Cannot delete subject referenced by notes");
        }
        noteHubSubjectRepository.delete(subject);
    }

    public NoteHubSubject findById(Long id) {
        return noteHubSubjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("NoteHubSubject", "id", id));
    }

    public List<NoteHubSubject> findAll(Integer year, Long courseId) {
        return findAll(year, courseId, null);
    }

    public List<NoteHubSubject> findAll(Integer year, Long courseId, String department) {
        String cleanDept = (department != null && !department.isBlank()) ? department.trim() : null;
        if (cleanDept != null || year != null || courseId != null) {
            return noteHubSubjectRepository.findFiltered(year, courseId, cleanDept);
        }
        return noteHubSubjectRepository.findAll();
    }
}