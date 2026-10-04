package org.acme.tags;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/// Generated from the capability spec in [org.acme.tags] — do not edit.
/// Marks the boundary method or test that realizes the given requirement statements.
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TagsRequirement {

    /// One constant per statement id in the spec's `## Requirements`.
    enum Rn {
        /// When tags are listed, the capability shall return each distinct tag carried by at least one existing article, exactly once.
        R1_1("R1.1", "When tags are listed, the capability shall return each distinct tag carried by at least one existing article, exactly once."),
        /// When tags are listed, the capability shall order the returned tags alphabetically.
        R1_2("R1.2", "When tags are listed, the capability shall order the returned tags alphabetically."),
        /// While no article carries tags, when tags are listed, the capability shall return an empty result.
        R1_3("R1.3", "While no article carries tags, when tags are listed, the capability shall return an empty result."),
        /// The capability shall list tags without requiring a session token.
        R1_4("R1.4", "The capability shall list tags without requiring a session token.");

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
