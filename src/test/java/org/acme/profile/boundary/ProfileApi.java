package org.acme.profile.boundary;

import static io.restassured.RestAssured.given;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

final class ProfileApi {

    enum Auth {
        ANONYMOUS, INVALID, VALID
    }

    enum Target {
        OTHER, SELF, GHOST
    }

    static String register(String username) {
        return given()
                .contentType(ContentType.JSON)
                .body("""
                        {"user":{"username":"%s","email":"%s@conduit.test","password":"pw-%s"}}
                        """.formatted(username, username, username))
                .post("/api/users")
                .jsonPath()
                .getString("user.token");
    }

    static void describe(String token, String bio, String image) {
        given()
                .header("Authorization", "Token " + token)
                .contentType(ContentType.JSON)
                .body("""
                        {"user":{"bio":"%s","image":"%s"}}
                        """.formatted(bio, image))
                .put("/api/user")
                .then()
                .statusCode(200);
    }

    static Response getProfile(Auth auth, String callerToken, String username) {
        return request(auth, callerToken).get("/api/profiles/" + username);
    }

    static Response follow(Auth auth, String callerToken, String username) {
        return request(auth, callerToken).post("/api/profiles/" + username + "/follow");
    }

    static Response unfollow(Auth auth, String callerToken, String username) {
        return request(auth, callerToken).delete("/api/profiles/" + username + "/follow");
    }

    static RequestSpecification request(Auth auth, String callerToken) {
        var request = given().contentType(ContentType.JSON);
        return switch (auth) {
            case ANONYMOUS -> request;
            case INVALID -> request.header("Authorization", "Token not-a-jwt");
            case VALID -> request.header("Authorization", "Token " + callerToken);
        };
    }
}
