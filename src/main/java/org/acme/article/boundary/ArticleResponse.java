package org.acme.article.boundary;

import java.time.Instant;
import java.util.List;
import org.acme.article.control.ArticleView;
import org.acme.profile.boundary.ProfileResponse;
import org.acme.profile.boundary.ProfileResponse.ProfileView;

public record ArticleResponse(FullArticle article) {

    public static ArticleResponse of(ArticleView view) {
        var article = view.article();
        return new ArticleResponse(new FullArticle(article.slug, article.title, article.description, article.body,
                article.tagList, article.createdAt, article.updatedAt, view.favorited(), view.favoritesCount(),
                ProfileResponse.of(view.author()).profile()));
    }

    public record FullArticle(String slug, String title, String description, String body, List<String> tagList,
            Instant createdAt, Instant updatedAt, boolean favorited, int favoritesCount, ProfileView author) {
    }
}
