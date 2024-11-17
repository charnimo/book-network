package com.momo.book.feedback;

import com.momo.book.book.Book;
import com.momo.book.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
public class Feedback extends BaseEntity {
    private Double note;
    private String comment;
    @ManyToOne
    @JoinColumn(name = "book_id")
    private Book book;

}
