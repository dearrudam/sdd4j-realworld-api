package org.acme.user.boundary;

import static io.restassured.RestAssured.given;
import static org.acme.user.UserRequirement.Rn.R4_1;
import static org.acme.user.UserRequirement.Rn.R4_10;
import static org.acme.user.UserRequirement.Rn.R4_11;
import static org.acme.user.UserRequirement.Rn.R4_2;
import static org.acme.user.UserRequirement.Rn.R4_5;
import static org.acme.user.UserRequirement.Rn.R4_6;
import static org.acme.user.UserRequirement.Rn.R4_7;
import static org.acme.user.UserRequirement.Rn.R4_8;
import static org.acme.user.UserRequirement.Rn.R4_9;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;
import org.acme.user.UserRequirement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@QuarkusTest
class UpdateUserTest {

    @ParameterizedTest(name = "{0}")
    @MethodSource("cases")
    void update(UserRequirement.Rn requirement, String key, Map<String, String> seed, boolean authenticated,
            Map<String, Object> update, int expectedStatus, String expectedErrorField) {
        if (seed != null) {
            register(seed.get("username"), seed.get("email"));
        }
        var me = "me-" + key;
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
            update.forEach((field, value) -> {
                if (!"password".equals(field)) {
                    assertThat(response.jsonPath().getString("user." + field))
                            .isEqualTo(normalized(field, value));
                }
            });
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

    @Test
    @UserRequirement(R4_10)
    void clearsPreviouslySetBioAndImage() {
        var token = register("me-r4-10-clear", "me-r4-10-clear@conduit.test");
        var set = put(token, "\"bio\":\"bio-set\",\"image\":\"img-set\"");
        assertThat(set.statusCode()).isEqualTo(200);
        assertThat(set.jsonPath().getString("user.bio")).isEqualTo("bio-set");
        assertThat(set.jsonPath().getString("user.image")).isEqualTo("img-set");
        var clearedByNull = put(token, "\"bio\":null,\"image\":\"\"");
        assertThat(clearedByNull.statusCode()).isEqualTo(200);
        assertThat(clearedByNull.jsonPath().getString("user.bio")).isNull();
        assertThat(clearedByNull.jsonPath().getString("user.image")).isNull();
        put(token, "\"bio\":\"bio-again\",\"image\":\"img-again\"");
        var clearedByBlank = put(token, "\"bio\":\"\",\"image\":null");
        assertThat(clearedByBlank.jsonPath().getString("user.bio")).isNull();
        assertThat(clearedByBlank.jsonPath().getString("user.image")).isNull();
    }

    static Stream<Arguments> cases() {
        return Stream.of(
                arguments(R4_1, "r4-1", null, true,
                        map("bio", "bio-r4-1", "image", "https://img.test/r4-1"), 200, null),
                arguments(R4_2, "r4-2", null, false,
                        map("bio", "bio-r4-2"), 401, "token"),
                arguments(R4_5, "r4-5",
                        Map.of("username", "taken-r4-5", "email", "seed-r4-5@conduit.test"), true,
                        map("username", "taken-r4-5"), 422, "username"),
                arguments(R4_6, "r4-6", null, true,
                        map(), 422, "user"),
                arguments(R4_7, "r4-7",
                        Map.of("username", "seed-r4-7", "email", "taken-r4-7@conduit.test"), true,
                        map("email", "taken-r4-7@conduit.test"), 422, "email"),
                arguments(R4_8, "r4-8a", null, true, map("username", ""), 422, "username"),
                arguments(R4_8, "r4-8b", null, true, map("email", ""), 422, "email"),
                arguments(R4_8, "r4-8c", null, true, map("password", ""), 422, "password"),
                arguments(R4_9, "r4-9a", null, true, map("username", null), 422, "username"),
                arguments(R4_9, "r4-9b", null, true, map("email", null), 422, "email"),
                arguments(R4_9, "r4-9c", null, true, map("password", null), 422, "password"),
                arguments(R4_10, "r4-10a", null, true, map("bio", ""), 200, null),
                arguments(R4_10, "r4-10b", null, true, map("image", null), 200, null),
                arguments(R4_11, "r4-11a", null, true, map("password", "short7c"), 422, "password"),
                arguments(R4_11, "r4-11b", null, true, map("password", "long-enough-1"), 200, null));
    }

    static Object normalized(String field, Object provided) {
        return ("bio".equals(field) || "image".equals(field))
                && (provided == null || provided.toString().isBlank()) ? null : provided;
    }

    static Response put(String token, String userFields) {
        return given()
                .header("Authorization", "Token " + token)
                .contentType(ContentType.JSON)
                .body("{\"user\":{%s}}".formatted(userFields))
                .put("/api/user");
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

    static Map<String, Object> map(Object... kv) {
        var map = new LinkedHashMap<String, Object>();
        for (var i = 0; i < kv.length; i += 2) {
            map.put((String) kv[i], kv[i + 1]);
        }
        return map;
    }

    static String fields(Map<String, Object> update) {
        return update.entrySet().stream()
                .map(e -> e.getValue() == null
                        ? "\"%s\":null".formatted(e.getKey())
                        : "\"%s\":\"%s\"".formatted(e.getKey(), e.getValue()))
                .reduce((a, b) -> a + "," + b)
                .orElse("");
    }
}
