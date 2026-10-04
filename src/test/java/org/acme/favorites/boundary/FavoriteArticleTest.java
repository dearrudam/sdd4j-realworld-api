package org.acme.favorites.boundary;

import static org.acme.favorites.FavoritesRequirement.Rn.R1_1;
import static org.acme.favorites.FavoritesRequirement.Rn.R1_2;
import static org.acme.favorites.FavoritesRequirement.Rn.R1_3;
import static org.acme.favorites.FavoritesRequirement.Rn.R1_4;
import static org.acme.favorites.FavoritesRequirement.Rn.R1_5;
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
class FavoriteArticleTest {

    enum Marker {
        OTHER, AUTHOR
    }

    enum Target {
        EXISTING, GHOST
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("markCases")
    void marks(FavoritesRequirement.Rn requirement, String key, Marker marker, int marks,
            int expectedStatus, boolean expectedFavorited, int expectedCount) {
        var author = "f-" + key + "-author";
        var authorToken = FavoritesApi.register(author);
        var slug = FavoritesApi.publish(authorToken, "Marked " + key);
        var callerToken = switch (marker) {
            case AUTHOR -> authorToken;
            case OTHER -> FavoritesApi.register("f-" + key + "-caller");
        };
        for (var i = 0; i < marks; i++) {
            var response = FavoritesApi.favorite(slug, Auth.VALID, callerToken);
            assertThat(response.statusCode())
                    .as(requirement + " — " + requirement.statement())
                    .isEqualTo(expectedStatus);
        }
        var json = FavoritesApi.get(slug, Auth.VALID, callerToken).jsonPath();
        assertThat(json.getBoolean("article.favorited")).isEqualTo(expectedFavorited);
        assertThat(json.getInt("article.favoritesCount")).isEqualTo(expectedCount);
        assertThat(json.getString("article.author.username")).isEqualTo(author);
    }

    static Stream<Arguments> markCases() {
        return Stream.of(
                arguments(R1_1, "r1-1", Marker.OTHER, 1, 200, true, 1),
                arguments(R1_2, "r1-2", Marker.OTHER, 2, 200, true, 1),
                arguments(R1_3, "r1-3", Marker.AUTHOR, 1, 200, true, 1));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("rejectCases")
    void rejects(FavoritesRequirement.Rn requirement, String key, Auth auth, Target target,
            int expectedStatus, String expectedErrorField) {
        var slug = "ghost-" + key;
        if (target == Target.EXISTING) {
            var ownerToken = FavoritesApi.register("f-" + key + "-owner");
            slug = FavoritesApi.publish(ownerToken, "Marked " + key);
        }
        var callerToken = auth == Auth.ANONYMOUS ? null : FavoritesApi.register("f-" + key + "-caller");
        var response = FavoritesApi.favorite(slug, auth, callerToken);
        assertThat(response.statusCode())
                .as(requirement + " — " + requirement.statement())
                .isEqualTo(expectedStatus);
        assertThat(response.jsonPath().getMap("errors")).containsKey(expectedErrorField);
    }

    static Stream<Arguments> rejectCases() {
        return Stream.of(
                arguments(R1_4, "r1-4", Auth.VALID, Target.GHOST, 404, "article"),
                arguments(R1_5, "r1-5a", Auth.ANONYMOUS, Target.EXISTING, 401, "token"),
                arguments(R1_5, "r1-5b", Auth.INVALID, Target.EXISTING, 401, "token"));
    }
}
