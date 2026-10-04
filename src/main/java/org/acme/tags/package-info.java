/// # Tags
/// > Report the distinct set of tags carried by existing articles, alphabetically ordered.
///
/// ## Boundary
/// - `list-tags` — return the distinct tags carried by existing articles, alphabetically ordered
///
/// ## Requirements
///
/// ### R1: List tags
/// - R1.1 — When tags are listed, the capability shall return each distinct tag carried by at least one existing article, exactly once.
/// - R1.2 — When tags are listed, the capability shall order the returned tags alphabetically.
/// - R1.3 — While no article carries tags, when tags are listed, the capability shall return an empty result.
/// - R1.4 — The capability shall list tags without requiring a session token.
///
/// ## Decisions
/// - D1 — The tag list is ordered alphabetically. _(why: the wire contract guarantees only set membership — confirmed choice; rejected: leaving ordering unspecified)_
///
/// ## Out of scope
/// - Recording or updating tags — `article` owns tags as article data; this capability only reports the distinct set
/// - Tag popularity, counts, and tag administration
package org.acme.tags;
