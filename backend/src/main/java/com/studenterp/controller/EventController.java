package com.studenterp.controller;

import com.studenterp.entity.Event;
import com.studenterp.exception.ResourceNotFoundException;
import com.studenterp.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventRepository eventRepository;

    @GetMapping
    public ResponseEntity<List<Event>> findAll() {
        return ResponseEntity.ok(eventRepository.findAll());
    }

    @GetMapping("/active")
    public ResponseEntity<List<Event>> findActive() {
        return ResponseEntity.ok(eventRepository.findByActiveTrue());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Event> create(@RequestBody Event event) {
        return ResponseEntity.ok(eventRepository.save(event));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Event> update(@PathVariable Long id, @RequestBody Event updated) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", id));
        event.setName(updated.getName());
        event.setDescription(updated.getDescription());
        event.setStartDate(updated.getStartDate());
        event.setEndDate(updated.getEndDate());
        event.setVenue(updated.getVenue());
        event.setActive(updated.getActive());
        return ResponseEntity.ok(eventRepository.save(event));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        eventRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
