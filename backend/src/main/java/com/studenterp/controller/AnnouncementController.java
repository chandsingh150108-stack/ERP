package com.studenterp.controller;

import com.studenterp.entity.Announcement;
import com.studenterp.repository.AnnouncementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/announcements")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementRepository announcementRepository;

    @GetMapping
    public ResponseEntity<List<Announcement>> findAll() {
        return ResponseEntity.ok(announcementRepository.findAll());
    }

    @GetMapping("/recent")
    public ResponseEntity<List<Announcement>> findRecent() {
        return ResponseEntity.ok(announcementRepository.findByActiveTrue());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('FACULTY')")
    public ResponseEntity<Announcement> create(@RequestBody Announcement announcement) {
        return ResponseEntity.ok(announcementRepository.save(announcement));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        announcementRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
