package org.acme.profile.boundary;

import jakarta.json.bind.annotation.JsonbProperty;
import org.acme.profile.control.Profile;

public record ProfileResponse(ProfileView profile) {

    public static ProfileResponse of(Profile profile) {
        var user = profile.user();
        return new ProfileResponse(new ProfileView(user.username, user.bio, user.image, profile.following()));
    }

    public record ProfileView(String username, @JsonbProperty(nillable = true) String bio,
            @JsonbProperty(nillable = true) String image, boolean following) {
    }
}
