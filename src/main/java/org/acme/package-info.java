/// # Conduit
/// > A spec-driven implementation of the RealWorld Conduit API on Quarkus.
///
/// ## Vision
/// - Demonstrate SDD4J — capability specs as contracts, EARS requirements traced to tests — on a real-world-shaped API.
///
/// ## Components
/// - `profile` → `user` — resolve a presented session token to its caller and read the target user account.
/// - `article` → `user` — resolve a presented session token to its caller and read author accounts.
/// - `article` → `profile` — assemble author profiles carrying the caller's following indicator and read the caller's follow graph for the feed.
/// - `favorites` → `user` — resolve a presented session token to its caller.
/// - `favorites` → `article` — resolve a slug to its article and render the returned article representation.
/// - `article` → `favorites` — read favorite marks to report indicators, counts, and evaluate the favorited-by filter.
///
/// ## Ubiquitous language
/// - User — a registered account; owned by `user`.
/// - Session token — the JWT identifying the caller; issued by `user` and required by any capability acting on the caller's behalf.
/// - Profile — a user's public representation (username, bio, image) plus the caller's following indicator; assembled by `profile` from `user` data.
/// - Follow — the directed relationship from a following user to a followed user; owned by `profile`.
/// - Article — a published piece identified by its slug; owned by `article`.
/// - Slug — an article's public identifier, derived from its title at creation; owned by `article`.
/// - Tag — a label attached to an article; recorded by `article`.
/// - Favorite — a user's mark of appreciation on an article; owned by `favorites`.
///
/// ## Decisions
/// - D1 — The wire contract conforms to the official RealWorld Conduit API spec (`openapi` v2.0.0, `specs/api/openapi.yml`, mirrored at `src/main/openapi/openapi.yml`). _(why: full official conformance chosen; rejected: custom variant with username-based login and non-unique email)_
///
/// ## Stack
/// - microprofile-server (Quarkus) · base package `org.acme`
package org.acme;
