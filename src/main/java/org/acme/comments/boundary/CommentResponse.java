package org.acme.comments.boundary;

import java.time.Instant;
import org.acme.comments.control.CommentView;
import org.acme.profile.boundary.ProfileResponse;
import org.acme.profile.boundary.ProfileResponse.ProfileView;

public record CommentResponse(WireComment comment) {

    public static CommentResponse of(CommentView view) {
        var comment = view.comment();
        return new CommentResponse(new WireComment(comment.id, comment.createdAt, comment.updatedAt,
                comment.body, ProfileResponse.of(view.author()).profile()));
    }

    public record WireComment(long id, Instant createdAt, Instant updatedAt, String body,
            ProfileView author) {
    }
}
