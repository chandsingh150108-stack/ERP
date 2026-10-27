package com.studenterp.service;

import com.studenterp.entity.Course;
import com.studenterp.entity.NoteHubNote;
import com.studenterp.entity.NoteHubSubject;
import com.studenterp.entity.Student;
import com.studenterp.exception.ResourceNotFoundException;
import com.studenterp.repository.CourseRepository;
import com.studenterp.repository.NoteHubNoteRepository;
import com.studenterp.repository.NoteHubSubjectRepository;
import com.studenterp.repository.StudentRepository;
import com.studenterp.repository.NoteHubVoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NoteHubService {

    private final NoteHubNoteRepository noteHubNoteRepository;
    private final NoteHubSubjectRepository noteHubSubjectRepository;
    private final CourseRepository courseRepository;
    private final StudentRepository studentRepository;
    private final NoteHubVoteRepository noteHubVoteRepository;

    @Value("${notehub.upload-dir:./uploads/notehub}")
    private String uploadDir;

    public NoteHubNote create(MultipartFile file, String title, String description,
                              String noteType, Integer studyYear, Long courseId,
                              Long subjectId, String unit, Long uploaderStudentId) throws IOException {
        Student uploader = studentRepository.findById(uploaderStudentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", uploaderStudentId));

        validatePdf(file);

        Course course = null;
        NoteHubSubject subject = null;

        if ("COURSE".equals(noteType)) {
            if (studyYear == null || studyYear < 1 || studyYear > 4) {
                throw new IllegalArgumentException("Study year must be between 1 and 4 for COURSE notes");
            }
            if (courseId == null || subjectId == null || unit == null || unit.isBlank()) {
                throw new IllegalArgumentException("Course, subject, and unit are required for COURSE notes");
            }
            course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));
            subject = noteHubSubjectRepository.findById(subjectId)
                    .orElseThrow(() -> new ResourceNotFoundException("NoteHubSubject", "id", subjectId));

            if (!subject.getCourse().getId().equals(course.getId())) {
                throw new IllegalArgumentException("Subject does not belong to the specified course");
            }
            if (!subject.getStudyYear().equals(studyYear)) {
                throw new IllegalArgumentException("Subject study year does not match the specified year");
            }
        }

        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(uploadPath);

        String originalFilename = file.getOriginalFilename();
        String storedFilename = UUID.randomUUID() + "_" + originalFilename;
        Path filePath = uploadPath.resolve(storedFilename);
        Files.copy(file.getInputStream(), filePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

        NoteHubNote note = NoteHubNote.builder()
                .noteType(noteType)
                .title(title)
                .description(description)
                .fileName(originalFilename)
                .filePath(filePath.toString())
                .fileSize(file.getSize())
                .fileType(file.getContentType())
                .uploader(uploader)
                .studyYear("COURSE".equals(noteType) ? studyYear : null)
                .course("COURSE".equals(noteType) ? course : null)
                .subject("COURSE".equals(noteType) ? subject : null)
                .unit("COURSE".equals(noteType) ? unit : null)
                .voteCount(0)
                .build();

        return noteHubNoteRepository.save(note);
    }

    public Page<NoteHubNote> findCourseNotes(Integer year, Long courseId, Long subjectId, String unit, int page, int size) {
        return findCourseNotes(year, courseId, subjectId, unit, null, page, size);
    }

    public Page<NoteHubNote> findCourseNotes(Integer year, Long courseId, Long subjectId, String unit, String department, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "voteCount", "createdAt"));
        String cleanUnit = (unit != null && !unit.isBlank()) ? unit.trim() : null;
        String cleanDept = (department != null && !department.isBlank()) ? department.trim() : null;
        return noteHubNoteRepository.findFilteredCourseNotes(year, courseId, subjectId, cleanUnit, cleanDept, pageable);
    }

    public Page<NoteHubNote> searchExtraNotes(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "voteCount", "createdAt"));
        return noteHubNoteRepository.searchExtra(keyword, pageable);
    }

    public NoteHubNote findById(Long id) {
        return noteHubNoteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("NoteHubNote", "id", id));
    }

    public void delete(Long noteId, Long callerStudentId, boolean isAdminOrFaculty) {
        NoteHubNote note = findById(noteId);
        boolean isOwner = callerStudentId != null && note.getUploader().getId().equals(callerStudentId);
        if (!isOwner && !isAdminOrFaculty) {
            throw new SecurityException("You can only delete your own notes");
        }
        noteHubVoteRepository.deleteByNoteId(noteId);
        try {
            Path filePath = Paths.get(note.getFilePath());
            Files.deleteIfExists(filePath);
        } catch (IOException ignored) {
        }
        noteHubNoteRepository.delete(note);
    }

    public byte[] downloadFile(Long id) throws IOException {
        NoteHubNote note = findById(id);
        Path filePath = Paths.get(note.getFilePath());
        return Files.readAllBytes(filePath);
    }

    public String getFileContentType(Long id) {
        NoteHubNote note = findById(id);
        return note.getFileType();
    }

    public String getFileName(Long id) {
        NoteHubNote note = findById(id);
        return note.getFileName();
    }

    private void validatePdf(MultipartFile file) {
        String contentType = file.getContentType();
        String originalFilename = file.getOriginalFilename();
        if (contentType == null || !"application/pdf".equals(contentType)) {
            throw new IllegalArgumentException("Only PDF files are allowed");
        }
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".pdf")) {
            throw new IllegalArgumentException("Only PDF files are allowed");
        }
    }
}