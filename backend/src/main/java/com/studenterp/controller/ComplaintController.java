package com.studenterp.controller;

import com.studenterp.entity.Complaint;
import com.studenterp.repository.ComplaintRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/complaints")
@RequiredArgsConstructor
public class ComplaintController {

    private final ComplaintRepository complaintRepository;

    @GetMapping
    public ResponseEntity<List<Complaint>> findAll() {
        return ResponseEntity.ok(complaintRepository.findAll());
    }

    @PostMapping
    public ResponseEntity<Complaint> create(@RequestBody Complaint complaint) {
        complaint.setStatus("OPEN");
        return ResponseEntity.ok(complaintRepository.save(complaint));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Complaint> updateStatus(@PathVariable Long id,
                                                   @RequestParam String status,
                                                   @RequestParam(required = false) String resolution) {
        Complaint complaint = complaintRepository.findById(id).orElseThrow();
        complaint.setStatus(status);
        if (resolution != null) complaint.setResolution(resolution);
        if ("RESOLVED".equals(status) || "CLOSED".equals(status)) {
            complaint.setResolvedDate(LocalDateTime.now());
        }
        return ResponseEntity.ok(complaintRepository.save(complaint));
    }
}
