package org.acme.user;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/// Generated from the capability spec in [org.acme.user] — do not edit.
/// Marks the boundary method or test that realizes the given requirement statements.
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface UserRequirement {

    /// One constant per statement id in the spec's `## Requirements`.
    enum Rn {
        /// When a registration providing a non-blank username, a well-formed email address, and a non-blank password is submitted, the capability shall create the account and return the user representation together with a JWT.
        R1_1("R1.1", "When a registration providing a non-blank username, a well-formed email address, and a non-blank password is submitted, the capability shall create the account and return the user representation together with a JWT."),
        /// If the username or password is missing or blank, then the capability shall reject the registration.
        R1_2("R1.2", "If the username or password is missing or blank, then the capability shall reject the registration."),
        /// If the email address is missing, blank, or malformed, then the capability shall reject the registration.
        R1_3("R1.3", "If the email address is missing, blank, or malformed, then the capability shall reject the registration."),
        /// If the username is already associated with an existing account, then the capability shall reject the registration.
        R1_6("R1.6", "If the username is already associated with an existing account, then the capability shall reject the registration."),
        /// If the email address is already associated with an existing account, then the capability shall reject the registration.
        R1_7("R1.7", "If the email address is already associated with an existing account, then the capability shall reject the registration."),
        /// When an email address and password matching an existing account are submitted, the capability shall return the user representation together with a JWT.
        R2_1("R2.1", "When an email address and password matching an existing account are submitted, the capability shall return the user representation together with a JWT."),
        /// If no account matches the submitted email address and password, then the capability shall reject the authentication.
        R2_2("R2.2", "If no account matches the submitted email address and password, then the capability shall reject the authentication."),
        /// When an authentication is rejected, the capability shall not reveal whether the email address or the password caused the failure.
        R2_3("R2.3", "When an authentication is rejected, the capability shall not reveal whether the email address or the password caused the failure."),
        /// If the email address or password is absent or blank, then the capability shall reject the authentication.
        R2_4("R2.4", "If the email address or password is absent or blank, then the capability shall reject the authentication."),
        /// When a request carries a valid JWT, the capability shall return the representation of the account the token identifies.
        R3_1("R3.1", "When a request carries a valid JWT, the capability shall return the representation of the account the token identifies."),
        /// If the JWT is absent, expired, or invalid, then the capability shall reject the request.
        R3_2("R3.2", "If the JWT is absent, expired, or invalid, then the capability shall reject the request."),
        /// While a request carries a valid JWT, when an update is submitted, the capability shall apply only the provided fields — email, username, password, bio, and image — leave all other fields unchanged, and return the updated user representation together with a JWT.
        R4_1("R4.1", "While a request carries a valid JWT, when an update is submitted, the capability shall apply only the provided fields — email, username, password, bio, and image — leave all other fields unchanged, and return the updated user representation together with a JWT."),
        /// If the JWT is absent, expired, or invalid, then the capability shall reject the update.
        R4_2("R4.2", "If the JWT is absent, expired, or invalid, then the capability shall reject the update."),
        /// If the update provides a username already associated with another account, then the capability shall reject the update.
        R4_5("R4.5", "If the update provides a username already associated with another account, then the capability shall reject the update."),
        /// If the update provides no fields, then the capability shall reject the update.
        R4_6("R4.6", "If the update provides no fields, then the capability shall reject the update."),
        /// If the update provides an email address already associated with another account, then the capability shall reject the update.
        R4_7("R4.7", "If the update provides an email address already associated with another account, then the capability shall reject the update."),
        /// If the update provides a blank username, email address, or password, then the capability shall reject the update.
        R4_8("R4.8", "If the update provides a blank username, email address, or password, then the capability shall reject the update."),
        /// If the update provides an explicit null username, email address, or password, then the capability shall reject the update.
        R4_9("R4.9", "If the update provides an explicit null username, email address, or password, then the capability shall reject the update."),
        /// When an update provides a blank or explicit null bio or image, the capability shall store the field as unset.
        R4_10("R4.10", "When an update provides a blank or explicit null bio or image, the capability shall store the field as unset."),
        /// If the update provides a password shorter than eight characters, then the capability shall reject the update.
        R4_11("R4.11", "If the update provides a password shorter than eight characters, then the capability shall reject the update."),
        /// The capability shall never persist a plaintext password.
        R5_1("R5.1", "The capability shall never persist a plaintext password.");

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
