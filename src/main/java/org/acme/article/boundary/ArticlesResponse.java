package org.acme.article.boundary;

import java.time.Instant;
import java.util.List;
import org.acme.article.control.ArticlePage;
import org.acme.article.control.ArticleView;
import org.acme.profile.boundary.ProfileResponse;
import org.acme.profile.boundary.ProfileResponse.ProfileView;

public record ArticlesResponse(List<ArticleSummary> articles, long articlesCount) {

    public static ArticlesResponse of(ArticlePage page) {
        return new ArticlesResponse(page.articles().stream().map(ArticlesResponse::summary).toList(), page.total());
    }

    static ArticleSummary summary(ArticleView view) {
        var article = view.article();
        return new ArticleSummary(article.slug, article.title, article.description, article.tagList,
                article.createdAt, article.updatedAt, false, 0,
                ProfileResponse.of(view.author()).profile());
    }

    public record ArticleSummary(String slug, String title, String description, List<String> tagList,
            Instant createdAt, Instant updatedAt, boolean favorited, int favoritesCount, ProfileView author) {
    }
}
