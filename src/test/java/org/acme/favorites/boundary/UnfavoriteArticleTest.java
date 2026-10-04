package org.acme.favorites.boundary;

import static org.acme.favorites.FavoritesRequirement.Rn.R2_1;
import static org.acme.favorites.FavoritesRequirement.Rn.R2_2;
import static org.acme.favorites.FavoritesRequirement.Rn.R2_3;
import static org.acme.favorites.FavoritesRequirement.Rn.R2_4;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import io.quarkus.test.junit.QuarkusTest;
import java.util.stream.Stream;
import org.acme.favorites.FavoritesRequirement;
import org.acme.favorites.boundary.FavoritesApi.Auth;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@QuarkusTest
class UnfavoriteArticleTest {

    enum Target {
        EXISTING, GHOST
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("unmarkCases")
    void unmarks(FavoritesRequirement.Rn requirement, String key, boolean marked,
            int expectedStatus, boolean expectedFavorited, int expectedCount) {
        var authorToken = FavoritesApi.register("f-" + key + "-author");
        var slug = FavoritesApi.publish(authorToken, "Unmarked " + key);
        var callerToken = FavoritesApi.register("f-" + key + "-caller");
        if (marked) {
            FavoritesApi.favorite(callerToken, slug);
        }
        var response = FavoritesApi.unfavorite(slug, Auth.VALID, callerToken);
        assertThat(response.statusCode())
                .as(requirement + " — " + requirement.statement())
                .isEqualTo(expectedStatus);
        var json = response.jsonPath();
        assertThat(json.getBoolean("article.favorited")).isEqualTo(expectedFavorited);
        assertThat(json.getInt("article.favoritesCount")).isEqualTo(expectedCount);
        var fresh = FavoritesApi.get(slug, Auth.VALID, callerToken).jsonPath();
        assertThat(fresh.getBoolean("article.favorited")).isEqualTo(expectedFavorited);
        assertThat(fresh.getInt("article.favoritesCount")).isEqualTo(expectedCount);
    }

    static Stream<Arguments> unmarkCases() {
        return Stream.of(
                arguments(R2_1, "r2-1", true, 200, false, 0),
                arguments(R2_2, "r2-2", false, 200, false, 0));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("rejectCases")
    void rejects(FavoritesRequirement.Rn requirement, String key, Auth auth, Target target,
            int expectedStatus, String expectedErrorField) {
        var slug = "ghost-" + key;
        if (target == Target.EXISTING) {
            var ownerToken = FavoritesApi.register("f-" + key + "-owner");
            slug = FavoritesApi.publish(ownerToken, "Unmarked " + key);
        }
        var callerToken = auth == Auth.ANONYMOUS ? null : FavoritesApi.register("f-" + key + "-caller");
        var response = FavoritesApi.unfavorite(slug, auth, callerToken);
        assertThat(response.statusCode())
                .as(requirement + " — " + requirement.statement())
                .isEqualTo(expectedStatus);
        assertThat(response.jsonPath().getMap("errors")).containsKey(expectedErrorField);
    }

    static Stream<Arguments> rejectCases() {
        return Stream.of(
                arguments(R2_3, "r2-3", Auth.VALID, Target.GHOST, 404, "article"),
                arguments(R2_4, "r2-4a", Auth.ANONYMOUS, Target.EXISTING, 401, "token"),
                arguments(R2_4, "r2-4b", Auth.INVALID, Target.EXISTING, 401, "token"));
    }
}
