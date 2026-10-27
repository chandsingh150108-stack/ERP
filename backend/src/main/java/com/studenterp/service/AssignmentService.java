package com.studenterp.service;

import com.studenterp.entity.*;
import com.studenterp.exception.ResourceNotFoundException;
import com.studenterp.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final AssignmentSubmissionRepository submissionRepository;
    private final CourseRepository courseRepository;
    private final SectionRepository sectionRepository;
    private final FacultyRepository facultyRepository;
    private final StudentRepository studentRepository;
    private final StudentSectionRepository studentSectionRepository;

    @Value("${assignment.upload-dir:./uploads/assignments}")
    private String uploadDir;

    public List<Assignment> findAll() {
        return assignmentRepository.findAll();
    }

    public Assignment findById(Long id) {
        return assignmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment", "id", id));
    }

    public List<Assignment> findBySectionId(Long sectionId) {
        return assignmentRepository.findBySectionIdOrderByDueDateDesc(sectionId);
    }

    public List<Assignment> findByFacultyId(Long facultyId) {
        return assignmentRepository.findByFacultyIdOrderByDueDateDesc(facultyId);
    }

    public List<Assignment> findByStudentId(Long studentId) {
        List<StudentSection> enrollments = studentSectionRepository.findByStudentId(studentId);
        List<Assignment> assignments = new java.util.ArrayList<>();
        for (StudentSection ss : enrollments) {
            assignments.addAll(assignmentRepository.findBySectionIdOrderByDueDateDesc(ss.getSection().getId()));
        }
        return assignments;
    }

    public Assignment create(Assignment assignment, MultipartFile file, Long facultyId) {
        Course course = courseRepository.findById(assignment.getCourse().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", assignment.getCourse().getId()));
        Section section = sectionRepository.findById(assignment.getSection().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Section", "id", assignment.getSection().getId()));
        Faculty faculty = facultyRepository.findById(facultyId)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty", "id", facultyId));

        assignment.setCourse(course);
        assignment.setSection(section);
        assignment.setFaculty(faculty);

        if (file != null && !file.isEmpty()) {
            try {
                Path uploadPath = Paths.get(uploadDir);
                Files.createDirectories(uploadPath);
                String storedName = UUID.randomUUID() + "_" + file.getOriginalFilename();
                Path filePath = uploadPath.resolve(storedName);
                file.transferTo(filePath.toFile());
                assignment.setAttachmentPath(filePath.toString());
                assignment.setAttachmentName(file.getOriginalFilename());
            } catch (IOException e) {
                throw new RuntimeException("Failed to save attachment file", e);
            }
        }

        return assignmentRepository.save(assignment);
    }

    @Transactional
    public Assignment update(Long id, Assignment updated, MultipartFile file, Long facultyId) {
        Assignment existing = findById(id);
        if (updated.getTitle() != null) existing.setTitle(updated.getTitle());
        if (updated.getDescription() != null) existing.setDescription(updated.getDescription());
        if (updated.getDueDate() != null) existing.setDueDate(updated.getDueDate());
        if (updated.getMaxMarks() != null) existing.setMaxMarks(updated.getMaxMarks());
        if (updated.getStatus() != null) existing.setStatus(updated.getStatus());
        if (updated.getAssignmentType() != null) existing.setAssignmentType(updated.getAssignmentType());

        if (updated.getCourse() != null && updated.getCourse().getId() != null) {
            Course course = courseRepository.findById(updated.getCourse().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Course", "id", updated.getCourse().getId()));
            existing.setCourse(course);
        }
        if (updated.getSection() != null && updated.getSection().getId() != null) {
            Section section = sectionRepository.findById(updated.getSection().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Section", "id", updated.getSection().getId()));
            existing.setSection(section);
        }

        if (file != null && !file.isEmpty()) {
            try {
                Path uploadPath = Paths.get(uploadDir);
                Files.createDirectories(uploadPath);
                String storedName = UUID.randomUUID() + "_" + file.getOriginalFilename();
                Path filePath = uploadPath.resolve(storedName);
                file.transferTo(filePath.toFile());
                existing.setAttachmentPath(filePath.toString());
                existing.setAttachmentName(file.getOriginalFilename());
            } catch (IOException e) {
                throw new RuntimeException("Failed to save attachment file", e);
            }
        }

        return assignmentRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        Assignment assignment = findById(id);
        if (assignment.getAttachmentPath() != null) {
            try {
                Files.deleteIfExists(Paths.get(assignment.getAttachmentPath()));
            } catch (IOException ignored) {}
        }
        submissionRepository.deleteAll(submissionRepository.findByAssignmentIdOrderBySubmittedAtDesc(id));
        assignmentRepository.delete(assignment);
    }

    public AssignmentSubmission submit(Long assignmentId, Long studentId, MultipartFile file, String textSubmission) {
        Assignment assignment = findById(assignmentId);
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", studentId));

        AssignmentSubmission submission = submissionRepository
                .findByAssignmentIdAndStudentId(assignmentId, studentId)
                .orElse(AssignmentSubmission.builder()
                        .assignment(assignment)
                        .student(student)
                        .build());

        submission.setTextSubmission(textSubmission);

        if (file != null && !file.isEmpty()) {
            try {
                Path uploadPath = Paths.get(uploadDir, "submissions");
                Files.createDirectories(uploadPath);
                String storedName = UUID.randomUUID() + "_" + file.getOriginalFilename();
                Path filePath = uploadPath.resolve(storedName);
                file.transferTo(filePath.toFile());
                submission.setFilePath(filePath.toString());
                submission.setFileName(file.getOriginalFilename());
            } catch (IOException e) {
                throw new RuntimeException("Failed to save submission file", e);
            }
        }

        return submissionRepository.save(submission);
    }

    public AssignmentSubmission gradeSubmission(Long submissionId, Integer marks, String feedback) {
        AssignmentSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("AssignmentSubmission", "id", submissionId));
        submission.setMarksObtained(marks);
        submission.setFeedback(feedback);
        submission.setStatus("GRADED");
        return submissionRepository.save(submission);
    }

    public List<AssignmentSubmission> getSubmissions(Long assignmentId) {
        return submissionRepository.findByAssignmentIdOrderBySubmittedAtDesc(assignmentId);
    }

    public AssignmentSubmission getStudentSubmission(Long assignmentId, Long studentId) {
        return submissionRepository.findByAssignmentIdAndStudentId(assignmentId, studentId)
                .orElse(null);
    }
}
