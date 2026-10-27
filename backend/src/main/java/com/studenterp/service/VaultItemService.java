package com.studenterp.service;

import com.studenterp.entity.Student;
import com.studenterp.entity.VaultItem;
import com.studenterp.entity.VaultSubject;
import com.studenterp.exception.ResourceNotFoundException;
import com.studenterp.repository.StudentRepository;
import com.studenterp.repository.VaultItemRepository;
import com.studenterp.repository.VaultSubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
public class VaultItemService {

    private final VaultItemRepository vaultItemRepository;
    private final VaultSubjectRepository vaultSubjectRepository;
    private final StudentRepository studentRepository;

    @Value("${vault.upload-dir:./uploads/vault}")
    private String uploadDir;

    public List<VaultItem> findAllByStudent(Long studentId) {
        return vaultItemRepository.findByStudentId(studentId);
    }

    public List<VaultItem> findBySubject(Long studentId, Long subjectId) {
        return vaultItemRepository.findByStudentIdAndSubjectId(studentId, subjectId);
    }

    public List<VaultItem> findByCategory(Long studentId, String category) {
        return vaultItemRepository.findByStudentIdAndCategory(studentId, category);
    }

    public VaultItem findById(Long id, Long studentId) {
        return vaultItemRepository.findByIdAndStudentId(id, studentId)
                .orElseThrow(() -> new ResourceNotFoundException("VaultItem", "id", id));
    }

    public VaultItem create(MultipartFile file, String title, String description,
                            String category, Long subjectId, Long studentId) throws IOException {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", studentId));

        VaultSubject subject = null;
        if (subjectId != null) {
            subject = vaultSubjectRepository.findByIdAndStudentId(subjectId, studentId)
                    .orElseThrow(() -> new ResourceNotFoundException("VaultSubject", "id", subjectId));
        }

        Path studentDir = Paths.get(uploadDir, String.valueOf(studentId));
        Files.createDirectories(studentDir);

        String originalFilename = file.getOriginalFilename();
        String storedFilename = UUID.randomUUID() + "_" + originalFilename;
        Path filePath = studentDir.resolve(storedFilename);
        file.transferTo(filePath.toFile());

        VaultItem item = VaultItem.builder()
                .title(title)
                .description(description)
                .fileName(originalFilename)
                .filePath(filePath.toString())
                .fileSize(file.getSize())
                .fileType(file.getContentType())
                .category(category != null ? category : "other")
                .student(student)
                .subject(subject)
                .build();

        return vaultItemRepository.save(item);
    }

    public VaultItem update(Long id, String title, String description, String category,
                            Long subjectId, Long studentId) {
        VaultItem existing = findById(id, studentId);

        if (title != null) existing.setTitle(title);
        if (description != null) existing.setDescription(description);
        if (category != null) existing.setCategory(category);

        if (subjectId != null) {
            VaultSubject subject = vaultSubjectRepository.findByIdAndStudentId(subjectId, studentId)
                    .orElseThrow(() -> new ResourceNotFoundException("VaultSubject", "id", subjectId));
            existing.setSubject(subject);
        } else if (subjectId == null) {
            existing.setSubject(null);
        }

        return vaultItemRepository.save(existing);
    }

    public void delete(Long id, Long studentId) {
        VaultItem item = findById(id, studentId);
        try {
            Path filePath = Paths.get(item.getFilePath());
            Files.deleteIfExists(filePath);
        } catch (IOException ignored) {
        }
        vaultItemRepository.delete(item);
    }

    public byte[] downloadFile(Long id, Long studentId) throws IOException {
        VaultItem item = findById(id, studentId);
        Path filePath = Paths.get(item.getFilePath());
        return Files.readAllBytes(filePath);
    }

    public String getFileContentType(Long id, Long studentId) {
        VaultItem item = findById(id, studentId);
        return item.getFileType();
    }

    public String getFileName(Long id, Long studentId) {
        VaultItem item = findById(id, studentId);
        return item.getFileName();
    }
}
