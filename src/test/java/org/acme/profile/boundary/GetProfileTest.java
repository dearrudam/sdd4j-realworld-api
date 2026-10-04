package org.acme.profile.boundary;

import static org.acme.profile.ProfileRequirement.Rn.R1_1;
import static org.acme.profile.ProfileRequirement.Rn.R1_2;
import static org.acme.profile.ProfileRequirement.Rn.R1_3;
import static org.acme.profile.ProfileRequirement.Rn.R1_4;
import static org.acme.profile.ProfileRequirement.Rn.R1_5;
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
class GetProfileTest {

    @ParameterizedTest(name = "{0}")
    @MethodSource("cases")
    void getProfile(ProfileRequirement.Rn requirement, String key, Auth auth, boolean alreadyFollowing,
            Target target, int expectedStatus, boolean expectedFollowing, String expectedErrorField) {
        var callerToken = auth == Auth.ANONYMOUS ? null : ProfileApi.register("p-" + key + "-caller");
        var targetName = target == Target.GHOST ? "p-" + key + "-ghost" : "p-" + key + "-target";
        if (target != Target.GHOST) {
            var targetToken = ProfileApi.register(targetName);
            ProfileApi.describe(targetToken, "bio-" + key, "https://img.test/" + key + ".png");
            if (target == Target.SELF) {
                callerToken = targetToken;
            }
        }
        if (alreadyFollowing) {
            ProfileApi.follow(Auth.VALID, callerToken, targetName).then().statusCode(200);
        }
        var response = ProfileApi.getProfile(auth, callerToken, targetName);
        assertThat(response.statusCode())
                .as(requirement + " — " + requirement.statement())
                .isEqualTo(expectedStatus);
        if (expectedErrorField != null) {
            assertThat(response.jsonPath().getMap("errors")).containsKey(expectedErrorField);
        } else {
            assertThat(response.jsonPath().getString("profile.username")).isEqualTo(targetName);
            assertThat(response.jsonPath().getString("profile.bio")).isEqualTo("bio-" + key);
            assertThat(response.jsonPath().getString("profile.image"))
                    .isEqualTo("https://img.test/" + key + ".png");
            assertThat(response.jsonPath().getBoolean("profile.following")).isEqualTo(expectedFollowing);
        }
    }

    static Stream<Arguments> cases() {
        return Stream.of(
                arguments(R1_1, "r1-1", Auth.ANONYMOUS, false, Target.OTHER, 200, false, null),
                arguments(R1_2, "r1-2", Auth.VALID, true, Target.OTHER, 200, true, null),
                arguments(R1_3, "r1-3a", Auth.ANONYMOUS, false, Target.OTHER, 200, false, null),
                arguments(R1_3, "r1-3b", Auth.VALID, false, Target.OTHER, 200, false, null),
                arguments(R1_3, "r1-3c", Auth.VALID, false, Target.SELF, 200, false, null),
                arguments(R1_4, "r1-4", Auth.INVALID, false, Target.OTHER, 401, false, "token"),
                arguments(R1_5, "r1-5", Auth.ANONYMOUS, false, Target.GHOST, 404, false, "profile"));
    }
}
