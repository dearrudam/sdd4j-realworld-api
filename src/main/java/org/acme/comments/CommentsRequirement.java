package org.acme.comments;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/// Generated from the capability spec in [org.acme.comments] — do not edit.
/// Marks the boundary method or test that realizes the given requirement statements.
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CommentsRequirement {

    /// One constant per statement id in the spec's `## Requirements`.
    enum Rn {
        /// When comments are listed for an existing article, the capability shall return the article's comments ordered oldest first.
        R1_1("R1.1", "When comments are listed for an existing article, the capability shall return the article's comments ordered oldest first."),
        /// While the article carries no comments, when comments are listed, the capability shall return an empty result.
        R1_2("R1.2", "While the article carries no comments, when comments are listed, the capability shall return an empty result."),
        /// While the caller is identified by a valid session token, the capability shall report each returned comment's author following indicator according to whether the caller follows that author.
        R1_3("R1.3", "While the caller is identified by a valid session token, the capability shall report each returned comment's author following indicator according to whether the caller follows that author."),
        /// While no session token identifies a caller, the capability shall report each returned comment's author following indicator unset.
        R1_4("R1.4", "While no session token identifies a caller, the capability shall report each returned comment's author following indicator unset."),
        /// If a session token is presented but expired or invalid, then the capability shall reject the request.
        R1_5("R1.5", "If a session token is presented but expired or invalid, then the capability shall reject the request."),
        /// If no article exists for the requested slug, then the capability shall reject the request.
        R1_6("R1.6", "If no article exists for the requested slug, then the capability shall reject the request."),
        /// While the request carries a valid session token, when a comment is created with a body on an existing article, the capability shall record the comment with the caller as author, assign it a unique identifier, stamp its creation and update times, and return the created comment.
        R2_1("R2.1", "While the request carries a valid session token, when a comment is created with a body on an existing article, the capability shall record the comment with the caller as author, assign it a unique identifier, stamp its creation and update times, and return the created comment."),
        /// If the body is absent or blank, then the capability shall reject the request.
        R2_2("R2.2", "If the body is absent or blank, then the capability shall reject the request."),
        /// If no article exists for the requested slug, then the capability shall reject the request.
        R2_3("R2.3", "If no article exists for the requested slug, then the capability shall reject the request."),
        /// If the session token is absent, expired, or invalid, then the capability shall reject the request.
        R2_4("R2.4", "If the session token is absent, expired, or invalid, then the capability shall reject the request."),
        /// While the requested slug identifies an existing article and the identified caller is the comment's author or the article's author, when deletion is requested for an existing comment on that article, the capability shall remove the comment such that it is no longer listed and return no content.
        R3_1("R3.1", "While the requested slug identifies an existing article and the identified caller is the comment's author or the article's author, when deletion is requested for an existing comment on that article, the capability shall remove the comment such that it is no longer listed and return no content."),
        /// If the session token is absent, expired, or invalid, then the capability shall reject the request.
        R3_2("R3.2", "If the session token is absent, expired, or invalid, then the capability shall reject the request."),
        /// If no article exists for the requested slug, then the capability shall reject the request.
        R3_3("R3.3", "If no article exists for the requested slug, then the capability shall reject the request."),
        /// If no comment exists for the requested identifier on the article, then the capability shall reject the request.
        R3_4("R3.4", "If no comment exists for the requested identifier on the article, then the capability shall reject the request."),
        /// If the identified caller is neither the comment's author nor the article's author, then the capability shall reject the request and leave the comment in place.
        R3_5("R3.5", "If the identified caller is neither the comment's author nor the article's author, then the capability shall reject the request and leave the comment in place.");

        private final String id;
        private final String statement;

        Rn(String id, String statement) {
            this.id = id;
            this.statement = statement;
        }

        public String statement() {
            return statement;
        }

        @Override
        public String toString() {
            return id;
        }
    }

    Rn[] value();
}
