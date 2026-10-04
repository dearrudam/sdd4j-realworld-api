package org.acme.article.boundary;

import static org.acme.article.ArticleRequirement.Rn.R4_1;
import static org.acme.article.ArticleRequirement.Rn.R4_2;
import static org.acme.article.ArticleRequirement.Rn.R4_3;
import static org.acme.article.ArticleRequirement.Rn.R4_4;
import static org.acme.article.ArticleRequirement.Rn.R4_5;
import static org.acme.article.ArticleRequirement.Rn.R4_6;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import io.quarkus.test.junit.QuarkusTest;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.acme.article.ArticleRequirement;
import org.acme.article.boundary.ArticleApi.Auth;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@QuarkusTest
class CreateArticleTest {

    @ParameterizedTest(name = "{0}")
    @MethodSource("createCases")
    void creates(ArticleRequirement.Rn requirement, String key, boolean withTags, List<String> expectedTags,
            boolean reGet) {
        var caller = "a-" + key + "-author";
        var token = ArticleApi.register(caller);
        var title = "How to " + key;
        var article = new LinkedHashMap<String, Object>();
        article.put("title", title);
        article.put("description", "desc " + key);
        article.put("body", "body " + key);
        if (withTags) {
            article.put("tagList", expectedTags);
        }
        var response = ArticleApi.create(Auth.VALID, token, article);
        assertThat(response.statusCode())
                .as(requirement + " — " + requirement.statement())
                .isEqualTo(201);
        var json = response.jsonPath();
        var slug = "how-to-" + key;
        assertThat(json.getString("article.slug")).isEqualTo(slug);
        assertThat(json.getString("article.title")).isEqualTo(title);
        assertThat(json.getString("article.description")).isEqualTo("desc " + key);
        assertThat(json.getString("article.body")).isEqualTo("body " + key);
        assertThat(json.getList("article.tagList", String.class)).containsExactlyElementsOf(expectedTags);
        assertThat(json.getString("article.createdAt")).isNotNull();
        assertThat(json.getString("article.updatedAt")).isNotNull();
        assertThat(json.getBoolean("article.favorited")).isFalse();
        assertThat(json.getInt("article.favoritesCount")).isZero();
        assertThat(json.getString("article.author.username")).isEqualTo(caller);
        assertThat(json.getBoolean("article.author.following")).isFalse();
        if (reGet) {
            assertThat(ArticleApi.get(slug, Auth.ANONYMOUS, null).statusCode()).isEqualTo(200);
        }
    }

    static Stream<Arguments> createCases() {
        return Stream.of(
                arguments(R4_1, "r4-1", true, List.of("howto", "test"), true),
                arguments(R4_2, "r4-2a", true, List.of("one-r4-2a", "two-r4-2a"), false),
                arguments(R4_2, "r4-2b", false, List.of(), false),
                arguments(R4_3, "r4-3", false, List.of(), false));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("rejectCases")
    void rejects(ArticleRequirement.Rn requirement, String key, Auth auth, Map<String, Object> article,
            boolean prime, int expectedStatus, String expectedErrorField) {
        var token = auth == Auth.ANONYMOUS ? null : ArticleApi.register("a-" + key + "-caller");
        var body = article != null ? article : article("Taken " + key, "d", "b");
        if (prime) {
            ArticleApi.create(Auth.VALID, token, body).then().statusCode(201);
        }
        var response = ArticleApi.create(auth, token, body);
        assertThat(response.statusCode())
                .as(requirement + " — " + requirement.statement())
                .isEqualTo(expectedStatus);
        assertThat(response.jsonPath().getMap("errors")).containsKey(expectedErrorField);
    }

    static Stream<Arguments> rejectCases() {
        return Stream.of(
                arguments(R4_4, "r4-4a", Auth.VALID, article(null, "d", "b"), false, 422, "title"),
                arguments(R4_4, "r4-4b", Auth.VALID, article("T", "", "b"), false, 422, "description"),
                arguments(R4_4, "r4-4c", Auth.VALID, article("T", "d", null), false, 422, "body"),
                arguments(R4_6, "r4-6a", Auth.ANONYMOUS, article("T", "d", "b"), false, 401, "token"),
                arguments(R4_6, "r4-6b", Auth.INVALID, article("T", "d", "b"), false, 401, "token"));
    }

    @Test
    @ArticleRequirement(R4_5)
    void duplicateTitleGetsDistinctSlug() {
        var token = ArticleApi.register("a-r4-5-author");
        var body = article("Dup R4.5", "d", "b");
        var first = ArticleApi.create(Auth.VALID, token, body);
        var second = ArticleApi.create(Auth.VALID, token, body);
        assertThat(first.statusCode()).isEqualTo(201);
        assertThat(second.statusCode()).isEqualTo(201);
        assertThat(second.jsonPath().getString("article.slug"))
                .isNotEqualTo(first.jsonPath().getString("article.slug"));
    }

    static Map<String, Object> article(String title, String description, String body) {
        var article = new LinkedHashMap<String, Object>();
        if (title != null) {
            article.put("title", title);
        }
        if (description != null) {
            article.put("description", description);
        }
        if (body != null) {
            article.put("body", body);
        }
        return article;
    }
}
