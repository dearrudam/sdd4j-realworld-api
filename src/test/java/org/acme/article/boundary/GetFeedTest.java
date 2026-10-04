package org.acme.article.boundary;

import static org.acme.article.ArticleRequirement.Rn.R2_1;
import static org.acme.article.ArticleRequirement.Rn.R2_2;
import static org.acme.article.ArticleRequirement.Rn.R2_3;
import static org.acme.article.ArticleRequirement.Rn.R2_4;
import static org.acme.article.ArticleRequirement.Rn.R2_5;
import static org.acme.article.ArticleRequirement.Rn.R2_6;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import io.quarkus.test.junit.QuarkusTest;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.acme.article.ArticleRequirement;
import org.acme.article.boundary.ArticleApi.Auth;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@QuarkusTest
class GetFeedTest {

    enum Following {
        AUTHOR, ARTICLELESS, NOBODY
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("contentCases")
    void contents(ArticleRequirement.Rn requirement, String key, Following target, boolean assertFollowing) {
        var caller = ArticleApi.register("a-" + key + "-c");
        var author = "a-" + key + "-a";
        var authorToken = ArticleApi.register(author);
        var a1 = ArticleApi.publish(authorToken, "Feed " + 1 + " " + key, null);
        var a2 = ArticleApi.publish(authorToken, "Feed " + 2 + " " + key, null);
        var otherToken = ArticleApi.register("a-" + key + "-o");
        ArticleApi.publish(otherToken, "Stray " + key, null);
        var articleless = "a-" + key + "-empty";
        ArticleApi.register(articleless);
        switch (target) {
            case AUTHOR -> ArticleApi.follow(caller, author);
            case ARTICLELESS -> ArticleApi.follow(caller, articleless);
            case NOBODY -> {
            }
        }
        var expected = target == Following.AUTHOR ? List.of(a2, a1) : List.of();
        var response = ArticleApi.feed(Auth.VALID, caller, Map.of());
        assertThat(response.statusCode())
                .as(requirement + " — " + requirement.statement())
                .isEqualTo(200);
        assertThat(response.jsonPath().getList("articles.slug"))
                .containsExactlyElementsOf(expected);
        assertThat(response.jsonPath().getLong("articlesCount")).isEqualTo(expected.size());
        if (assertFollowing) {
            assertThat(response.jsonPath().getList("articles.author.following", Boolean.class))
                    .allMatch(following -> following);
        }
    }

    static Stream<Arguments> contentCases() {
        return Stream.of(
                arguments(R2_1, "r2-1", Following.AUTHOR, false),
                arguments(R2_2, "r2-2a", Following.NOBODY, false),
                arguments(R2_2, "r2-2b", Following.ARTICLELESS, false),
                arguments(R2_4, "r2-4", Following.AUTHOR, true));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("pageCases")
    void pages(ArticleRequirement.Rn requirement, String key, Integer limit, Integer offset, int created,
            int expectedSize) {
        var caller = ArticleApi.register("a-" + key + "-c");
        var authorToken = ArticleApi.register("a-" + key + "-a");
        var slugs = IntStream.range(0, created)
                .mapToObj(i -> ArticleApi.publish(authorToken, "FeedPage " + i + " " + key, null))
                .toList();
        ArticleApi.follow(caller, "a-" + key + "-a");
        var params = new LinkedHashMap<String, Object>();
        if (limit != null) {
            params.put("limit", limit);
        }
        if (offset != null) {
            params.put("offset", offset);
        }
        var response = ArticleApi.feed(Auth.VALID, caller, params);
        assertThat(response.statusCode())
                .as(requirement + " — " + requirement.statement())
                .isEqualTo(200);
        var skip = offset == null ? 0 : offset;
        assertThat(response.jsonPath().getList("articles.slug"))
                .containsExactlyElementsOf(slugs.reversed().subList(skip, skip + expectedSize));
        assertThat(response.jsonPath().getLong("articlesCount")).isEqualTo(created);
    }

    static Stream<Arguments> pageCases() {
        return Stream.of(
                arguments(R2_3, "r2-3a", 2, null, 3, 2),
                arguments(R2_3, "r2-3b", null, 1, 3, 2),
                arguments(R2_3, "r2-3c", null, null, 2, 2));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("rejectCases")
    void rejects(ArticleRequirement.Rn requirement, String key, Auth auth, int expectedStatus) {
        var caller = auth == Auth.ANONYMOUS ? null : ArticleApi.register("a-" + key + "-c");
        var response = ArticleApi.feed(auth, caller, Map.of());
        assertThat(response.statusCode())
                .as(requirement + " — " + requirement.statement())
                .isEqualTo(expectedStatus);
        assertThat(response.jsonPath().getMap("errors")).containsKey("token");
    }

    static Stream<Arguments> rejectCases() {
        return Stream.of(
                arguments(R2_5, "r2-5a", Auth.ANONYMOUS, 401),
                arguments(R2_5, "r2-5b", Auth.INVALID, 401));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("favoriteCases")
    void favoriteMarks(ArticleRequirement.Rn requirement, String key) {
        var caller = ArticleApi.register("a-" + key + "-c");
        var author = "a-" + key + "-a";
        var authorToken = ArticleApi.register(author);
        var loved = ArticleApi.publish(authorToken, "Loved " + key, null);
        var plain = ArticleApi.publish(authorToken, "Plain " + key, null);
        ArticleApi.follow(caller, author);
        ArticleApi.favorite(caller, loved);
        var response = ArticleApi.feed(Auth.VALID, caller, Map.of());
        assertThat(response.statusCode())
                .as(requirement + " — " + requirement.statement())
                .isEqualTo(200);
        var json = response.jsonPath();
        assertThat(json.getList("articles.slug")).containsExactly(plain, loved);
        assertThat(json.getBoolean("articles[0].favorited")).isFalse();
        assertThat(json.getInt("articles[0].favoritesCount")).isZero();
        assertThat(json.getBoolean("articles[1].favorited")).isTrue();
        assertThat(json.getInt("articles[1].favoritesCount")).isEqualTo(1);
    }

    static Stream<Arguments> favoriteCases() {
        return Stream.of(arguments(R2_6, "r2-6"));
    }
}
