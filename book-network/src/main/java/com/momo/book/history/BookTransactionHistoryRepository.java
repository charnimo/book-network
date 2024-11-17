package com.momo.book.history;


import com.momo.book.book.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BookTransactionHistoryRepository extends JpaRepository<BookTransactionHistory, Integer> {
    Optional<BookTransactionHistory> findById(Integer bookHistoryId);
    @Query("""
            SELECT history
            FROM BookTransactionHistory history
            WHERE history.user.email = :userId
            AND history.returned = false
            """)
    Page<BookTransactionHistory> findAllBorrowedBooks(Pageable pageable, String  userId);
    @Query("""
            SELECT history
            FROM BookTransactionHistory history
            WHERE history.user.email = :userId
            AND history.returned=true
            AND history.returnApproved=true
            """)
    Page<BookTransactionHistory> findAllReturnedBooks(Pageable pageable, @Param("userId") String userId);

    @Query("""
            SELECT
            (COUNT (*) > 0) AS isBorrowed
            FROM BookTransactionHistory bookTransactionHistory
            WHERE bookTransactionHistory.user.email = :userId
            AND bookTransactionHistory.book.id = :bookId
            AND bookTransactionHistory.returnApproved = false
            """)
    boolean isAlreadyBorrowedByUser(@Param("bookId") Integer bookId, @Param("userId") String userId);

    @Query("""
            SELECT
            (COUNT (*) > 0) AS isBorrowed
            FROM BookTransactionHistory bookTransactionHistory
            WHERE bookTransactionHistory.book.id = :bookId
            AND bookTransactionHistory.returnApproved = false
            """)
    boolean isAlreadyBorrowed(@Param("bookId") Integer bookId);

    @Query("""
            SELECT transaction
            FROM BookTransactionHistory  transaction
            WHERE transaction.user.email = :userId
            AND transaction.book.id = :bookId
            AND transaction.returned = false
            AND transaction.returnApproved = false
            """)
    Optional<BookTransactionHistory> findByBookIdAndUserId(@Param("bookId") Integer bookId, @Param("userId") String userId);
    @Query("""
            SELECT transaction
            FROM BookTransactionHistory  transaction
            WHERE transaction.book.createdBy = :userId
            AND transaction.book.id = :bookId
            AND transaction.returned = true
            AND transaction.returnApproved = false
            """)
    Optional<BookTransactionHistory> findByBookIdAndOwnerId(@Param("bookId") Integer bookId, @Param("userId") String userId);

}
