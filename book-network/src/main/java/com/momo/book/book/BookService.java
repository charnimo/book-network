package com.momo.book.book;

import com.momo.book.User.User;
import com.momo.book.common.PageResponse;
import com.momo.book.exceptions.OperationNotPermittedException;
import com.momo.book.history.BookTransactionHistory;
import com.momo.book.history.BookTransactionHistoryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;



@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BookService {
    private final BookRepository bookRepository;
    private final BookTransactionHistoryRepository bookTransactionHistoryRepository; ;
    private final BookMapper bookMapper;
    public Integer save(BookRequest request, Authentication connectedUser) {
        User user = ((User) connectedUser.getPrincipal());
        Book book = bookMapper.toBook(request);
        book.setOwner(user);
        return bookRepository.save(book).getId();
    }

    public BookResponse findById(Integer bookId) {
        return bookRepository.findById(bookId)
                .map(bookMapper::toBookResponse)
                .orElseThrow(() -> new EntityNotFoundException("No book found with ID:: " + bookId));
    }

    public List<BookResponse> findAll() {
        return bookRepository.findAll()
                .stream()
                .map(bookMapper::toBookResponse)
                .collect(Collectors.toList());

    }

    public PageResponse<BookResponse> findAllBooks(int page, int size, Authentication connectedUser) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        Page<Book> books = bookRepository.findAllDisplayableBooks(pageable, connectedUser.getName());
        List<BookResponse> booksResponse = books.stream()
                .map(bookMapper::toBookResponse)
                .toList();
        return new PageResponse<>(
                booksResponse,
                books.getNumber(),
                books.getSize(),
                books.getTotalElements(),
                books.getTotalPages(),
                books.isFirst(),
                books.isLast()
        );
    }
    public PageResponse<BookResponse> findByOwner(int page, int size, String owner) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        Page<Book> books = bookRepository.findBooksByOwner(pageable, owner);
        List<BookResponse> booksResponse = books.stream()
                .map(bookMapper::toBookResponse)
                .toList();
        return new PageResponse<>(
                booksResponse,
                books.getNumber(),
                books.getSize(),
                books.getTotalElements(),
                books.getTotalPages(),
                books.isFirst(),
                books.isLast()
        );
    }


    public PageResponse<BorrowedBookResponse> findAllBorrowedBooks(int page, int size, String owner) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        Page<BookTransactionHistory> allBorrowedBooks = bookTransactionHistoryRepository.findAllBorrowedBooks(pageable,owner);
        List<BorrowedBookResponse> booksResponse = allBorrowedBooks.stream()
                .map(bookMapper::toBorrowedBookResponse)
                .toList();
        return new PageResponse<>(
                booksResponse,
                allBorrowedBooks.getNumber(),
                allBorrowedBooks.getSize(),
                allBorrowedBooks.getTotalElements(),
                allBorrowedBooks.getTotalPages(),
                allBorrowedBooks.isFirst(),
                allBorrowedBooks.isLast()
        );
    }

    public PageResponse<BorrowedBookResponse> findAllReturnedBooks(int page, int size, String owner) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        Page<BookTransactionHistory> allBorrowedBooks = bookTransactionHistoryRepository.findAllReturnedBooks(pageable,owner);
        List<BorrowedBookResponse> booksResponse = allBorrowedBooks.stream()
                .map(bookMapper::toBorrowedBookResponse)
                .toList();
        return new PageResponse<>(
                booksResponse,
                allBorrowedBooks.getNumber(),
                allBorrowedBooks.getSize(),
                allBorrowedBooks.getTotalElements(),
                allBorrowedBooks.getTotalPages(),
                allBorrowedBooks.isFirst(),
                allBorrowedBooks.isLast()
        );
    }

    public Integer updateShareableStatus(Integer bookId, Authentication connectedUser) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("No book found with ID:: " + bookId));
        if (!Objects.equals(book.getCreatedBy(), connectedUser.getName())) {
            throw new OperationNotPermittedException("You cannot update others books shareable status");
        }
        book.setShareable(!book.isShareable());
        bookRepository.save(book);
        return bookId;
    }
    public Integer updateArchivedStatus(Integer bookId, Authentication connectedUser) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("No book found with ID:: " + bookId));
        if (!Objects.equals(book.getCreatedBy(), connectedUser.getName())) {
            throw new OperationNotPermittedException("You cannot update others books archived status");
        }
        book.setArchived(!book.isArchived());
        bookRepository.save(book);
        return bookId;
    }

    public Integer borrowBook(Integer bookId, Authentication connectedUser) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("No book found with ID:: " + bookId));
        User user = (User) connectedUser.getPrincipal();
        if (book.isArchived() || !book.isShareable()) {
            throw new OperationNotPermittedException("The requested book cannot be borrowed since it is archived or not shareable");
        }
        if (Objects.equals(book.getCreatedBy(), connectedUser.getName())) {
            throw new OperationNotPermittedException("You cannot borrow your own book");
        }
        final boolean isAlreadyBorrowedByUser = bookTransactionHistoryRepository.isAlreadyBorrowedByUser(bookId, connectedUser.getName());
        if (isAlreadyBorrowedByUser) {
            throw new OperationNotPermittedException("You already borrowed this book and it is still not returned or the return is not approved by the owner");
        }

        final boolean isAlreadyBorrowedByOtherUser = bookTransactionHistoryRepository.isAlreadyBorrowed(bookId);
        if (isAlreadyBorrowedByOtherUser) {
            throw new OperationNotPermittedException("Te requested book is already borrowed");
        }

        BookTransactionHistory bookTransactionHistory = BookTransactionHistory.builder()
                .user(user)
                .book(book)
                .returned(false)
                .returnApproved(false)
                .build();
        return bookTransactionHistoryRepository.save(bookTransactionHistory).getId();
    }
    public Integer returnBorrowedBook(Integer bookId, Authentication connectedUser) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("No book found with ID:: " + bookId));
        if (book.isArchived() || !book.isShareable()) {
            throw new OperationNotPermittedException("The requested book is archived or not shareable");
        }
        if (Objects.equals(book.getCreatedBy(), connectedUser.getName())) {
            throw new OperationNotPermittedException("You cannot borrow or return your own book");
        }

        BookTransactionHistory bookTransactionHistory = bookTransactionHistoryRepository.findByBookIdAndUserId(bookId, connectedUser.getName())
                .orElseThrow(() -> new OperationNotPermittedException("You did not borrow this book"));

        bookTransactionHistory.setReturned(true);
        return bookTransactionHistoryRepository.save(bookTransactionHistory).getId();
    }

    public Integer approveReturnBorrowedBook(Integer bookId, Authentication connectedUser) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("No book found with ID:: " + bookId));
        if (book.isArchived() || !book.isShareable()) {
            throw new OperationNotPermittedException("The requested book is archived or not shareable");
        }
        if (!Objects.equals(book.getCreatedBy(), connectedUser.getName())) {
            throw new OperationNotPermittedException("You cannot approve the return of a book you do not own");
        }

        BookTransactionHistory bookTransactionHistory = bookTransactionHistoryRepository.findByBookIdAndOwnerId(bookId, connectedUser.getName())
                .orElseThrow(() -> new OperationNotPermittedException("The book is not returned yet. You cannot approve its return"));

        bookTransactionHistory.setReturnApproved(true);
        return bookTransactionHistoryRepository.save(bookTransactionHistory).getId();
    }


}
