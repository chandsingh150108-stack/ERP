package com.studenterp.repository;

import com.studenterp.entity.LibraryBook;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LibraryBookRepository extends JpaRepository<LibraryBook, Long> {
}
