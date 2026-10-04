package org.acme.article.boundary;

import static io.restassured.RestAssured.given;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class ArticleApi {

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

    static void follow(String token, String username) {
        request(Auth.VALID, token)
                .post("/api/profiles/" + username + "/follow")
                .then()
                .statusCode(200);
    }

    static String publish(String token, String title, List<String> tags) {
        var article = new LinkedHashMap<String, Object>();
        article.put("title", title);
        article.put("description", "about " + title);
        article.put("body", "body of " + title);
        if (tags != null) {
            article.put("tagList", tags);
        }
        return create(Auth.VALID, token, article)
                .then()
                .statusCode(201)
                .extract()
                .jsonPath()
                .getString("article.slug");
    }

    static Response create(Auth auth, String token, Map<String, ?> article) {
        return request(auth, token).body(Map.of("article", article)).post("/api/articles");
    }

    static Response list(Auth auth, String token, Map<String, ?> params) {
        return request(auth, token).queryParams(params).get("/api/articles");
    }

    static Response feed(Auth auth, String token, Map<String, ?> params) {
        return request(auth, token).queryParams(params).get("/api/articles/feed");
    }

    static Response get(String slug, Auth auth, String token) {
        return request(auth, token).get("/api/articles/" + slug);
    }

    static void favorite(String token, String slug) {
        favorite(slug, Auth.VALID, token)
                .then()
                .statusCode(200);
    }

    static Response favorite(String slug, Auth auth, String token) {
        return request(auth, token).post("/api/articles/" + slug + "/favorite");
    }

    static Response unfavorite(String slug, Auth auth, String token) {
        return request(auth, token).delete("/api/articles/" + slug + "/favorite");
    }

    static Response update(String slug, Auth auth, String token, Map<String, ?> article) {
        return request(auth, token).body(Map.of("article", article)).put("/api/articles/" + slug);
    }

    static Response delete(String slug, Auth auth, String token) {
        return request(auth, token).delete("/api/articles/" + slug);
    }

    static void comment(String token, String slug, String body) {
        request(Auth.VALID, token)
                .body("{\"comment\":{\"body\":\"%s\"}}".formatted(body))
                .post("/api/articles/" + slug + "/comments")
                .then()
                .statusCode(201);
    }

    static Response listComments(String slug, Auth auth, String token) {
        return request(auth, token).get("/api/articles/" + slug + "/comments");
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
