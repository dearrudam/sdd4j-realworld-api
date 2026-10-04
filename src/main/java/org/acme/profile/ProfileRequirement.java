package org.acme.profile;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/// Generated from the capability spec in [org.acme.profile] — do not edit.
/// Marks the boundary method or test that realizes the given requirement statements.
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ProfileRequirement {

    /// One constant per statement id in the spec's `## Requirements`.
    enum Rn {
        /// When a profile is requested for the username of an existing user, the capability shall return the user's public representation — username, bio, and image — together with the caller's following indicator.
        R1_1("R1.1", "When a profile is requested for the username of an existing user, the capability shall return the user's public representation — username, bio, and image — together with the caller's following indicator."),
        /// While the caller identified by a valid session token follows the requested user, the capability shall return the profile with its following indicator set.
        R1_2("R1.2", "While the caller identified by a valid session token follows the requested user, the capability shall return the profile with its following indicator set."),
        /// While no session token identifies a caller, or the identified caller does not follow the requested user, the capability shall return the profile with its following indicator unset.
        R1_3("R1.3", "While no session token identifies a caller, or the identified caller does not follow the requested user, the capability shall return the profile with its following indicator unset."),
        /// If a session token is presented but expired or invalid, then the capability shall reject the request.
        R1_4("R1.4", "If a session token is presented but expired or invalid, then the capability shall reject the request."),
        /// If no user exists for the requested username, then the capability shall reject the request.
        R1_5("R1.5", "If no user exists for the requested username, then the capability shall reject the request."),
        /// While the request carries a valid session token, when the identified caller follows an existing user, the capability shall record the follow relationship and return the target's profile with its following indicator set.
        R2_1("R2.1", "While the request carries a valid session token, when the identified caller follows an existing user, the capability shall record the follow relationship and return the target's profile with its following indicator set."),
        /// While the identified caller already follows the target user, when a follow is requested, the capability shall keep a single follow relationship and return the target's profile with its following indicator set.
        R2_2("R2.2", "While the identified caller already follows the target user, when a follow is requested, the capability shall keep a single follow relationship and return the target's profile with its following indicator set."),
        /// If the session token is absent, expired, or invalid, then the capability shall reject the request.
        R2_3("R2.3", "If the session token is absent, expired, or invalid, then the capability shall reject the request."),
        /// If no user exists for the target username, then the capability shall reject the request.
        R2_4("R2.4", "If no user exists for the target username, then the capability shall reject the request."),
        /// If the target username identifies the caller, then the capability shall reject the request.
        R2_5("R2.5", "If the target username identifies the caller, then the capability shall reject the request."),
        /// While the request carries a valid session token, when the identified caller unfollows an existing user they follow, the capability shall remove the follow relationship and return the target's profile with its following indicator unset.
        R3_1("R3.1", "While the request carries a valid session token, when the identified caller unfollows an existing user they follow, the capability shall remove the follow relationship and return the target's profile with its following indicator unset."),
        /// While the identified caller does not follow the target user, when an unfollow is requested, the capability shall return the target's profile with its following indicator unset.
        R3_2("R3.2", "While the identified caller does not follow the target user, when an unfollow is requested, the capability shall return the target's profile with its following indicator unset."),
        /// If the session token is absent, expired, or invalid, then the capability shall reject the request.
        R3_3("R3.3", "If the session token is absent, expired, or invalid, then the capability shall reject the request."),
        /// If no user exists for the target username, then the capability shall reject the request.
        R3_4("R3.4", "If no user exists for the target username, then the capability shall reject the request."),
        /// If the target username identifies the caller, then the capability shall reject the request.
        R3_5("R3.5", "If the target username identifies the caller, then the capability shall reject the request.");

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
