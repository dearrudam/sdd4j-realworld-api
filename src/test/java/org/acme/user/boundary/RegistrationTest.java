package org.acme.user.boundary;

import static io.restassured.RestAssured.given;
import static org.acme.user.UserRequirement.Rn.R1_1;
import static org.acme.user.UserRequirement.Rn.R1_2;
import static org.acme.user.UserRequirement.Rn.R1_3;
import static org.acme.user.UserRequirement.Rn.R1_6;
import static org.acme.user.UserRequirement.Rn.R1_7;
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
class RegistrationTest {

    @ParameterizedTest(name = "{0}")
    @MethodSource("cases")
    void registration(UserRequirement.Rn requirement, Map<String, String> seed, Map<String, String> user,
            int expectedStatus, String expectedErrorField) {
        if (seed != null) {
            register(seed).then().statusCode(201);
        }
        var response = register(user);
        assertThat(response.statusCode())
                .as(requirement + " — " + requirement.statement())
                .isEqualTo(expectedStatus);
        if (expectedErrorField == null) {
            assertThat(response.jsonPath().getString("user.username")).isEqualTo(user.get("username"));
            assertThat(response.jsonPath().getString("user.email")).isEqualTo(user.get("email"));
            assertThat(response.jsonPath().getString("user.token")).isNotBlank();
        } else {
            assertThat(response.jsonPath().getMap("errors")).containsKey(expectedErrorField);
        }
    }

    static Stream<Arguments> cases() {
        return Stream.of(
                arguments(R1_1, null,
                        Map.of("username", "r1-1", "email", "r1-1@conduit.test", "password", "pw-r1-1"), 201, null),
                arguments(R1_2, null,
                        Map.of("email", "r1-2a@conduit.test", "password", "pw-r1-2"), 422, "username"),
                arguments(R1_2, null,
                        Map.of("username", "r1-2b", "email", "r1-2b@conduit.test", "password", "  "), 422, "password"),
                arguments(R1_3, null,
                        Map.of("username", "r1-3a", "password", "pw-r1-3"), 422, "email"),
                arguments(R1_3, null,
                        Map.of("username", "r1-3b", "email", "not-an-email", "password", "pw-r1-3"), 422, "email"),
                arguments(R1_6,
                        Map.of("username", "taken-r1-6", "email", "seed-r1-6@conduit.test", "password", "pw-r1-6"),
                        Map.of("username", "taken-r1-6", "email", "r1-6@conduit.test", "password", "pw-r1-6"),
                        409, "username"),
                arguments(R1_7,
                        Map.of("username", "seed-r1-7", "email", "taken-r1-7@conduit.test", "password", "pw-r1-7"),
                        Map.of("username", "r1-7", "email", "taken-r1-7@conduit.test", "password", "pw-r1-7"),
                        409, "email"));
    }

    static io.restassured.response.Response register(Map<String, String> user) {
        return given()
                .contentType(ContentType.JSON)
                .body("""
                        {"user":{%s}}
                        """.formatted(fields(user)))
                .post("/api/users");
    }

    static String fields(Map<String, String> user) {
        return user.entrySet().stream()
                .map(e -> "\"%s\":\"%s\"".formatted(e.getKey(), e.getValue()))
                .reduce((a, b) -> a + "," + b)
                .orElse("");
    }
}
