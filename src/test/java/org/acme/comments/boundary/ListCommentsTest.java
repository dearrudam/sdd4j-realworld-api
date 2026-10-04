package org.acme.comments.boundary;

import static org.acme.comments.CommentsRequirement.Rn.R1_1;
import static org.acme.comments.CommentsRequirement.Rn.R1_2;
import static org.acme.comments.CommentsRequirement.Rn.R1_3;
import static org.acme.comments.CommentsRequirement.Rn.R1_4;
import static org.acme.comments.CommentsRequirement.Rn.R1_5;
import static org.acme.comments.CommentsRequirement.Rn.R1_6;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import io.quarkus.test.junit.QuarkusTest;
import java.util.ArrayList;
import java.util.stream.Stream;
import org.acme.comments.CommentsRequirement;
import org.acme.comments.boundary.CommentsApi.Auth;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@QuarkusTest
class ListCommentsTest {

    enum Target {
        EXISTING, GHOST
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("listingCases")
    void listing(CommentsRequirement.Rn requirement, String key, int remarks) {
        var authorToken = CommentsApi.register("c-" + key + "-author");
        var slug = CommentsApi.publish(authorToken, "Discussed " + key);
        var expected = new ArrayList<String>();
        for (var i = 1; i <= remarks; i++) {
            expected.add("remark-" + i);
            CommentsApi.comment(authorToken, slug, "remark-" + i);
        }
        var response = CommentsApi.list(slug, Auth.ANONYMOUS, null);
        assertThat(response.statusCode())
                .as(requirement + " — " + requirement.statement())
                .isEqualTo(200);
        var json = response.jsonPath();
        assertThat(json.getList("comments.body", String.class)).containsExactlyElementsOf(expected);
        assertThat(json.getList("comments.id", Long.class)).isSorted();
    }

    static Stream<Arguments> listingCases() {
        return Stream.of(
                arguments(R1_1, "r1-1", 3),
                arguments(R1_2, "r1-2", 0));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("followingCases")
    void following(CommentsRequirement.Rn requirement, String key, Auth auth, boolean follow,
            boolean expectedFollowing) {
        var author = "c-" + key + "-author";
        var authorToken = CommentsApi.register(author);
        var slug = CommentsApi.publish(authorToken, "Discussed " + key);
        CommentsApi.comment(authorToken, slug, "remark");
        String callerToken = switch (auth) {
            case ANONYMOUS, INVALID -> null;
            case VALID -> {
                var token = CommentsApi.register("c-" + key + "-caller");
                if (follow) {
                    CommentsApi.follow(token, author);
                }
                yield token;
            }
        };
        var response = CommentsApi.list(slug, auth, callerToken);
        assertThat(response.statusCode())
                .as(requirement + " — " + requirement.statement())
                .isEqualTo(200);
        var profile = response.jsonPath().getMap("comments[0].author");
        assertThat(profile.get("username")).isEqualTo(author);
        assertThat(profile.get("following")).isEqualTo(expectedFollowing);
    }

    static Stream<Arguments> followingCases() {
        return Stream.of(
                arguments(R1_3, "r1-3a", Auth.VALID, true, true),
                arguments(R1_3, "r1-3b", Auth.VALID, false, false),
                arguments(R1_4, "r1-4", Auth.ANONYMOUS, false, false));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("rejectCases")
    void rejects(CommentsRequirement.Rn requirement, String key, Auth auth, Target target,
            int expectedStatus, String expectedErrorField) {
        var slug = "ghost-" + key;
        if (target == Target.EXISTING) {
            var ownerToken = CommentsApi.register("c-" + key + "-owner");
            slug = CommentsApi.publish(ownerToken, "Discussed " + key);
        }
        var callerToken = auth == Auth.VALID ? CommentsApi.register("c-" + key + "-caller") : null;
        var response = CommentsApi.list(slug, auth, callerToken);
        assertThat(response.statusCode())
                .as(requirement + " — " + requirement.statement())
                .isEqualTo(expectedStatus);
        assertThat(response.jsonPath().getMap("errors")).containsKey(expectedErrorField);
    }

    static Stream<Arguments> rejectCases() {
        return Stream.of(
                arguments(R1_5, "r1-5", Auth.INVALID, Target.EXISTING, 401, "token"),
                arguments(R1_6, "r1-6", Auth.ANONYMOUS, Target.GHOST, 404, "article"));
    }
}
