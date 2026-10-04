package org.acme.article.control;

import java.util.List;

public record ArticlePage(List<ArticleView> articles, long total) {
}
