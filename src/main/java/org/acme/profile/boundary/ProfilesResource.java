package org.acme.profile.boundary;

import static org.acme.profile.ProfileRequirement.Rn.R1_1;
import static org.acme.profile.ProfileRequirement.Rn.R1_2;
import static org.acme.profile.ProfileRequirement.Rn.R1_3;
import static org.acme.profile.ProfileRequirement.Rn.R1_4;
import static org.acme.profile.ProfileRequirement.Rn.R1_5;
import static org.acme.profile.ProfileRequirement.Rn.R2_1;
import static org.acme.profile.ProfileRequirement.Rn.R2_2;
import static org.acme.profile.ProfileRequirement.Rn.R2_3;
import static org.acme.profile.ProfileRequirement.Rn.R2_4;
import static org.acme.profile.ProfileRequirement.Rn.R2_5;
import static org.acme.profile.ProfileRequirement.Rn.R3_1;
import static org.acme.profile.ProfileRequirement.Rn.R3_2;
import static org.acme.profile.ProfileRequirement.Rn.R3_3;
import static org.acme.profile.ProfileRequirement.Rn.R3_4;
import static org.acme.profile.ProfileRequirement.Rn.R3_5;

import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.acme.profile.ProfileRequirement;
import org.acme.profile.control.Profiles;
import org.acme.user.control.SessionTokens;
import org.acme.user.control.Users;
import org.acme.user.entity.User;

@Path("/profiles")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ProfilesResource {

    @Inject
    Users users;

    @Inject
    SessionTokens tokens;

    @Inject
    Profiles profiles;

    @GET
    @Path("/{username}")
    @PermitAll
    @ProfileRequirement({ R1_1, R1_2, R1_3, R1_4, R1_5 })
    public ProfileResponse getProfile(@PathParam("username") String username,
            @HeaderParam("Authorization") String authorization) {
        var caller = authorization == null ? null : caller(authorization);
        return ProfileResponse.of(profiles.get(caller, username));
    }

    @POST
    @Path("/{username}/follow")
    @PermitAll
    @ProfileRequirement({ R2_1, R2_2, R2_3, R2_4, R2_5 })
    public ProfileResponse followUser(@PathParam("username") String username,
            @HeaderParam("Authorization") String authorization) {
        return ProfileResponse.of(profiles.follow(caller(authorization), username));
    }

    @DELETE
    @Path("/{username}/follow")
    @PermitAll
    @ProfileRequirement({ R3_1, R3_2, R3_3, R3_4, R3_5 })
    public ProfileResponse unfollowUser(@PathParam("username") String username,
            @HeaderParam("Authorization") String authorization) {
        return ProfileResponse.of(profiles.unfollow(caller(authorization), username));
    }

    User caller(String authorization) {
        return users.find(tokens.verify(authorization));
    }
}
