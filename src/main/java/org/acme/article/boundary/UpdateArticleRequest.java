package org.acme.article.boundary;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.List;

public record UpdateArticleRequest(@NotNull @Valid UpdateArticle article) {

    public record UpdateArticle(@Pattern(regexp = ".*\\S.*", message = "must not be blank") String title,
            @Pattern(regexp = ".*\\S.*", message = "must not be blank") String description,
            @Pattern(regexp = ".*\\S.*", message = "must not be blank") String body,
            List<String> tagList) {
    }
}
