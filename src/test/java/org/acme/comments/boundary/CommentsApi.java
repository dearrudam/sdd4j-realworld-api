package org.acme.comments.boundary;

import static io.restassured.RestAssured.given;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

final class CommentsApi {

    enum Auth {
        ANONYMOUS, INVALID, VALID
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

    static String publish(String token, String title) {
        return request(Auth.VALID, token)
                .body("""
                        {"article":{"title":"%s","description":"about %s","body":"body of %s"}}
                        """.formatted(title, title, title))
                .post("/api/articles")
                .then()
                .statusCode(201)
                .extract()
                .jsonPath()
                .getString("article.slug");
    }

    static void follow(String token, String username) {
        request(Auth.VALID, token)
                .post("/api/profiles/" + username + "/follow")
                .then()
                .statusCode(200);
    }

    static long comment(String token, String slug, String body) {
        return create(slug, Auth.VALID, token, "{\"comment\":{\"body\":\"%s\"}}".formatted(body))
                .then()
                .statusCode(201)
                .extract()
                .jsonPath()
                .getLong("comment.id");
    }

    static Response create(String slug, Auth auth, String token, String payload) {
        return request(auth, token).body(payload).post("/api/articles/" + slug + "/comments");
    }

    static Response list(String slug, Auth auth, String token) {
        return request(auth, token).get("/api/articles/" + slug + "/comments");
    }

    static Response delete(String slug, long id, Auth auth, String token) {
        return request(auth, token).delete("/api/articles/" + slug + "/comments/" + id);
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
