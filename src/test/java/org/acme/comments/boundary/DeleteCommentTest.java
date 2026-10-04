package org.acme.comments.boundary;

import static org.acme.comments.CommentsRequirement.Rn.R3_1;
import static org.acme.comments.CommentsRequirement.Rn.R3_2;
import static org.acme.comments.CommentsRequirement.Rn.R3_3;
import static org.acme.comments.CommentsRequirement.Rn.R3_4;
import static org.acme.comments.CommentsRequirement.Rn.R3_5;
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
class DeleteCommentTest {

    enum Deleter {
        COMMENT_AUTHOR, ARTICLE_AUTHOR
    }

    enum Target {
        EXISTING, GHOST
    }

    enum CommentId {
        EXISTING, UNKNOWN, FOREIGN
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("deleteCases")
    void deletes(CommentsRequirement.Rn requirement, String key, Deleter deleter) {
        var ownerToken = CommentsApi.register("c-" + key + "-owner");
        var slug = CommentsApi.publish(ownerToken, "Discussed " + key);
        var commenterToken = CommentsApi.register("c-" + key + "-commenter");
        var id = CommentsApi.comment(commenterToken, slug, "remark");
        var callerToken = switch (deleter) {
            case COMMENT_AUTHOR -> commenterToken;
            case ARTICLE_AUTHOR -> ownerToken;
        };
        var response = CommentsApi.delete(slug, id, Auth.VALID, callerToken);
        assertThat(response.statusCode())
                .as(requirement + " — " + requirement.statement())
                .isEqualTo(204);
        assertThat(response.getBody().asString()).isEmpty();
        var json = CommentsApi.list(slug, Auth.ANONYMOUS, null).jsonPath();
        assertThat(json.getList("comments.id", Long.class)).doesNotContain(id);
    }

    static Stream<Arguments> deleteCases() {
        return Stream.of(
                arguments(R3_1, "r3-1a", Deleter.COMMENT_AUTHOR),
                arguments(R3_1, "r3-1b", Deleter.ARTICLE_AUTHOR));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("rejectCases")
    void rejects(CommentsRequirement.Rn requirement, String key, Auth auth, Target target,
            CommentId idKind, int expectedStatus, String expectedErrorField) {
        var commenterToken = CommentsApi.register("c-" + key + "-commenter");
        var slug = "ghost-" + key;
        if (target == Target.EXISTING) {
            var ownerToken = CommentsApi.register("c-" + key + "-owner");
            slug = CommentsApi.publish(ownerToken, "Discussed " + key);
        }
        var id = switch (idKind) {
            case EXISTING -> CommentsApi.comment(commenterToken, slug, "remark");
            case UNKNOWN -> 99999L;
            case FOREIGN -> {
                var other = CommentsApi.publish(commenterToken, "Other " + key);
                yield CommentsApi.comment(commenterToken, other, "remark");
            }
        };
        var response = CommentsApi.delete(slug, id, auth, commenterToken);
        assertThat(response.statusCode())
                .as(requirement + " — " + requirement.statement())
                .isEqualTo(expectedStatus);
        assertThat(response.jsonPath().getMap("errors")).containsKey(expectedErrorField);
    }

    static Stream<Arguments> rejectCases() {
        return Stream.of(
                arguments(R3_2, "r3-2a", Auth.ANONYMOUS, Target.EXISTING, CommentId.EXISTING, 401,
                        "token"),
                arguments(R3_2, "r3-2b", Auth.INVALID, Target.EXISTING, CommentId.EXISTING, 401,
                        "token"),
                arguments(R3_3, "r3-3", Auth.VALID, Target.GHOST, CommentId.UNKNOWN, 404, "article"),
                arguments(R3_4, "r3-4a", Auth.VALID, Target.EXISTING, CommentId.UNKNOWN, 404,
                        "comment"),
                arguments(R3_4, "r3-4b", Auth.VALID, Target.EXISTING, CommentId.FOREIGN, 404,
                        "comment"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("forbiddenCases")
    void forbidden(CommentsRequirement.Rn requirement, String key) {
        var ownerToken = CommentsApi.register("c-" + key + "-owner");
        var slug = CommentsApi.publish(ownerToken, "Discussed " + key);
        var commenterToken = CommentsApi.register("c-" + key + "-commenter");
        var id = CommentsApi.comment(commenterToken, slug, "remark");
        var outsiderToken = CommentsApi.register("c-" + key + "-outsider");
        var response = CommentsApi.delete(slug, id, Auth.VALID, outsiderToken);
        assertThat(response.statusCode())
                .as(requirement + " — " + requirement.statement())
                .isEqualTo(403);
        assertThat(response.jsonPath().getMap("errors")).containsKey("comment");
        var json = CommentsApi.list(slug, Auth.ANONYMOUS, null).jsonPath();
        assertThat(json.getList("comments.id", Long.class)).contains(id);
    }

    static Stream<Arguments> forbiddenCases() {
        return Stream.of(arguments(R3_5, "r3-5"));
    }
}
