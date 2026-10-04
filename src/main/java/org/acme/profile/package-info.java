/// # Profile
/// > Present a user's public representation and manage the follow relationships between users.
///
/// ## Boundary
/// - `get-profile` — return the public representation of the user identified by a username, for an optionally identified caller
/// - `follow-user` — make the caller identified by a session token follow a user
/// - `unfollow-user` — make the caller identified by a session token stop following a user
///
/// ## Requirements
///
/// ### R1: Get a profile
/// - R1.1 — When a profile is requested for the username of an existing user, the capability shall return the user's public representation — username, bio, and image — together with the caller's following indicator.
/// - R1.2 — While the caller identified by a valid session token follows the requested user, the capability shall return the profile with its following indicator set.
/// - R1.3 — While no session token identifies a caller, or the identified caller does not follow the requested user, the capability shall return the profile with its following indicator unset.
/// - R1.4 — If a session token is presented but expired or invalid, then the capability shall reject the request.
/// - R1.5 — If no user exists for the requested username, then the capability shall reject the request.
///
/// ### R2: Follow a user
/// - R2.1 — While the request carries a valid session token, when the identified caller follows an existing user, the capability shall record the follow relationship and return the target's profile with its following indicator set.
/// - R2.2 — While the identified caller already follows the target user, when a follow is requested, the capability shall keep a single follow relationship and return the target's profile with its following indicator set.
/// - R2.3 — If the session token is absent, expired, or invalid, then the capability shall reject the request.
/// - R2.4 — If no user exists for the target username, then the capability shall reject the request.
/// - R2.5 — If the target username identifies the caller, then the capability shall reject the request.
///
/// ### R3: Unfollow a user
/// - R3.1 — While the request carries a valid session token, when the identified caller unfollows an existing user they follow, the capability shall remove the follow relationship and return the target's profile with its following indicator unset.
/// - R3.2 — While the identified caller does not follow the target user, when an unfollow is requested, the capability shall return the target's profile with its following indicator unset.
/// - R3.3 — If the session token is absent, expired, or invalid, then the capability shall reject the request.
/// - R3.4 — If no user exists for the target username, then the capability shall reject the request.
/// - R3.5 — If the target username identifies the caller, then the capability shall reject the request.
///
/// ## Entities
/// - Follow
///
/// ## Decisions
/// - D1 — Self-follow and self-unfollow are rejected. _(why: the official spec is silent and a self-edge in the follow graph is meaningless; rejected: allowing them as no-ops)_
/// - D2 — Repeated follow and unfollow are idempotent, returning the target's profile with the expected following indicator. _(why: the following indicator is set-membership, not a count; rejected: rejecting the repeated call as a conflict)_
///
/// ## Out of scope
/// - Account fields beyond the public representation — email and credentials are owned by `user`
/// - Follower and following counts, lists, and suggestions
/// - Blocking, muting, and private profiles
package org.acme.profile;
