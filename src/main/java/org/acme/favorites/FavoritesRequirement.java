package org.acme.favorites;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/// Generated from the capability spec in [org.acme.favorites] — do not edit.
/// Marks the boundary method or test that realizes the given requirement statements.
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FavoritesRequirement {

    /// One constant per statement id in the spec's `## Requirements`.
    enum Rn {
        /// While the request carries a valid session token, when the caller marks an existing article as favorite, the capability shall record the caller's mark on the article and return the article with its favorite indicator set and its favorite count including the new mark.
        R1_1("R1.1", "While the request carries a valid session token, when the caller marks an existing article as favorite, the capability shall record the caller's mark on the article and return the article with its favorite indicator set and its favorite count including the new mark."),
        /// When the caller marks an article they already marked as favorite, the capability shall leave the mark and the favorite count unchanged and return the article.
        R1_2("R1.2", "When the caller marks an article they already marked as favorite, the capability shall leave the mark and the favorite count unchanged and return the article."),
        /// When the caller marks an article they authored as favorite, the capability shall record the mark.
        R1_3("R1.3", "When the caller marks an article they authored as favorite, the capability shall record the mark."),
        /// If no article exists for the requested slug, then the capability shall reject the request.
        R1_4("R1.4", "If no article exists for the requested slug, then the capability shall reject the request."),
        /// If the session token is absent, expired, or invalid, then the capability shall reject the request.
        R1_5("R1.5", "If the session token is absent, expired, or invalid, then the capability shall reject the request."),
        /// While the request carries a valid session token, when the caller removes their mark from an article they marked favorite, the capability shall remove the caller's mark and return the article with its favorite indicator unset and its favorite count excluding the removed mark.
        R2_1("R2.1", "While the request carries a valid session token, when the caller removes their mark from an article they marked favorite, the capability shall remove the caller's mark and return the article with its favorite indicator unset and its favorite count excluding the removed mark."),
        /// When the caller removes a mark from an article they have not marked as favorite, the capability shall leave the article and its favorite count unchanged and return the article.
        R2_2("R2.2", "When the caller removes a mark from an article they have not marked as favorite, the capability shall leave the article and its favorite count unchanged and return the article."),
        /// If no article exists for the requested slug, then the capability shall reject the request.
        R2_3("R2.3", "If no article exists for the requested slug, then the capability shall reject the request."),
        /// If the session token is absent, expired, or invalid, then the capability shall reject the request.
        R2_4("R2.4", "If the session token is absent, expired, or invalid, then the capability shall reject the request.");

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
