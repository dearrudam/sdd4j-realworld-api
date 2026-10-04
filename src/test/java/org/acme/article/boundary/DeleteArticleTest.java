package org.acme.article.boundary;

import static org.acme.article.ArticleRequirement.Rn.R6_1;
import static org.acme.article.ArticleRequirement.Rn.R6_2;
import static org.acme.article.ArticleRequirement.Rn.R6_3;
import static org.acme.article.ArticleRequirement.Rn.R6_4;
import static org.acme.article.ArticleRequirement.Rn.R6_5;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import io.quarkus.test.junit.QuarkusTest;
import java.util.stream.Stream;
import org.acme.article.ArticleRequirement;
import org.acme.article.boundary.ArticleApi.Auth;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@QuarkusTest
class DeleteArticleTest {

    enum Actor {
        AUTHOR, OTHER
    }

    enum Target {
        EXISTING, GHOST
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("cases")
    void deleteArticle(ArticleRequirement.Rn requirement, String key, Auth auth, Actor actor, Target target,
            int expectedStatus, String expectedErrorField) {
        String ownerToken = null;
        var slug = "ghost-" + key;
        if (target == Target.EXISTING) {
            ownerToken = ArticleApi.register("a-" + key + "-owner");
            slug = ArticleApi.publish(ownerToken, "Doomed " + key, null);
        }
        String callerToken = switch (auth) {
            case ANONYMOUS, INVALID -> null;
            case VALID -> actor == Actor.AUTHOR ? ownerToken : ArticleApi.register("a-" + key + "-other");
        };
        var response = ArticleApi.delete(slug, auth, callerToken);
        assertThat(response.statusCode())
                .as(requirement + " — " + requirement.statement())
                .isEqualTo(expectedStatus);
        if (expectedErrorField != null) {
            assertThat(response.jsonPath().getMap("errors")).containsKey(expectedErrorField);
        } else {
            assertThat(response.getBody().asString()).isEmpty();
            assertThat(ArticleApi.get(slug, Auth.ANONYMOUS, null).statusCode()).isEqualTo(404);
        }
    }

    static Stream<Arguments> cases() {
        return Stream.of(
                arguments(R6_1, "r6-1", Auth.VALID, Actor.AUTHOR, Target.EXISTING, 204, null),
                arguments(R6_2, "r6-2a", Auth.ANONYMOUS, Actor.OTHER, Target.EXISTING, 401, "token"),
                arguments(R6_2, "r6-2b", Auth.INVALID, Actor.OTHER, Target.EXISTING, 401, "token"),
                arguments(R6_3, "r6-3", Auth.VALID, Actor.OTHER, Target.GHOST, 404, "article"),
                arguments(R6_4, "r6-4", Auth.VALID, Actor.OTHER, Target.EXISTING, 403, "article"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("favoriteCases")
    void favoriteMarksRemoved(ArticleRequirement.Rn requirement, String key) {
        var ownerToken = ArticleApi.register("a-" + key + "-owner");
        var slug = ArticleApi.publish(ownerToken, "Doomed " + key, null);
        var fanToken = ArticleApi.register("a-" + key + "-fan");
        ArticleApi.favorite(fanToken, slug);
        ArticleApi.delete(slug, Auth.VALID, ownerToken)
                .then()
                .statusCode(204);
        var recreated = ArticleApi.publish(ownerToken, "Doomed " + key, null);
        assertThat(recreated)
                .as(requirement + " — " + requirement.statement())
                .isEqualTo(slug);
        var json = ArticleApi.get(recreated, Auth.VALID, fanToken).jsonPath();
        assertThat(json.getBoolean("article.favorited")).isFalse();
        assertThat(json.getInt("article.favoritesCount")).isZero();
    }

    static Stream<Arguments> favoriteCases() {
        return Stream.of(arguments(R6_5, "r6-5"));
    }
}
