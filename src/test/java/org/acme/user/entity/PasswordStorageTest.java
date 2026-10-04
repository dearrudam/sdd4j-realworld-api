package org.acme.user.entity;

import static io.restassured.RestAssured.given;
import static org.acme.user.UserRequirement.Rn.R5_1;
import static org.assertj.core.api.Assertions.assertThat;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import org.acme.user.UserRequirement;
import org.hibernate.Session;
import org.junit.jupiter.api.Test;

@QuarkusTest
class PasswordStorageTest {

    @Inject
    Session session;

    @Test
    @TestTransaction
    @UserRequirement(R5_1)
    void passwordsAreNeverStoredInPlaintext() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"user":{"username":"r5-1","email":"r5-1@conduit.test","password":"s3cret-r5-1"}}
                        """)
                .post("/api/users")
                .then()
                .statusCode(201);
        var user = session
                .createSelectionQuery("from User where email = :email", User.class)
                .setParameter("email", "r5-1@conduit.test")
                .getSingleResult();
        assertThat(user.passwordHash)
                .as(R5_1 + " — " + R5_1.statement())
                .isNotEqualTo("s3cret-r5-1")
                .startsWith("$2");
        assertThat(user.matchesPassword("s3cret-r5-1")).isTrue();
        assertThat(user.matchesPassword("wrong-password")).isFalse();
    }
}
