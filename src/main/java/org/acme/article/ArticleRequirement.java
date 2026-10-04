package org.acme.article;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/// Generated from the capability spec in [org.acme.article] — do not edit.
/// Marks the boundary method or test that realizes the given requirement statements.
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ArticleRequirement {

    /// One constant per statement id in the spec's `## Requirements`.
    enum Rn {
        /// When articles are listed, the capability shall return the matching articles ordered most recent first, together with the total count of matching articles.
        R1_1("R1.1", "When articles are listed, the capability shall return the matching articles ordered most recent first, together with the total count of matching articles."),
        /// While a tag filter is provided, when articles are listed, the capability shall return only articles carrying that tag.
        R1_2("R1.2", "While a tag filter is provided, when articles are listed, the capability shall return only articles carrying that tag."),
        /// While an author filter is provided, when articles are listed, the capability shall return only articles authored by that user.
        R1_3("R1.3", "While an author filter is provided, when articles are listed, the capability shall return only articles authored by that user."),
        /// While a favorited-by filter is provided, when articles are listed, the capability shall return only articles marked favorite by that user.
        R1_4("R1.4", "While a favorited-by filter is provided, when articles are listed, the capability shall return only articles marked favorite by that user."),
        /// If an author or favorited-by filter names no existing user, then the capability shall return an empty result.
        R1_5("R1.5", "If an author or favorited-by filter names no existing user, then the capability shall return an empty result."),
        /// When a listing is bounded by a limit, the capability shall return at most that many articles; absent a limit, the capability shall return at most twenty.
        R1_6("R1.6", "When a listing is bounded by a limit, the capability shall return at most that many articles; absent a limit, the capability shall return at most twenty."),
        /// When a listing is bounded by an offset, the capability shall skip that many of the most recent matching articles; absent an offset, the capability shall skip none.
        R1_7("R1.7", "When a listing is bounded by an offset, the capability shall skip that many of the most recent matching articles; absent an offset, the capability shall skip none."),
        /// While the caller is identified by a valid session token, the capability shall report each returned article's author following indicator according to whether the caller follows that author.
        R1_8("R1.8", "While the caller is identified by a valid session token, the capability shall report each returned article's author following indicator according to whether the caller follows that author."),
        /// While no session token identifies a caller, the capability shall report each returned article's author following indicator unset.
        R1_9("R1.9", "While no session token identifies a caller, the capability shall report each returned article's author following indicator unset."),
        /// If a session token is presented but expired or invalid, then the capability shall reject the request.
        R1_10("R1.10", "If a session token is presented but expired or invalid, then the capability shall reject the request."),
        /// While the caller is identified by a valid session token, the capability shall report each returned article's favorite indicator according to whether the caller has marked it favorite, and each favorite count as the number of favorite marks recorded on that article.
        R1_11("R1.11", "While the caller is identified by a valid session token, the capability shall report each returned article's favorite indicator according to whether the caller has marked it favorite, and each favorite count as the number of favorite marks recorded on that article."),
        /// While no session token identifies a caller, the capability shall report each returned article's favorite indicator unset, and each favorite count as the number of favorite marks recorded on that article.
        R1_12("R1.12", "While no session token identifies a caller, the capability shall report each returned article's favorite indicator unset, and each favorite count as the number of favorite marks recorded on that article."),
        /// While the request carries a valid session token, when the feed is requested, the capability shall return the most recent articles authored by users the caller follows, ordered most recent first, together with their total count.
        R2_1("R2.1", "While the request carries a valid session token, when the feed is requested, the capability shall return the most recent articles authored by users the caller follows, ordered most recent first, together with their total count."),
        /// While none of the users the caller follows has articles, when the feed is requested, the capability shall return an empty result.
        R2_2("R2.2", "While none of the users the caller follows has articles, when the feed is requested, the capability shall return an empty result."),
        /// When the feed is bounded by a limit or offset, the capability shall page the result under the same bounds and defaults as article listing.
        R2_3("R2.3", "When the feed is bounded by a limit or offset, the capability shall page the result under the same bounds and defaults as article listing."),
        /// While the request carries a valid session token, the capability shall report each returned article's author following indicator set.
        R2_4("R2.4", "While the request carries a valid session token, the capability shall report each returned article's author following indicator set."),
        /// If the session token is absent, expired, or invalid, then the capability shall reject the request.
        R2_5("R2.5", "If the session token is absent, expired, or invalid, then the capability shall reject the request."),
        /// The capability shall report each returned article's favorite indicator and count under the same rules as article listing.
        R2_6("R2.6", "The capability shall report each returned article's favorite indicator and count under the same rules as article listing."),
        /// When an article is requested for the slug of an existing article, the capability shall return the article's full representation — slug, title, description, body, tag list, creation and update times, favorite indicator and count, and author profile.
        R3_1("R3.1", "When an article is requested for the slug of an existing article, the capability shall return the article's full representation — slug, title, description, body, tag list, creation and update times, favorite indicator and count, and author profile."),
        /// While the caller identified by a valid session token follows the article's author, the capability shall return the article with its author following indicator set.
        R3_2("R3.2", "While the caller identified by a valid session token follows the article's author, the capability shall return the article with its author following indicator set."),
        /// While no session token identifies a caller, or the identified caller does not follow the article's author, the capability shall return the article with its author following indicator unset.
        R3_3("R3.3", "While no session token identifies a caller, or the identified caller does not follow the article's author, the capability shall return the article with its author following indicator unset."),
        /// If a session token is presented but expired or invalid, then the capability shall reject the request.
        R3_4("R3.4", "If a session token is presented but expired or invalid, then the capability shall reject the request."),
        /// If no article exists for the requested slug, then the capability shall reject the request.
        R3_5("R3.5", "If no article exists for the requested slug, then the capability shall reject the request."),
        /// While the identified caller has marked the article favorite, the capability shall return the article with its favorite indicator set and its favorite count as the number of marks recorded on it.
        R3_6("R3.6", "While the identified caller has marked the article favorite, the capability shall return the article with its favorite indicator set and its favorite count as the number of marks recorded on it."),
        /// While no session token identifies a caller, or the identified caller has not marked the article favorite, the capability shall return the article with its favorite indicator unset and its favorite count as the number of marks recorded on it.
        R3_7("R3.7", "While no session token identifies a caller, or the identified caller has not marked the article favorite, the capability shall return the article with its favorite indicator unset and its favorite count as the number of marks recorded on it."),
        /// While the request carries a valid session token, when an article is created with a title, description, and body, the capability shall record the article with the caller as author, assign it a slug derived from its title, stamp its creation and update times, and return the created article.
        R4_1("R4.1", "While the request carries a valid session token, when an article is created with a title, description, and body, the capability shall record the article with the caller as author, assign it a slug derived from its title, stamp its creation and update times, and return the created article."),
        /// When an article is created with a tag list, the capability shall record the article carrying those tags; absent a tag list, the capability shall record the article carrying no tags.
        R4_2("R4.2", "When an article is created with a tag list, the capability shall record the article carrying those tags; absent a tag list, the capability shall record the article carrying no tags."),
        /// When an article is created, the capability shall report its favorite indicator unset and its favorite count zero.
        R4_3("R4.3", "When an article is created, the capability shall report its favorite indicator unset and its favorite count zero."),
        /// If the title, description, or body is absent or blank, then the capability shall reject the request.
        R4_4("R4.4", "If the title, description, or body is absent or blank, then the capability shall reject the request."),
        /// If the slug derived from the title already identifies an existing article, then the capability shall reject the request.
        R4_5("R4.5", "If the slug derived from the title already identifies an existing article, then the capability shall reject the request."),
        /// If the session token is absent, expired, or invalid, then the capability shall reject the request.
        R4_6("R4.6", "If the session token is absent, expired, or invalid, then the capability shall reject the request."),
        /// While the requested slug identifies an article authored by the identified caller, when an update provides a title, description, body, or tag list, the capability shall apply the provided changes, refresh the article's update time, and return the article.
        R5_1("R5.1", "While the requested slug identifies an article authored by the identified caller, when an update provides a title, description, body, or tag list, the capability shall apply the provided changes, refresh the article's update time, and return the article."),
        /// While an article's title is updated, the capability shall keep the article's existing slug.
        R5_2("R5.2", "While an article's title is updated, the capability shall keep the article's existing slug."),
        /// When an update provides no changes, the capability shall return the article unmodified.
        R5_3("R5.3", "When an update provides no changes, the capability shall return the article unmodified."),
        /// If a provided title, description, or body is blank, then the capability shall reject the request.
        R5_4("R5.4", "If a provided title, description, or body is blank, then the capability shall reject the request."),
        /// If the session token is absent, expired, or invalid, then the capability shall reject the request.
        R5_5("R5.5", "If the session token is absent, expired, or invalid, then the capability shall reject the request."),
        /// If no article exists for the requested slug, then the capability shall reject the request.
        R5_6("R5.6", "If no article exists for the requested slug, then the capability shall reject the request."),
        /// If the identified caller is not the article's author, then the capability shall reject the request.
        R5_7("R5.7", "If the identified caller is not the article's author, then the capability shall reject the request."),
        /// While the requested slug identifies an article authored by the identified caller, when deletion is requested, the capability shall remove the article such that it is no longer retrievable and return no content.
        R6_1("R6.1", "While the requested slug identifies an article authored by the identified caller, when deletion is requested, the capability shall remove the article such that it is no longer retrievable and return no content."),
        /// If the session token is absent, expired, or invalid, then the capability shall reject the request.
        R6_2("R6.2", "If the session token is absent, expired, or invalid, then the capability shall reject the request."),
        /// If no article exists for the requested slug, then the capability shall reject the request.
        R6_3("R6.3", "If no article exists for the requested slug, then the capability shall reject the request."),
        /// If the identified caller is not the article's author, then the capability shall reject the request.
        R6_4("R6.4", "If the identified caller is not the article's author, then the capability shall reject the request."),
        /// When an article is removed, the capability shall remove every favorite mark recorded on it.
        R6_5("R6.5", "When an article is removed, the capability shall remove every favorite mark recorded on it."),
        /// When an article is removed, the capability shall remove every comment recorded on it.
        R6_6("R6.6", "When an article is removed, the capability shall remove every comment recorded on it.");

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
