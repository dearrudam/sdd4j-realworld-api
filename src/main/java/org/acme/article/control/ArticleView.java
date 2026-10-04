package org.acme.article.control;

import org.acme.article.entity.Article;
import org.acme.profile.control.Profile;

public record ArticleView(Article article, Profile author, boolean favorited, int favoritesCount) {
}
