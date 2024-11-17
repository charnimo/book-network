package com.momo.book.book;

import com.momo.book.User.User;
import com.momo.book.common.BaseEntity;
import com.momo.book.feedback.Feedback;
import com.momo.book.history.BookTransactionHistory;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.List;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
public class Book extends BaseEntity {
    @Id
    @GeneratedValue
    private Integer id;
    private String title;
    private String authorName;
    private String isbn;
    private String synopsis;
    private String bookCover;
    private boolean archived;
    private boolean shareable;
    @OneToMany(mappedBy = "book")
    private List<Feedback> feedbacks;
    @OneToMany(mappedBy = "book")
    private List<BookTransactionHistory> histories;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;
    @Transient
    public double getRate() {
        if (feedbacks == null || feedbacks.isEmpty()) {
            return 0.0;
        }
        var rate = this.feedbacks.stream()
                .mapToDouble(Feedback::getNote)
                .average()
                .orElse(0.0);
        double roundedRate = Math.round(rate * 10.0) / 10.0;
        return roundedRate;
    }

}
