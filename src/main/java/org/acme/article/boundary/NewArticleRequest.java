package org.acme.article.boundary;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record NewArticleRequest(@NotNull @Valid NewArticle article) {

    public record NewArticle(@NotBlank String title, @NotBlank String description, @NotBlank String body,
            List<String> tagList) {
    }
}
