package com.studenterp.controller;

import com.studenterp.entity.*;
import com.studenterp.exception.ResourceNotFoundException;
import com.studenterp.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/certificates")
@RequiredArgsConstructor
public class CertificateController {

    private final CertificateRepository certificateRepository;
    private final StudentRepository studentRepository;
    private final ProgramRepository programRepository;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<Certificate>> list(Authentication auth) {
        String username = auth.getName();
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) return ResponseEntity.ok(new ArrayList<>());

        Student student = studentRepository.findByUser(user).orElse(null);
        if (student != null) {
            return ResponseEntity.ok(certificateRepository.findByStudentIdOrderByIssueDateDesc(student.getId()));
        }

        return ResponseEntity.ok(certificateRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Certificate> findById(@PathVariable Long id) {
        return ResponseEntity.ok(certificateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Certificate", "id", id)));
    }

    @GetMapping("/verify/{number}")
    public ResponseEntity<Map<String, Object>> verify(@PathVariable String number) {
        Certificate cert = certificateRepository.findByCertificateNumber(number).orElse(null);
        Map<String, Object> result = new HashMap<>();
        if (cert != null) {
            result.put("valid", true);
            result.put("certificateNumber", cert.getCertificateNumber());
            result.put("studentName", cert.getStudent().getFirstName() + " " + cert.getStudent().getLastName());
            result.put("rollNumber", cert.getStudent().getRollNumber());
            result.put("certificateType", cert.getCertificateType());
            result.put("issueDate", cert.getIssueDate().toString());
            result.put("status", cert.getStatus());
        } else {
            result.put("valid", false);
            result.put("message", "Certificate not found");
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Certificate> issue(@RequestBody Map<String, Object> payload) {
        Long studentId = Long.valueOf(payload.get("studentId").toString());
        String certType = (String) payload.get("certificateType");
        String purpose = (String) payload.getOrDefault("purpose", "");

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", studentId));

        String certNumber = "VIT-" + LocalDate.now().getYear() + "-" +
                String.format("%06d", System.nanoTime() % 1000000);

        Certificate cert = Certificate.builder()
                .student(student)
                .program(student.getProgram())
                .certificateType(certType)
                .certificateNumber(certNumber)
                .purpose(purpose)
                .issueDate(LocalDate.now())
                .status("ISSUED")
                .build();

        return ResponseEntity.ok(certificateRepository.save(cert));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> revoke(@PathVariable Long id) {
        Certificate cert = certificateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Certificate", "id", id));
        cert.setStatus("REVOKED");
        certificateRepository.save(cert);
        return ResponseEntity.ok().build();
    }
}
