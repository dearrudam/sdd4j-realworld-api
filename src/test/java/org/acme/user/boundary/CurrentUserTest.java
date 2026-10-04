package org.acme.user.boundary;

import static io.restassured.RestAssured.given;
import static org.acme.user.UserRequirement.Rn.R3_1;
import static org.acme.user.UserRequirement.Rn.R3_2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.smallrye.jwt.build.Jwt;
import java.time.Instant;
import java.util.stream.Stream;
import org.acme.user.UserRequirement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@QuarkusTest
class CurrentUserTest {

    @Test
    @UserRequirement(R3_1)
    void validTokenReturnsUser() {
        var token = registerAndToken("r3-1", "r3-1@conduit.test");
        var response = authenticated(token).get("/api/user");
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getString("user.email")).isEqualTo("r3-1@conduit.test");
        assertThat(response.jsonPath().getString("user.username")).isEqualTo("r3-1");
        assertThat(response.jsonPath().getString("user.token")).isNotBlank();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("rejectedTokens")
    void invalidTokenIsRejected(UserRequirement.Rn requirement, String authorization) {
        var request = authorization == null ? given() : given().header("Authorization", authorization);
        var response = request.get("/api/user");
        assertThat(response.statusCode())
                .as(requirement + " — " + requirement.statement())
                .isEqualTo(401);
        assertThat(response.jsonPath().getMap("errors")).containsKey("token");
    }

    static Stream<Arguments> rejectedTokens() {
        return Stream.of(
                arguments(R3_2, (String) null),
                arguments(R3_2, "Token not-a-jwt"),
                arguments(R3_2, "Bearer " + expiredToken()),
                arguments(R3_2, "Token " + expiredToken()));
    }

    static String expiredToken() {
        return Jwt.subject("1")
                .upn("ghost")
                .issuer("conduit")
                .expiresAt(Instant.now().minusSeconds(60))
                .sign();
    }

    static RequestSpecification authenticated(String token) {
        return given().header("Authorization", "Token " + token);
    }

    static String registerAndToken(String username, String email) {
        return given()
                .contentType(ContentType.JSON)
                .body("""
                        {"user":{"username":"%s","email":"%s","password":"pw-%s"}}
                        """.formatted(username, email, username))
                .post("/api/users")
                .jsonPath()
                .getString("user.token");
    }
}
