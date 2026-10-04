package org.acme.profile.boundary;

import static org.acme.profile.ProfileRequirement.Rn.R3_1;
import static org.acme.profile.ProfileRequirement.Rn.R3_2;
import static org.acme.profile.ProfileRequirement.Rn.R3_3;
import static org.acme.profile.ProfileRequirement.Rn.R3_4;
import static org.acme.profile.ProfileRequirement.Rn.R3_5;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import io.quarkus.test.junit.QuarkusTest;
import java.util.stream.Stream;
import org.acme.profile.ProfileRequirement;
import org.acme.profile.boundary.ProfileApi.Auth;
import org.acme.profile.boundary.ProfileApi.Target;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@QuarkusTest
class UnfollowUserTest {

    @ParameterizedTest(name = "{0}")
    @MethodSource("cases")
    void unfollowUser(ProfileRequirement.Rn requirement, String key, Auth auth, boolean alreadyFollowing,
            Target target, int expectedStatus, boolean expectedFollowing, String expectedErrorField) {
        var callerName = "p-" + key + "-caller";
        var callerToken = auth == Auth.ANONYMOUS ? null : ProfileApi.register(callerName);
        var targetName = switch (target) {
            case SELF -> callerName;
            case GHOST -> "p-" + key + "-ghost";
            case OTHER -> "p-" + key + "-target";
        };
        if (target == Target.OTHER) {
            ProfileApi.register(targetName);
        }
        if (alreadyFollowing) {
            ProfileApi.follow(Auth.VALID, callerToken, targetName).then().statusCode(200);
        }
        var response = ProfileApi.unfollow(auth, callerToken, targetName);
        assertThat(response.statusCode())
                .as(requirement + " — " + requirement.statement())
                .isEqualTo(expectedStatus);
        if (expectedErrorField != null) {
            assertThat(response.jsonPath().getMap("errors")).containsKey(expectedErrorField);
        } else {
            assertThat(response.jsonPath().getString("profile.username")).isEqualTo(targetName);
            assertThat(response.jsonPath().getBoolean("profile.following")).isEqualTo(expectedFollowing);
            assertThat(ProfileApi.getProfile(Auth.VALID, callerToken, targetName)
                    .jsonPath().getBoolean("profile.following")).isEqualTo(expectedFollowing);
        }
    }

    static Stream<Arguments> cases() {
        return Stream.of(
                arguments(R3_1, "r3-1", Auth.VALID, true, Target.OTHER, 200, false, null),
                arguments(R3_2, "r3-2", Auth.VALID, false, Target.OTHER, 200, false, null),
                arguments(R3_3, "r3-3a", Auth.ANONYMOUS, false, Target.OTHER, 401, false, "token"),
                arguments(R3_3, "r3-3b", Auth.INVALID, false, Target.OTHER, 401, false, "token"),
                arguments(R3_4, "r3-4", Auth.VALID, false, Target.GHOST, 404, false, "profile"),
                arguments(R3_5, "r3-5", Auth.VALID, false, Target.SELF, 422, false, "profile"));
    }
}
