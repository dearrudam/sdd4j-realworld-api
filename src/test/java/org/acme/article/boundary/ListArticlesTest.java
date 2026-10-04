package org.acme.article.boundary;

import static org.acme.article.ArticleRequirement.Rn.R1_1;
import static org.acme.article.ArticleRequirement.Rn.R1_10;
import static org.acme.article.ArticleRequirement.Rn.R1_11;
import static org.acme.article.ArticleRequirement.Rn.R1_12;
import static org.acme.article.ArticleRequirement.Rn.R1_2;
import static org.acme.article.ArticleRequirement.Rn.R1_3;
import static org.acme.article.ArticleRequirement.Rn.R1_4;
import static org.acme.article.ArticleRequirement.Rn.R1_5;
import static org.acme.article.ArticleRequirement.Rn.R1_6;
import static org.acme.article.ArticleRequirement.Rn.R1_7;
import static org.acme.article.ArticleRequirement.Rn.R1_8;
import static org.acme.article.ArticleRequirement.Rn.R1_9;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import io.quarkus.test.junit.QuarkusTest;
import java.util.Comparator;
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
class ListArticlesTest {

    enum Filter {
        TAG, AUTHOR, FAVORITED, GHOST_AUTHOR, GHOST_FAVORITED
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("listingCases")
    void listing(ArticleRequirement.Rn requirement, String key, int created) {
        var token = ArticleApi.register("a-" + key + "-l");
        var slugs = IntStream.range(0, created)
                .mapToObj(i -> ArticleApi.publish(token, "List " + i + " " + key, null))
                .toList();
        var response = ArticleApi.list(Auth.ANONYMOUS, null, Map.of());
        assertThat(response.statusCode())
                .as(requirement + " — " + requirement.statement())
                .isEqualTo(200);
        var json = response.jsonPath();
        assertThat(json.getList("articles.slug").subList(0, created))
                .containsExactlyElementsOf(slugs.reversed());
        assertThat(json.getLong("articlesCount")).isGreaterThanOrEqualTo(created);
        assertThat(json.getList("articles.createdAt", String.class))
                .isSortedAccordingTo(Comparator.reverseOrder());
        assertThat(json.getString("articles[0].body")).isNull();
    }

