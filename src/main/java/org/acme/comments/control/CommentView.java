package org.acme.comments.control;

import org.acme.comments.entity.Comment;
import org.acme.profile.control.Profile;

public record CommentView(Comment comment, Profile author) {
}
