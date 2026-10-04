package org.acme.comments.boundary;

import static org.acme.comments.CommentsRequirement.Rn.R2_1;
import static org.acme.comments.CommentsRequirement.Rn.R2_2;
import static org.acme.comments.CommentsRequirement.Rn.R2_3;
import static org.acme.comments.CommentsRequirement.Rn.R2_4;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import io.quarkus.test.junit.QuarkusTest;
import java.util.stream.Stream;
import org.acme.comments.CommentsRequirement;
import org.acme.comments.boundary.CommentsApi.Auth;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@QuarkusTest
class CreateCommentTest {

    enum Target {
        EXISTING, GHOST
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("createCases")
    void creates(CommentsRequirement.Rn requirement, String key, boolean self) {
        var ownerToken = CommentsApi.register("c-" + key + "-owner");
        var slug = CommentsApi.publish(ownerToken, "Discussed " + key);
        var commenterToken = self ? ownerToken : CommentsApi.register("c-" + key + "-commenter");
        var response = CommentsApi.create(slug, Auth.VALID, commenterToken,
                "{\"comment\":{\"body\":\"nice read\"}}");
        assertThat(response.statusCode())
                .as(requirement + " — " + requirement.statement())
                .isEqualTo(201);
        var json = response.jsonPath();
        var id = json.getLong("comment.id");
        assertThat(json.getString("comment.body")).isEqualTo("nice read");
        assertThat(json.getString("comment.createdAt")).isNotBlank();
        assertThat(json.getString("comment.updatedAt")).isNotBlank();
        assertThat(json.getBoolean("comment.author.following")).isFalse();
        var listed = CommentsApi.list(slug, Auth.ANONYMOUS, null).jsonPath();
        assertThat(listed.getList("comments.id", Long.class)).containsExactly(id);
    }

    static Stream<Arguments> createCases() {
        return Stream.of(
                arguments(R2_1, "r2-1a", false),
                arguments(R2_1, "r2-1b", true));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("rejectCases")
    void rejects(CommentsRequirement.Rn requirement, String key, Auth auth, Target target,
            String payload, int expectedStatus, String expectedErrorField) {
        var slug = "ghost-" + key;
        if (target == Target.EXISTING) {
            var ownerToken = CommentsApi.register("c-" + key + "-owner");
            slug = CommentsApi.publish(ownerToken, "Discussed " + key);
        }
        var callerToken = auth == Auth.VALID ? CommentsApi.register("c-" + key + "-caller") : null;
        var response = CommentsApi.create(slug, auth, callerToken, payload);
        assertThat(response.statusCode())
                .as(requirement + " — " + requirement.statement())
                .isEqualTo(expectedStatus);
        assertThat(response.jsonPath().getMap("errors")).containsKey(expectedErrorField);
    }

    static Stream<Arguments> rejectCases() {
        var body = "{\"comment\":{\"body\":\"hi\"}}";
        return Stream.of(
                arguments(R2_2, "r2-2a", Auth.VALID, Target.EXISTING, "{\"comment\":{}}", 422, "body"),
                arguments(R2_2, "r2-2b", Auth.VALID, Target.EXISTING, "{\"comment\":{\"body\":\"\"}}",
                        422, "body"),
                arguments(R2_2, "r2-2c", Auth.VALID, Target.EXISTING, "{\"comment\":{\"body\":\"  \"}}",
                        422, "body"),
                arguments(R2_3, "r2-3", Auth.VALID, Target.GHOST, body, 404, "article"),
                arguments(R2_4, "r2-4a", Auth.ANONYMOUS, Target.EXISTING, body, 401, "token"),
                arguments(R2_4, "r2-4b", Auth.INVALID, Target.EXISTING, body, 401, "token"));
    }
}
