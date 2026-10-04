package org.acme.article.boundary;

import static org.acme.article.ArticleRequirement.Rn.R3_1;
import static org.acme.article.ArticleRequirement.Rn.R3_2;
import static org.acme.article.ArticleRequirement.Rn.R3_3;
import static org.acme.article.ArticleRequirement.Rn.R3_4;
import static org.acme.article.ArticleRequirement.Rn.R3_5;
import static org.acme.article.ArticleRequirement.Rn.R3_6;
import static org.acme.article.ArticleRequirement.Rn.R3_7;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import io.quarkus.test.junit.QuarkusTest;
import java.util.List;
import java.util.stream.Stream;
import org.acme.article.ArticleRequirement;
import org.acme.article.boundary.ArticleApi.Auth;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@QuarkusTest
class GetArticleTest {

    enum Target {
        EXISTING, GHOST
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("cases")
    void getArticle(ArticleRequirement.Rn requirement, String key, Auth auth, boolean follows, Target target,
            int expectedStatus, boolean expectedFollowing, String expectedErrorField) {
        var callerToken = auth == Auth.ANONYMOUS ? null : ArticleApi.register("a-" + key + "-caller");
        var author = "a-" + key + "-author";
        var slug = "ghost-" + key;
        var title = "Get " + key;
        if (target == Target.EXISTING) {
            var authorToken = ArticleApi.register(author);
            ArticleApi.describe(authorToken, "bio-" + key, "https://img.test/" + key + ".png");
            slug = ArticleApi.publish(authorToken, title, List.of("one-" + key, "two-" + key));
            if (follows) {
                ArticleApi.follow(callerToken, author);
            }
        }
        var response = ArticleApi.get(slug, auth, callerToken);
        assertThat(response.statusCode())
                .as(requirement + " — " + requirement.statement())
                .isEqualTo(expectedStatus);
        if (expectedErrorField != null) {
            assertThat(response.jsonPath().getMap("errors")).containsKey(expectedErrorField);
        } else {
            var json = response.jsonPath();
            assertThat(json.getString("article.slug")).isEqualTo(slug);
            assertThat(json.getString("article.title")).isEqualTo(title);
            assertThat(json.getString("article.description")).isEqualTo("about " + title);
            assertThat(json.getString("article.body")).isEqualTo("body of " + title);
            assertThat(json.getList("article.tagList", String.class))
                    .containsExactly("one-" + key, "two-" + key);
            assertThat(json.getString("article.createdAt")).isNotNull();
            assertThat(json.getString("article.updatedAt")).isNotNull();
            assertThat(json.getBoolean("article.favorited")).isFalse();
            assertThat(json.getInt("article.favoritesCount")).isZero();
            assertThat(json.getString("article.author.username")).isEqualTo(author);
            assertThat(json.getString("article.author.bio")).isEqualTo("bio-" + key);
            assertThat(json.getString("article.author.image"))
                    .isEqualTo("https://img.test/" + key + ".png");
            assertThat(json.getBoolean("article.author.following")).isEqualTo(expectedFollowing);
        }
    }

    static Stream<Arguments> cases() {
        return Stream.of(
                arguments(R3_1, "r3-1", Auth.ANONYMOUS, false, Target.EXISTING, 200, false, null),
                arguments(R3_2, "r3-2", Auth.VALID, true, Target.EXISTING, 200, true, null),
                arguments(R3_3, "r3-3a", Auth.ANONYMOUS, false, Target.EXISTING, 200, false, null),
                arguments(R3_3, "r3-3b", Auth.VALID, false, Target.EXISTING, 200, false, null),
                arguments(R3_4, "r3-4", Auth.INVALID, false, Target.EXISTING, 401, false, "token"),
                arguments(R3_5, "r3-5", Auth.ANONYMOUS, false, Target.GHOST, 404, false, "article"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("favoriteCases")
    void favoriteMarks(ArticleRequirement.Rn requirement, String key, Auth auth, boolean markedByCaller,
            boolean expectedFavorited, int expectedCount) {
        var callerToken = auth == Auth.ANONYMOUS ? null : ArticleApi.register("a-" + key + "-caller");
        var authorToken = ArticleApi.register("a-" + key + "-author");
        var slug = ArticleApi.publish(authorToken, "Marked " + key, null);
        var markerToken = markedByCaller ? callerToken : ArticleApi.register("a-" + key + "-marker");
        ArticleApi.favorite(markerToken, slug);
        var response = ArticleApi.get(slug, auth, callerToken);
        assertThat(response.statusCode())
                .as(requirement + " — " + requirement.statement())
                .isEqualTo(200);
        var json = response.jsonPath();
        assertThat(json.getBoolean("article.favorited")).isEqualTo(expectedFavorited);
        assertThat(json.getInt("article.favoritesCount")).isEqualTo(expectedCount);
    }

    static Stream<Arguments> favoriteCases() {
        return Stream.of(
                arguments(R3_6, "r3-6", Auth.VALID, true, true, 1),
                arguments(R3_7, "r3-7a", Auth.ANONYMOUS, false, false, 1),
                arguments(R3_7, "r3-7b", Auth.VALID, false, false, 1));
    }
}
