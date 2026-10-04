package org.acme.tags.boundary;

import static org.acme.tags.TagsRequirement.Rn.R1_1;
import static org.acme.tags.TagsRequirement.Rn.R1_2;
import static org.acme.tags.TagsRequirement.Rn.R1_4;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import io.quarkus.test.junit.QuarkusTest;
import java.util.List;
import java.util.stream.Stream;
import org.acme.tags.TagsRequirement;
import org.acme.tags.boundary.TagsApi.Auth;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@QuarkusTest
class ListTagsTest {

    @ParameterizedTest(name = "{0}")
    @MethodSource("distinctCases")
    void distinct(TagsRequirement.Rn requirement, String key) {
        var ownerToken = TagsApi.register("t-" + key + "-owner");
        TagsApi.publish(ownerToken, "Tagged " + key + " One",
                List.of("zz-" + key, "aa-" + key, "mm-" + key));
        TagsApi.publish(ownerToken, "Tagged " + key + " Two", List.of("aa-" + key, "nn-" + key));
        var response = TagsApi.list(Auth.ANONYMOUS, null);
        assertThat(response.statusCode())
                .as(requirement + " — " + requirement.statement())
                .isEqualTo(200);
        var tags = response.jsonPath().getList("tags", String.class);
        assertThat(tags).doesNotHaveDuplicates();
        assertThat(tags).contains("zz-" + key, "aa-" + key, "mm-" + key, "nn-" + key);
    }

    static Stream<Arguments> distinctCases() {
        return Stream.of(arguments(R1_1, "r1-1"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("existingOnlyCases")
    void existingOnly(TagsRequirement.Rn requirement, String key) {
        var ownerToken = TagsApi.register("t-" + key + "-owner");
        TagsApi.publish(ownerToken, "Tagged " + key + " One", List.of("aa-" + key));
        var doomed = TagsApi.publish(ownerToken, "Tagged " + key + " Two", List.of("zz-" + key));
        TagsApi.delete(ownerToken, doomed);
        var response = TagsApi.list(Auth.ANONYMOUS, null);
        assertThat(response.statusCode())
                .as(requirement + " — " + requirement.statement())
                .isEqualTo(200);
        var tags = response.jsonPath().getList("tags", String.class);
        assertThat(tags).contains("aa-" + key);
        assertThat(tags).doesNotContain("zz-" + key);
    }

    static Stream<Arguments> existingOnlyCases() {
        return Stream.of(arguments(R1_1, "r1-1b"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("orderCases")
    void alphabetical(TagsRequirement.Rn requirement) {
        var response = TagsApi.list(Auth.ANONYMOUS, null);
        assertThat(response.statusCode())
                .as(requirement + " — " + requirement.statement())
                .isEqualTo(200);
        assertThat(response.jsonPath().getList("tags", String.class)).isSorted();
    }

    static Stream<Arguments> orderCases() {
        return Stream.of(arguments(R1_2, "r1-2"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("authCases")
    void noTokenRequired(TagsRequirement.Rn requirement, String key, Auth auth) {
        var response = TagsApi.list(auth, null);
        assertThat(response.statusCode())
                .as(requirement + " — " + requirement.statement())
                .isEqualTo(200);
        assertThat(response.jsonPath().getList("tags")).isNotNull();
    }

    static Stream<Arguments> authCases() {
        return Stream.of(
                arguments(R1_4, "r1-4a", Auth.ANONYMOUS),
                arguments(R1_4, "r1-4b", Auth.INVALID));
    }
}