    static Stream<Arguments> listingCases() {
        return Stream.of(arguments(R1_1, "r1-1", 3));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("filterCases")
    void filters(ArticleRequirement.Rn requirement, String key, Filter filter) {
        var authorA = "a-" + key + "-a";
        var authorB = "a-" + key + "-b";
        var tokenA = ArticleApi.register(authorA);
        var tokenB = ArticleApi.register(authorB);
        var a1 = ArticleApi.publish(tokenA, "First " + key, List.of("tag-" + key));
        var a2 = ArticleApi.publish(tokenA, "Second " + key, List.of("noise-" + key));
        var b1 = ArticleApi.publish(tokenB, "Third " + key, List.of("tag-" + key));
        if (filter == Filter.FAVORITED) {
            ArticleApi.favorite(tokenB, a1);
        }
        var params = switch (filter) {
            case TAG -> Map.of("tag", "tag-" + key);
            case AUTHOR -> Map.of("author", authorA);
            case FAVORITED -> Map.of("favorited", authorB);
            case GHOST_AUTHOR -> Map.of("author", "ghost-" + key);
            case GHOST_FAVORITED -> Map.of("favorited", "ghost-" + key);
        };
        var expected = switch (filter) {
            case TAG -> List.of(b1, a1);
            case AUTHOR -> List.of(a2, a1);
            case FAVORITED -> List.of(a1);
            default -> List.of();
        };
        var response = ArticleApi.list(Auth.ANONYMOUS, null, params);
        assertThat(response.statusCode())
                .as(requirement + " — " + requirement.statement())
                .isEqualTo(200);
        assertThat(response.jsonPath().getList("articles.slug"))
                .containsExactlyElementsOf(expected);
        assertThat(response.jsonPath().getLong("articlesCount")).isEqualTo(expected.size());
    }

    static Stream<Arguments> filterCases() {
        return Stream.of(
                arguments(R1_2, "r1-2", Filter.TAG),
                arguments(R1_3, "r1-3", Filter.AUTHOR),
                arguments(R1_4, "r1-4", Filter.FAVORITED),
                arguments(R1_5, "r1-5a", Filter.GHOST_AUTHOR),
                arguments(R1_5, "r1-5b", Filter.GHOST_FAVORITED));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("pageCases")
    void pages(ArticleRequirement.Rn requirement, String key, Integer limit, Integer offset, int created,
            int expectedSize) {
        var token = ArticleApi.register("a-" + key + "-p");
        var slugs = IntStream.range(0, created)
                .mapToObj(i -> ArticleApi.publish(token, "Page " + i + " " + key, List.of("page-" + key)))
                .toList();
        var params = new LinkedHashMap<String, Object>();
        params.put("tag", "page-" + key);
        if (limit != null) {
            params.put("limit", limit);
        }
        if (offset != null) {
            params.put("offset", offset);
        }
        var response = ArticleApi.list(Auth.ANONYMOUS, null, params);
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
                arguments(R1_6, "r1-6a", 2, null, 3, 2),
                arguments(R1_6, "r1-6b", null, null, 21, 20),
                arguments(R1_7, "r1-7a", null, 1, 3, 2),
                arguments(R1_7, "r1-7b", null, null, 2, 2));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("presentationCases")
    void presentation(ArticleRequirement.Rn requirement, String key, Auth auth, boolean follows,
            int expectedStatus, boolean expectedFollowing, String expectedErrorField) {
        var callerToken = auth == Auth.ANONYMOUS ? null : ArticleApi.register("a-" + key + "-caller");
        var author = "a-" + key + "-author";
        var authorToken = ArticleApi.register(author);
        ArticleApi.publish(authorToken, "Shown " + key, null);
        if (follows) {
            ArticleApi.follow(callerToken, author);
        }
        var response = ArticleApi.list(auth, callerToken, Map.of("author", author));
        assertThat(response.statusCode())
                .as(requirement + " — " + requirement.statement())
                .isEqualTo(expectedStatus);
        if (expectedErrorField != null) {
            assertThat(response.jsonPath().getMap("errors")).containsKey(expectedErrorField);
        } else {
            assertThat(response.jsonPath().getBoolean("articles[0].author.following"))
                    .isEqualTo(expectedFollowing);
        }
    }

    static Stream<Arguments> presentationCases() {
        return Stream.of(
                arguments(R1_8, "r1-8a", Auth.VALID, true, 200, true, null),
                arguments(R1_8, "r1-8b", Auth.VALID, false, 200, false, null),
                arguments(R1_9, "r1-9", Auth.ANONYMOUS, false, 200, false, null),
                arguments(R1_10, "r1-10", Auth.INVALID, false, 401, false, "token"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("favoriteCases")
    void favoriteMarks(ArticleRequirement.Rn requirement, String key, Auth auth,
            boolean expectedFavorited, int expectedCount) {
        var author = "a-" + key + "-author";
        var authorToken = ArticleApi.register(author);
        var loved = ArticleApi.publish(authorToken, "Loved " + key, null);
        var plain = ArticleApi.publish(authorToken, "Plain " + key, null);
        var callerToken = auth == Auth.ANONYMOUS ? null : ArticleApi.register("a-" + key + "-caller");
        var otherToken = ArticleApi.register("a-" + key + "-other");
        if (callerToken != null) {
            ArticleApi.favorite(callerToken, loved);
        }
        ArticleApi.favorite(otherToken, loved);
        var response = ArticleApi.list(auth, callerToken, Map.of("author", author));
        assertThat(response.statusCode())
                .as(requirement + " — " + requirement.statement())
                .isEqualTo(200);
        var json = response.jsonPath();
        assertThat(json.getList("articles.slug")).containsExactly(plain, loved);
        assertThat(json.getBoolean("articles[0].favorited")).isFalse();
        assertThat(json.getInt("articles[0].favoritesCount")).isZero();
        assertThat(json.getBoolean("articles[1].favorited")).isEqualTo(expectedFavorited);
        assertThat(json.getInt("articles[1].favoritesCount")).isEqualTo(expectedCount);
    }

    static Stream<Arguments> favoriteCases() {
        return Stream.of(
                arguments(R1_11, "r1-11", Auth.VALID, true, 2),
                arguments(R1_12, "r1-12", Auth.ANONYMOUS, false, 1));
    }
}
