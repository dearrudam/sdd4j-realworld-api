package org.acme.article.boundary;

import static org.acme.article.ArticleRequirement.Rn.R5_1;
import static org.acme.article.ArticleRequirement.Rn.R5_2;
import static org.acme.article.ArticleRequirement.Rn.R5_3;
import static org.acme.article.ArticleRequirement.Rn.R5_4;
import static org.acme.article.ArticleRequirement.Rn.R5_5;
import static org.acme.article.ArticleRequirement.Rn.R5_6;
import static org.acme.article.ArticleRequirement.Rn.R5_7;
import static org.acme.article.ArticleRequirement.Rn.R5_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import io.quarkus.test.junit.QuarkusTest;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.acme.article.ArticleRequirement;
import org.acme.article.boundary.ArticleApi.Auth;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@QuarkusTest
class UpdateArticleTest {

    enum Actor {
        AUTHOR, OTHER
    }

    enum Target {
        EXISTING, GHOST
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("updateCases")
    void updates(ArticleRequirement.Rn requirement, String key, Map<String, Object> changes,
            boolean expectRefreshed) {
        var token = ArticleApi.register("a-" + key + "-author");
        var originalTitle = "Orig " + key;
        var created = ArticleApi
                .create(Auth.VALID, token, Map.of("title", originalTitle, "description", "od " + key, "body",
                        "ob " + key, "tagList", List.of("orig-" + key)))
                .jsonPath();
        var slug = created.getString("article.slug");
        var updatedAt = created.getString("article.updatedAt");
        var response = ArticleApi.update(slug, Auth.VALID, token, changes);
        assertThat(response.statusCode())
                .as(requirement + " — " + requirement.statement())
                .isEqualTo(200);
        var json = response.jsonPath();
        assertThat(json.getString("article.slug")).isEqualTo(slug);
        assertThat(json.getString("article.title")).isEqualTo(changes.getOrDefault("title", originalTitle));
        assertThat(json.getString("article.description"))
                .isEqualTo(changes.getOrDefault("description", "od " + key));
        assertThat(json.getString("article.body")).isEqualTo(changes.getOrDefault("body", "ob " + key));
        assertThat(json.getList("article.tagList", String.class))
                .containsExactlyElementsOf(expectedTags(changes, "orig-" + key));
        if (expectRefreshed) {
            assertThat(json.getString("article.updatedAt")).isNotEqualTo(updatedAt);
        } else {
            assertThat(json.getString("article.updatedAt")).isEqualTo(updatedAt);
        }
    }

    @SuppressWarnings("unchecked")
    static List<String> expectedTags(Map<String, Object> changes, String original) {
        return changes.containsKey("tagList") ? (List<String>) changes.get("tagList") : List.of(original);
    }

    static Stream<Arguments> updateCases() {
        return Stream.of(
                arguments(R5_1, "r5-1", Map.of("title", "New r5-1", "description", "nd r5-1", "body", "nb r5-1",
                        "tagList", List.of("new-r5-1")), true),
                arguments(R5_2, "r5-2", Map.of("title", "Renamed r5-2"), true),
                arguments(R5_3, "r5-3", Map.of(), false));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("rejectCases")
    @SuppressWarnings("unchecked")
    void rejects(ArticleRequirement.Rn requirement, String key, Auth auth, Actor actor, Target target,
            Object changes, int expectedStatus, String expectedErrorField) {
        String ownerToken = null;
        var slug = "ghost-" + key;
        if (target == Target.EXISTING) {
            ownerToken = ArticleApi.register("a-" + key + "-owner");
            slug = ArticleApi.publish(ownerToken, "Own " + key, null);
        }
        String callerToken = switch (auth) {
            case ANONYMOUS, INVALID -> null;
            case VALID -> actor == Actor.AUTHOR ? ownerToken : ArticleApi.register("a-" + key + "-other");
        };
        var response = changes instanceof String json
                ? ArticleApi.update(slug, auth, callerToken, json)
                : ArticleApi.update(slug, auth, callerToken, (Map<String, Object>) changes);
        assertThat(response.statusCode())
                .as(requirement + " — " + requirement.statement())
                .isEqualTo(expectedStatus);
        assertThat(response.jsonPath().getMap("errors")).containsKey(expectedErrorField);
    }

    static Stream<Arguments> rejectCases() {
        return Stream.of(
                arguments(R5_4, "r5-4a", Auth.VALID, Actor.AUTHOR, Target.EXISTING, blank("title"), 422, "title"),
                arguments(R5_4, "r5-4b", Auth.VALID, Actor.AUTHOR, Target.EXISTING, blank("body"), 422, "body"),
                arguments(R5_5, "r5-5a", Auth.ANONYMOUS, Actor.OTHER, Target.EXISTING, change(), 401, "token"),
                arguments(R5_5, "r5-5b", Auth.INVALID, Actor.OTHER, Target.EXISTING, change(), 401, "token"),
                arguments(R5_6, "r5-6", Auth.VALID, Actor.OTHER, Target.GHOST, change(), 404, "article"),
                arguments(R5_7, "r5-7", Auth.VALID, Actor.OTHER, Target.EXISTING, change(), 403, "article"),
                arguments(R5_8, "r5-8a", Auth.VALID, Actor.AUTHOR, Target.EXISTING, "{\"tagList\":null}", 422,
                        "tagList"),
                arguments(R5_8, "r5-8b", Auth.VALID, Actor.AUTHOR, Target.EXISTING, "{\"title\":null}", 422,
                        "title"));
    }

    static Map<String, Object> blank(String field) {
        var changes = new LinkedHashMap<String, Object>();
        changes.put(field, "");
        return changes;
    }

    static Map<String, Object> change() {
        return Map.of("title", "Anything");
    }
}
