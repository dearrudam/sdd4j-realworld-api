package org.acme.user.boundary;

import static io.restassured.RestAssured.given;
import static org.acme.user.UserRequirement.Rn.R4_1;
import static org.acme.user.UserRequirement.Rn.R4_2;
import static org.acme.user.UserRequirement.Rn.R4_5;
import static org.acme.user.UserRequirement.Rn.R4_6;
import static org.acme.user.UserRequirement.Rn.R4_7;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import java.util.Map;
import java.util.stream.Stream;
import org.acme.user.UserRequirement;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@QuarkusTest
class UpdateUserTest {

    @ParameterizedTest(name = "{0}")
    @MethodSource("cases")
    void update(UserRequirement.Rn requirement, Map<String, String> seed, boolean authenticated,
            Map<String, String> update, int expectedStatus, String expectedErrorField) {
        if (seed != null) {
            register(seed.get("username"), seed.get("email"));
        }
        var me = "me-" + requirement.name();
        var token = register(me, me + "@conduit.test");
        var request = authenticated ? given().header("Authorization", "Token " + token) : given();
        var response = request
                .contentType(ContentType.JSON)
                .body("""
                        {"user":{%s}}
                        """.formatted(fields(update)))
                .put("/api/user");
        assertThat(response.statusCode())
                .as(requirement + " — " + requirement.statement())
                .isEqualTo(expectedStatus);
        if (expectedErrorField == null) {
            update.forEach((field, value) -> assertThat(response.jsonPath().getString("user." + field))
                    .isEqualTo(value));
            if (!update.containsKey("email")) {
                assertThat(response.jsonPath().getString("user.email")).isEqualTo(me + "@conduit.test");
            }
            if (!update.containsKey("username")) {
                assertThat(response.jsonPath().getString("user.username")).isEqualTo(me);
            }
            assertThat(response.jsonPath().getString("user.token")).isNotBlank();
        } else {
            assertThat(response.jsonPath().getMap("errors")).containsKey(expectedErrorField);
        }
    }

    static Stream<Arguments> cases() {
        return Stream.of(
                arguments(R4_1, null, true,
                        Map.of("bio", "bio-r4-1", "image", "https://img.test/r4-1"), 200, null),
                arguments(R4_2, null, false,
                        Map.of("bio", "bio-r4-2"), 401, "token"),
                arguments(R4_5,
                        Map.of("username", "taken-r4-5", "email", "seed-r4-5@conduit.test"), true,
                        Map.of("username", "taken-r4-5"), 422, "username"),
                arguments(R4_6, null, true,
                        Map.of(), 422, "user"),
                arguments(R4_7,
                        Map.of("username", "seed-r4-7", "email", "taken-r4-7@conduit.test"), true,
                        Map.of("email", "taken-r4-7@conduit.test"), 422, "email"));
    }

    static String register(String username, String email) {
        return given()
                .contentType(ContentType.JSON)
                .body("""
                        {"user":{"username":"%s","email":"%s","password":"pw-%s"}}
                        """.formatted(username, email, username))
                .post("/api/users")
                .jsonPath()
                .getString("user.token");
    }

    static String fields(Map<String, String> update) {
        return update.entrySet().stream()
                .map(e -> "\"%s\":\"%s\"".formatted(e.getKey(), e.getValue()))
                .reduce((a, b) -> a + "," + b)
                .orElse("");
    }
}
