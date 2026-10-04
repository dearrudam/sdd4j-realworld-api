package org.acme.comments.boundary;

import java.util.List;
import org.acme.comments.boundary.CommentResponse.WireComment;
import org.acme.comments.control.CommentView;

public record CommentsResponse(List<WireComment> comments) {

    public static CommentsResponse of(List<CommentView> views) {
        return new CommentsResponse(views.stream().map(view -> CommentResponse.of(view).comment()).toList());
    }
}
