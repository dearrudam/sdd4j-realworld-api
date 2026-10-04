package org.acme.favorites.control;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.acme.article.control.ArticleView;
import org.acme.article.control.Articles;
import org.acme.article.entity.Article;
import org.acme.favorites.entity.Favorite;
import org.acme.user.entity.User;
import org.hibernate.Session;

@ApplicationScoped
public class Favorites {

    @Inject
    Session session;

    @Inject
    Articles articles;

    @Transactional
    public ArticleView mark(User caller, String slug) {
        var article = articles.article(slug);
        if (find(caller, article) == null) {
            session.persist(Favorite.mark(caller, article));
        }
        return articles.view(caller, article);
    }

    @Transactional
    public ArticleView unmark(User caller, String slug) {
        var article = articles.article(slug);
        var favorite = find(caller, article);
        if (favorite != null) {
            session.remove(favorite);
        }
        return articles.view(caller, article);
    }

    public boolean marked(User caller, Article article) {
        return caller != null && find(caller, article) != null;
    }

    public int countFor(Article article) {
        return session.createSelectionQuery(
                "select count(f) from Favorite f where f.article = :article", Long.class)
                .setParameter("article", article)
                .getSingleResult()
                .intValue();
    }

    public Set<Long> markedIds(User caller, List<Article> articles) {
        if (caller == null || articles.isEmpty()) {
            return Set.of();
        }
        return new HashSet<>(session.createSelectionQuery(
                "select f.article.id from Favorite f where f.user = :caller and f.article in :articles",
                Long.class)
                .setParameter("caller", caller)
                .setParameter("articles", articles)
                .getResultList());
    }

    public Map<Long, Integer> counts(List<Article> articles) {
        if (articles.isEmpty()) {
            return Map.of();
        }
        var counts = new HashMap<Long, Integer>();
        session.createSelectionQuery(
                "select f.article.id, count(f) from Favorite f where f.article in :articles group by f.article.id",
                Object[].class)
                .setParameter("articles", articles)
                .getResultList()
                .forEach(row -> counts.put((Long) row[0], ((Long) row[1]).intValue()));
        return counts;
    }

    public void unmarkAll(Article article) {
        session.createMutationQuery("delete from Favorite f where f.article = :article")
                .setParameter("article", article)
                .executeUpdate();
    }

    Favorite find(User caller, Article article) {
        return session.createSelectionQuery(
                "from Favorite f where f.user = :caller and f.article = :article", Favorite.class)
                .setParameter("caller", caller)
                .setParameter("article", article)
                .getSingleResultOrNull();
    }
}
