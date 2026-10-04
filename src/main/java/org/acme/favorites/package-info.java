/// # Favorites
/// > Record and remove users' favorite marks on articles, returning the affected article's representation.
///
/// ## Boundary
/// - `favorite-article` — record the identified caller's favorite mark on the article identified by a slug and return the article's representation
/// - `unfavorite-article` — remove the identified caller's favorite mark from the article identified by a slug and return the article's representation
///
/// ## Requirements
///
/// ### R1: Favorite an article
/// - R1.1 — While the request carries a valid session token, when the caller marks an existing article as favorite, the capability shall record the caller's mark on the article and return the article with its favorite indicator set and its favorite count including the new mark.
/// - R1.2 — When the caller marks an article they already marked as favorite, the capability shall leave the mark and the favorite count unchanged and return the article.
/// - R1.3 — When the caller marks an article they authored as favorite, the capability shall record the mark.
/// - R1.4 — If no article exists for the requested slug, then the capability shall reject the request.
/// - R1.5 — If the session token is absent, expired, or invalid, then the capability shall reject the request.
///
/// ### R2: Unfavorite an article
/// - R2.1 — While the request carries a valid session token, when the caller removes their mark from an article they marked favorite, the capability shall remove the caller's mark and return the article with its favorite indicator unset and its favorite count excluding the removed mark.
/// - R2.2 — When the caller removes a mark from an article they have not marked as favorite, the capability shall leave the article and its favorite count unchanged and return the article.
/// - R2.3 — If no article exists for the requested slug, then the capability shall reject the request.
/// - R2.4 — If the session token is absent, expired, or invalid, then the capability shall reject the request.
///
/// ## Entities
/// - Favorite
///
/// ## Decisions
/// - D1 — Repeating a favorite or an unfavorite that changes nothing succeeds idempotently, returning the article. _(why: the wire contract is silent; consistent with the profile capability's idempotent follow/unfollow — confirmed choice; rejected: rejecting repeat marks as invalid)_
/// - D2 — Authors may mark their own articles as favorite. _(why: the wire contract imposes no self-favorite restriction — confirmed choice; rejected: rejecting self-marks like self-follow)_
///
/// ## Out of scope
/// - Article representations carrying the favorite indicator, count, and favorited-by filter — `article` reports them through declared wiring; this capability owns only the marks
/// - Follow relationships between users — owned by `profile`
/// - Favorite notifications, favorite ranking, and unauthenticated favorite marking
package org.acme.favorites;
