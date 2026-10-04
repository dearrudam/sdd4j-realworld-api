package org.acme.comments.control;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;
import org.acme.article.control.Articles;
import org.acme.article.entity.Article;
import org.acme.comments.entity.Comment;
import org.acme.profile.control.Profiles;
import org.acme.user.control.Rejection;
import org.acme.user.entity.User;
import org.hibernate.Session;

@ApplicationScoped
public class Comments {

    @Inject
    Session session;

    @Inject
    Articles articles;

    @Inject
    Profiles profiles;

    @Transactional
    public List<CommentView> list(User caller, String slug) {
        var article = articles.article(slug);
        return session
                .createSelectionQuery(
                        "from Comment c where c.article = :article order by c.createdAt asc, c.id asc",
                        Comment.class)
                .setParameter("article", article)
                .getResultList()
                .stream()
                .map(comment -> view(caller, comment))
                .toList();
    }

    @Transactional
    public CommentView create(User caller, String slug, String body) {
        var comment = Comment.remark(caller, articles.article(slug), body);
        session.persist(comment);
        return view(caller, comment);
    }

    @Transactional
    public void delete(User caller, String slug, long id) {
        var article = articles.article(slug);
        var comment = findOn(article, id);
        if (comment == null) {
            throw Rejection.notFound("comment");
        }
        if (!comment.author.id.equals(caller.id) && !article.author.id.equals(caller.id)) {
            throw Rejection.forbidden("comment");
        }
        session.remove(comment);
    }

    public void deleteAll(Article article) {
        session.createMutationQuery("delete from Comment c where c.article = :article")
                .setParameter("article", article)
                .executeUpdate();
    }

    Comment findOn(Article article, long id) {
        return session
                .createSelectionQuery("from Comment c where c.article = :article and c.id = :id",
                        Comment.class)
                .setParameter("article", article)
                .setParameter("id", id)
                .getSingleResultOrNull();
    }

    CommentView view(User caller, Comment comment) {
        return new CommentView(comment, profiles.get(caller, comment.author.username));
    }
}
