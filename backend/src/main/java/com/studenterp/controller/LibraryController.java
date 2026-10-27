package com.studenterp.controller;

import com.studenterp.entity.LibraryBook;
import com.studenterp.exception.ResourceNotFoundException;
import com.studenterp.repository.LibraryBookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/library")
@RequiredArgsConstructor
public class LibraryController {

    private final LibraryBookRepository libraryBookRepository;

    @GetMapping("/books")
    public ResponseEntity<List<LibraryBook>> findAll() {
        return ResponseEntity.ok(libraryBookRepository.findAll());
    }

    @PostMapping("/books")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LibraryBook> createBook(@RequestBody LibraryBook book) {
        return ResponseEntity.ok(libraryBookRepository.save(book));
    }

    @PutMapping("/books/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LibraryBook> updateBook(@PathVariable Long id, @RequestBody LibraryBook updated) {
        LibraryBook book = libraryBookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LibraryBook", "id", id));
        book.setTitle(updated.getTitle());
        book.setAuthor(updated.getAuthor());
        book.setIsbn(updated.getIsbn());
        book.setPublisher(updated.getPublisher());
        book.setCategory(updated.getCategory());
        return ResponseEntity.ok(libraryBookRepository.save(book));
    }

    @DeleteMapping("/books/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        libraryBookRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/issued")
    public ResponseEntity<List<Object>> issuedBooks() {
        return ResponseEntity.ok(Collections.emptyList());
    }

    @GetMapping("/overdue")
    public ResponseEntity<List<Object>> overdueBooks() {
        return ResponseEntity.ok(Collections.emptyList());
    }
}
