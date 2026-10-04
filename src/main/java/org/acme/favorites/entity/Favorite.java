package org.acme.favorites.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.acme.article.entity.Article;
import org.acme.user.entity.User;

@Entity
@Table(name = "favorites", uniqueConstraints = @UniqueConstraint(columnNames = { "user_id", "article_id" }))
public class Favorite {

    @Id
    @GeneratedValue
    public Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    public User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "article_id")
    public Article article;

    protected Favorite() {
    }

    Favorite(User user, Article article) {
        this.user = user;
        this.article = article;
    }

    public static Favorite mark(User user, Article article) {
        return new Favorite(user, article);
    }
}
