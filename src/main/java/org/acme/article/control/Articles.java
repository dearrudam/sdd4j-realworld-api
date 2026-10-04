package org.acme.article.control;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;
import org.acme.article.entity.Article;
import org.acme.comments.control.Comments;
import org.acme.favorites.control.Favorites;
import org.acme.profile.control.Profiles;
import org.acme.user.control.Rejection;
import org.acme.user.entity.User;
import org.hibernate.Session;

@ApplicationScoped
public class Articles {

    @Inject
    Session session;

    @Inject
    Profiles profiles;

    @Inject
    Favorites favorites;

    @Inject
    Comments comments;

    @Transactional
    public ArticlePage list(User caller, String tag, String author, String favorited, int limit, int offset) {
        var filter = new StringBuilder();
        if (tag != null) {
            filter.append(" and :tag member of a.tagList");
        }
        if (author != null) {
            filter.append(" and a.author.username = :author");
        }
        if (favorited != null) {
            filter.append(
                    " and a.id in (select f.article.id from Favorite f where f.user.username = :favorited)");
        }
        var select = session.createSelectionQuery(
                "from Article a where 1=1" + filter + " order by a.createdAt desc, a.id desc", Article.class);
        var count = session.createSelectionQuery(
                "select count(a) from Article a where 1=1" + filter, Long.class);
        if (tag != null) {
            select.setParameter("tag", tag);
            count.setParameter("tag", tag);
        }
        if (author != null) {
            select.setParameter("author", author);
            count.setParameter("author", author);
        }
        if (favorited != null) {
            select.setParameter("favorited", favorited);
            count.setParameter("favorited", favorited);
        }
        var articles = select.setFirstResult(offset).setMaxResults(limit).getResultList();
        return new ArticlePage(views(caller, articles), count.getSingleResult());
    }

    @Transactional
    public ArticlePage feed(User caller, int limit, int offset) {
        var authors = profiles.followedBy(caller);
        if (authors.isEmpty()) {
            return new ArticlePage(List.of(), 0);
        }
        var articles = session
                .createSelectionQuery(
                        "from Article a where a.author in :authors order by a.createdAt desc, a.id desc",
                        Article.class)
                .setParameter("authors", authors)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
        var total = session
                .createSelectionQuery("select count(a) from Article a where a.author in :authors", Long.class)
                .setParameter("authors", authors)
                .getSingleResult();
        return new ArticlePage(views(caller, articles), total);
    }

    @Transactional
    public ArticleView get(User caller, String slug) {
        return view(caller, article(slug));
    }

    @Transactional
    public ArticleView create(User caller, String title, String description, String body, List<String> tags) {
        if (findBySlug(Article.slugify(title)) != null) {
            throw Rejection.taken(409, "slug");
        }
        var article = Article.draft(caller, title, description, body, tags);
        session.persist(article);
        return view(caller, article);
    }

    @Transactional
    public ArticleView update(User caller, String slug, ArticlePatch patch) {
        var article = article(slug);
        rejectIfNotAuthor(caller, article);
        if (!patch.isEmpty()) {
            article.revise(patch.title(), patch.description(), patch.body(), patch.tagList());
        }
        return view(caller, article);
    }

    @Transactional
    public void delete(User caller, String slug) {
        var article = article(slug);
        rejectIfNotAuthor(caller, article);
        favorites.unmarkAll(article);
        comments.deleteAll(article);
        session.remove(article);
    }

    public List<String> distinctTags() {
        return session
                .createSelectionQuery("select distinct t from Article a join a.tagList t order by t",
                        String.class)
                .getResultList();
    }

    public Article article(String slug) {
        var article = findBySlug(slug);
        if (article == null) {
            throw Rejection.notFound("article");
        }
        return article;
    }

    Article findBySlug(String slug) {
        return session.createSelectionQuery("from Article where slug = :slug", Article.class)
                .setParameter("slug", slug)
                .getSingleResultOrNull();
    }

    void rejectIfNotAuthor(User caller, Article article) {
        if (!article.author.id.equals(caller.id)) {
            throw Rejection.forbidden("article");
        }
    }

    List<ArticleView> views(User caller, List<Article> articles) {
        var marked = favorites.markedIds(caller, articles);
        var counts = favorites.counts(articles);
        return articles.stream()
                .map(article -> new ArticleView(article, profiles.get(caller, article.author.username),
                        marked.contains(article.id), counts.getOrDefault(article.id, 0)))
                .toList();
    }

    public ArticleView view(User caller, Article article) {
        return new ArticleView(article, profiles.get(caller, article.author.username),
                favorites.marked(caller, article), favorites.countFor(article));
    }
}
