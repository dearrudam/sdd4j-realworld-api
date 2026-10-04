package org.acme.tags.boundary;

import static io.restassured.RestAssured.given;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class TagsApi {

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

    static String publish(String token, String title, List<String> tags) {
        var article = new LinkedHashMap<String, Object>();
        article.put("title", title);
        article.put("description", "about " + title);
        article.put("body", "body of " + title);
        if (tags != null) {
            article.put("tagList", tags);
        }
        return request(Auth.VALID, token)
                .body(Map.of("article", article))
                .post("/api/articles")
                .then()
                .statusCode(201)
                .extract()
                .jsonPath()
                .getString("article.slug");
    }

    static void delete(String token, String slug) {
        request(Auth.VALID, token)
                .delete("/api/articles/" + slug)
                .then()
                .statusCode(204);
    }

    static Response list(Auth auth, String token) {
        return request(auth, token).get("/api/tags");
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
