/// # Comments
/// > List, create, and delete comments on articles — remarks authored by identified users, ordered oldest first.
///
/// ## Boundary
/// - `list-comments` — return the comments on the article identified by a slug, for an optionally identified caller
/// - `create-comment` — record a comment authored by the identified caller on the article identified by a slug
/// - `delete-comment` — remove the comment identified by its id on the article identified by a slug, restricted to the comment's author and the article's author
///
/// ## Requirements
///
/// ### R1: List comments
/// - R1.1 — When comments are listed for an existing article, the capability shall return the article's comments ordered oldest first.
/// - R1.2 — While the article carries no comments, when comments are listed, the capability shall return an empty result.
/// - R1.3 — While the caller is identified by a valid session token, the capability shall report each returned comment's author following indicator according to whether the caller follows that author.
/// - R1.4 — While no session token identifies a caller, the capability shall report each returned comment's author following indicator unset.
/// - R1.5 — If a session token is presented but expired or invalid, then the capability shall reject the request.
/// - R1.6 — If no article exists for the requested slug, then the capability shall reject the request.
///
/// ### R2: Create a comment
/// - R2.1 — While the request carries a valid session token, when a comment is created with a body on an existing article, the capability shall record the comment with the caller as author, assign it a unique identifier, stamp its creation and update times, and return the created comment.
/// - R2.2 — If the body is absent or blank, then the capability shall reject the request.
/// - R2.3 — If no article exists for the requested slug, then the capability shall reject the request.
/// - R2.4 — If the session token is absent, expired, or invalid, then the capability shall reject the request.
///
/// ### R3: Delete a comment
/// - R3.1 — While the requested slug identifies an existing article and the identified caller is the comment's author or the article's author, when deletion is requested for an existing comment on that article, the capability shall remove the comment such that it is no longer listed and return no content.
/// - R3.2 — If the session token is absent, expired, or invalid, then the capability shall reject the request.
/// - R3.3 — If no article exists for the requested slug, then the capability shall reject the request.
/// - R3.4 — If no comment exists for the requested identifier on the article, then the capability shall reject the request.
/// - R3.5 — If the identified caller is neither the comment's author nor the article's author, then the capability shall reject the request and leave the comment in place.
///
/// ## Entities
/// - Comment
///
/// ## Decisions
/// - D1 — Both the comment's author and the article's author may delete a comment. _(why: the wire contract admits a permission rejection without naming the allowed parties — confirmed choice; rejected: restricting deletion to the comment's author only)_
/// - D2 — Comments are returned oldest first. _(why: chronological thread order matches how discussions read and the reference implementation's insertion order — confirmed choice; rejected: most recent first like article listings)_
///
/// ## Out of scope
/// - Comment editing — no update operation exists; a comment's update time reflects its creation
/// - Comment pagination — the wire contract declares none
/// - Removing comments when their article is removed — the cascade is `article`'s contract, executed through declared wiring
/// - Rich-text processing, replies or threading, and comment reporting
package org.acme.comments;
