/// # User
/// > Manage user accounts: registration, authentication, current-user retrieval, and profile updates.
///
/// ## Boundary
/// - `register-user` — create an account from a username, email address, and password
/// - `authenticate-user` — verify an email address and password and return a session token
/// - `get-current-user` — return the account identified by a session token
/// - `update-user` — apply partial changes to the account identified by a session token
///
/// ## Requirements
///
/// ### R1: Register a user
/// - R1.1 — When a registration providing a non-blank username, a well-formed email address, and a non-blank password is submitted, the capability shall create the account and return the user representation together with a JWT.
/// - R1.2 — If the username or password is missing or blank, then the capability shall reject the registration.
/// - R1.3 — If the email address is missing, blank, or malformed, then the capability shall reject the registration.
/// - R1.6 — If the username is already associated with an existing account, then the capability shall reject the registration.
/// - R1.7 — If the email address is already associated with an existing account, then the capability shall reject the registration.
///
/// ### R2: Authenticate a user
/// - R2.1 — When an email address and password matching an existing account are submitted, the capability shall return the user representation together with a JWT.
/// - R2.2 — If no account matches the submitted email address and password, then the capability shall reject the authentication.
/// - R2.3 — When an authentication is rejected, the capability shall not reveal whether the email address or the password caused the failure. _(why: prevents account enumeration)_
///
/// ### R3: Get the current user
/// - R3.1 — When a request carries a valid JWT, the capability shall return the representation of the account the token identifies.
/// - R3.2 — If the JWT is absent, expired, or invalid, then the capability shall reject the request.
///
/// ### R4: Update the current user
/// - R4.1 — While a request carries a valid JWT, when an update is submitted, the capability shall apply only the provided fields — email, username, password, bio, and image — leave all other fields unchanged, and return the updated user representation together with a JWT.
/// - R4.2 — If the JWT is absent, expired, or invalid, then the capability shall reject the update.
/// - R4.5 — If the update provides a username already associated with another account, then the capability shall reject the update.
/// - R4.6 — If the update provides no fields, then the capability shall reject the update.
/// - R4.7 — If the update provides an email address already associated with another account, then the capability shall reject the update.
///
/// ### R5: Protect credentials
/// - R5.1 — The capability shall never persist a plaintext password. _(why: credential storage is a security contract, not an implementation detail)_
///
/// ## Entities
/// - User
///
/// ## Out of scope
/// - Email verification and password recovery flows
/// - Token lifetime policy, refresh, and revocation
/// - Roles, permissions, and authorization between users
/// - Public profiles and user-following relationships
package org.acme.user;
