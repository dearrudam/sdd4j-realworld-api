package org.acme.comments.boundary;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record NewCommentRequest(@NotNull @Valid NewComment comment) {

    public record NewComment(@NotBlank String body) {
    }
}
