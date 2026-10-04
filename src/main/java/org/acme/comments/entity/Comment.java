package org.acme.comments.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.acme.article.entity.Article;
import org.acme.user.entity.User;

@Entity
@Table(name = "comments")
public class Comment {

    @Id
    @GeneratedValue
    public Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "article_id")
    public Article article;

    @ManyToOne(optional = false)
    @JoinColumn(name = "author_id")
    public User author;

    @Column(nullable = false)
    public String body;

    @Column(nullable = false)
    public Instant createdAt;

    @Column(nullable = false)
    public Instant updatedAt;

    protected Comment() {
    }

    public static Comment remark(User author, Article article, String body) {
        var comment = new Comment();
        comment.author = author;
        comment.article = article;
        comment.body = body;
        comment.createdAt = comment.updatedAt = Instant.now().truncatedTo(ChronoUnit.MICROS);
        return comment;
    }
}
