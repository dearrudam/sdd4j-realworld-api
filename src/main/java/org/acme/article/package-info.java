/// # Article
/// > Publish, list, read, update, and delete articles — including tag, author, and favorited-by filtering and a personal feed.
///
/// ## Boundary
/// - `list-articles` — return the most recent articles matching optional tag, author, and favorited-by filters, for an optionally identified caller
/// - `get-feed` — return the most recent articles authored by users the identified caller follows
/// - `get-article` — return the article identified by a slug, for an optionally identified caller
/// - `create-article` — publish a new article authored by the identified caller
/// - `update-article` — modify the article identified by a slug, restricted to its author
/// - `delete-article` — remove the article identified by a slug, restricted to its author
///
/// ## Requirements
///
/// ### R1: List articles
/// - R1.1 — When articles are listed, the capability shall return the matching articles ordered most recent first, together with the total count of matching articles.
/// - R1.2 — While a tag filter is provided, when articles are listed, the capability shall return only articles carrying that tag.
/// - R1.3 — While an author filter is provided, when articles are listed, the capability shall return only articles authored by that user.
/// - R1.4 — While a favorited-by filter is provided, when articles are listed, the capability shall return only articles marked favorite by that user. _(why: the wire contract offers the filter; the marks are owned by `favorites` — see D4)_
/// - R1.5 — If an author or favorited-by filter names no existing user, then the capability shall return an empty result.
/// - R1.6 — When a listing is bounded by a limit, the capability shall return at most that many articles; absent a limit, the capability shall return at most twenty.
/// - R1.7 — When a listing is bounded by an offset, the capability shall skip that many of the most recent matching articles; absent an offset, the capability shall skip none.
/// - R1.8 — While the caller is identified by a valid session token, the capability shall report each returned article's author following indicator according to whether the caller follows that author.
/// - R1.9 — While no session token identifies a caller, the capability shall report each returned article's author following indicator unset.
/// - R1.10 — If a session token is presented but expired or invalid, then the capability shall reject the request.
/// - R1.11 — While the caller is identified by a valid session token, the capability shall report each returned article's favorite indicator according to whether the caller has marked it favorite, and each favorite count as the number of favorite marks recorded on that article.
/// - R1.12 — While no session token identifies a caller, the capability shall report each returned article's favorite indicator unset, and each favorite count as the number of favorite marks recorded on that article.
///
/// ### R2: Get the caller's feed
/// - R2.1 — While the request carries a valid session token, when the feed is requested, the capability shall return the most recent articles authored by users the caller follows, ordered most recent first, together with their total count.
/// - R2.2 — While none of the users the caller follows has articles, when the feed is requested, the capability shall return an empty result.
/// - R2.3 — When the feed is bounded by a limit or offset, the capability shall page the result under the same bounds and defaults as article listing.
/// - R2.4 — While the request carries a valid session token, the capability shall report each returned article's author following indicator set.
/// - R2.5 — If the session token is absent, expired, or invalid, then the capability shall reject the request.
/// - R2.6 — The capability shall report each returned article's favorite indicator and count under the same rules as article listing.
///
/// ### R3: Get an article
/// - R3.1 — When an article is requested for the slug of an existing article, the capability shall return the article's full representation — slug, title, description, body, tag list, creation and update times, favorite indicator and count, and author profile.
/// - R3.2 — While the caller identified by a valid session token follows the article's author, the capability shall return the article with its author following indicator set.
/// - R3.3 — While no session token identifies a caller, or the identified caller does not follow the article's author, the capability shall return the article with its author following indicator unset.
/// - R3.4 — If a session token is presented but expired or invalid, then the capability shall reject the request.
/// - R3.5 — If no article exists for the requested slug, then the capability shall reject the request.
/// - R3.6 — While the identified caller has marked the article favorite, the capability shall return the article with its favorite indicator set and its favorite count as the number of marks recorded on it.
/// - R3.7 — While no session token identifies a caller, or the identified caller has not marked the article favorite, the capability shall return the article with its favorite indicator unset and its favorite count as the number of marks recorded on it.
///
/// ### R4: Create an article
/// - R4.1 — While the request carries a valid session token, when an article is created with a title, description, and body, the capability shall record the article with the caller as author, assign it a slug derived from its title, stamp its creation and update times, and return the created article.
/// - R4.2 — When an article is created with a tag list, the capability shall record the article carrying those tags; absent a tag list, the capability shall record the article carrying no tags.
/// - R4.3 — When an article is created, the capability shall report its favorite indicator unset and its favorite count zero.
/// - R4.4 — If the title, description, or body is absent or blank, then the capability shall reject the request.
/// - R4.5 — When the slug derived from the title already identifies an existing article, the capability shall assign a distinct slug derived from the title. _(why: the official conformance suite requires duplicate titles to produce distinct slugs; rejected: rejecting the request as a slug conflict)_
/// - R4.6 — If the session token is absent, expired, or invalid, then the capability shall reject the request.
///
/// ### R5: Update an article
/// - R5.1 — While the requested slug identifies an article authored by the identified caller, when an update provides a title, description, body, or tag list, the capability shall apply the provided changes, refresh the article's update time, and return the article.
/// - R5.2 — While an article's title is updated, the capability shall keep the article's existing slug.
/// - R5.3 — When an update provides no changes, the capability shall return the article unmodified.
/// - R5.4 — If a provided title, description, or body is blank, then the capability shall reject the request.
/// - R5.5 — If the session token is absent, expired, or invalid, then the capability shall reject the request.
/// - R5.6 — If no article exists for the requested slug, then the capability shall reject the request.
/// - R5.7 — If the identified caller is not the article's author, then the capability shall reject the request.
/// - R5.8 — If the update provides an explicit null title, description, body, or tag list, then the capability shall reject the request.
///
/// ### R6: Delete an article
/// - R6.1 — While the requested slug identifies an article authored by the identified caller, when deletion is requested, the capability shall remove the article such that it is no longer retrievable and return no content.
/// - R6.2 — If the session token is absent, expired, or invalid, then the capability shall reject the request.
/// - R6.3 — If no article exists for the requested slug, then the capability shall reject the request.
/// - R6.4 — If the identified caller is not the article's author, then the capability shall reject the request.
/// - R6.5 — When an article is removed, the capability shall remove every favorite mark recorded on it.
/// - R6.6 — When an article is removed, the capability shall remove every comment recorded on it.
///
/// ## Entities
/// - Article
///
/// ## Decisions
/// - D1 — An article's slug is derived from its title at creation and stays stable across title updates. _(why: the slug is the article's public identifier — regenerating it on edit silently breaks links; rejected: regenerating the slug whenever the title changes)_
/// - D2 — Favorite indicator, favorite count, and the favorited-by filter report unset, zero, and empty until a `favorites` capability owns favorite state. _(why: the wire contract requires the fields but nothing can be favorited yet — reporting empty is the truthful state; rejected: `article` owning favorite state now, or omitting the required fields)_ _(superseded by D4)_
/// - D3 — An update providing no changes returns the article unmodified rather than being rejected. _(why: the official `UpdateArticle` carries no at-least-one-field requirement, unlike `UpdateUser`; rejected: rejecting empty updates as invalid)_
/// - D4 — Favorite state is owned by `favorites`; the article reports favorite indicators, counts, and the favorited-by filter through the declared `article` → `favorites` wiring. _(why: `favorites` is now declared and owns the marks; rejected: `article` owning favorite state — per D2)_
///
/// ## Out of scope
/// - Comments on articles — owned by `comments`
/// - Favorite and unfavorite operations and favorite state storage — owned by `favorites`; this capability only reports the wire fields
/// - Tag listing and aggregation — owned by `tags`; a tag list is recorded as article data
/// - Drafts, versioning, article transfer, and co-authorship
/// - Full-text search and rich-text processing
package org.acme.article;
