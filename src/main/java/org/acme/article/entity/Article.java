package org.acme.article.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.acme.user.entity.User;

@Entity
@Table(name = "articles", uniqueConstraints = @UniqueConstraint(columnNames = "slug"))
public class Article {

    @Id
    @GeneratedValue
    public Long id;

    @Column(nullable = false)
    public String slug;

    @Column(nullable = false)
    public String title;

    @Column(nullable = false)
    public String description;

    @Column(nullable = false)
    public String body;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "article_tags", joinColumns = @JoinColumn(name = "article_id"))
    @OrderColumn(name = "position")
    @Column(name = "tag")
    public List<String> tagList = new ArrayList<>();

    @ManyToOne(optional = false)
    @JoinColumn(name = "author_id")
    public User author;

    @Column(nullable = false)
    public Instant createdAt;

    @Column(nullable = false)
    public Instant updatedAt;

    protected Article() {
    }

    public static Article draft(User author, String title, String description, String body, List<String> tags) {
        var article = new Article();
        article.author = author;
        article.title = title;
        article.description = description;
        article.body = body;
        article.tagList = tags == null ? new ArrayList<>() : new ArrayList<>(tags);
        article.slug = slugify(title);
        article.createdAt = article.updatedAt = Instant.now().truncatedTo(ChronoUnit.MICROS);
        return article;
    }

    public void revise(String title, String description, String body, List<String> tags) {
        if (title != null) {
            this.title = title;
        }
        if (description != null) {
            this.description = description;
        }
        if (body != null) {
            this.body = body;
        }
        if (tags != null) {
            this.tagList = new ArrayList<>(tags);
        }
        this.updatedAt = Instant.now().truncatedTo(ChronoUnit.MICROS);
    }

    public static String slugify(String title) {
        return title.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
    }
}
