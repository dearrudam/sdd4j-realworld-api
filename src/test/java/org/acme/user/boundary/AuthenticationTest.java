package org.acme.user.boundary;

import static io.restassured.RestAssured.given;
import static org.acme.user.UserRequirement.Rn.R2_1;
import static org.acme.user.UserRequirement.Rn.R2_2;
import static org.acme.user.UserRequirement.Rn.R2_3;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.util.stream.Stream;
import org.acme.user.UserRequirement;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@QuarkusTest
class AuthenticationTest {

    static final String INVALID_CREDENTIALS = "{\"errors\":{\"credentials\":[\"is invalid\"]}}";

    @ParameterizedTest(name = "{0}")
    @MethodSource("cases")
    void authentication(UserRequirement.Rn requirement, String seed, String email, String password,
            int expectedStatus, String expectedBody) {
        if (seed != null) {
            register(seed, "pw-" + seed);
        }
        var response = login(email, password);
        assertThat(response.statusCode())
                .as(requirement + " — " + requirement.statement())
                .isEqualTo(expectedStatus);
        if (expectedStatus == 200) {
            assertThat(response.jsonPath().getString("user.email")).isEqualTo(email);
            assertThat(response.jsonPath().getString("user.token")).isNotBlank();
        }
        if (expectedBody != null) {
            assertThat(response.asString()).isEqualTo(expectedBody);
        }
    }

    static Stream<Arguments> cases() {
        return Stream.of(
                arguments(R2_1, "r2-1", "r2-1@conduit.test", "pw-r2-1", 200, null),
                arguments(R2_2, null, "ghost-r2-2@conduit.test", "any-password", 401, INVALID_CREDENTIALS),
                arguments(R2_2, "r2-2b", "r2-2b@conduit.test", "wrong-password", 401, INVALID_CREDENTIALS),
                arguments(R2_3, null, "nobody-r2-3@conduit.test", "any-password", 401, INVALID_CREDENTIALS),
                arguments(R2_3, "r2-3", "r2-3@conduit.test", "wrong-password", 401, INVALID_CREDENTIALS));
    }

    static void register(String username, String password) {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"user":{"username":"%s","email":"%s@conduit.test","password":"%s"}}
                        """.formatted(username, username, password))
                .post("/api/users");
    }

    static Response login(String email, String password) {
        return given()
                .contentType(ContentType.JSON)
                .body("""
                        {"user":{"email":"%s","password":"%s"}}
                        """.formatted(email, password))
                .post("/api/users/login");
    }
}
