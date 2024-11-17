package com.momo.book.book;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Integer> {
    Optional<Book> findById(Integer bookId);
    @Query("""
            SELECT book
            FROM Book book
            WHERE book.archived = false
            AND book.shareable = true
            AND book.createdBy != :userId
            """)
    Page<Book> findAllDisplayableBooks(Pageable pageable, String userId);
    @Query("""
            SELECT book
            FROM Book book
            WHERE book.archived = false
            AND book.shareable = true
            AND book.owner.email = :userId
            """)
    Page<Book> findBooksByOwner(Pageable pageable, String userId);
}
