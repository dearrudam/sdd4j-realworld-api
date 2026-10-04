package org.acme.user.boundary;

import static org.acme.user.UserRequirement.Rn.R3_1;
import static org.acme.user.UserRequirement.Rn.R3_2;
import static org.acme.user.UserRequirement.Rn.R4_1;
import static org.acme.user.UserRequirement.Rn.R4_2;
import static org.acme.user.UserRequirement.Rn.R4_5;
import static org.acme.user.UserRequirement.Rn.R4_6;
import static org.acme.user.UserRequirement.Rn.R4_7;
import static org.acme.user.UserRequirement.Rn.R5_1;

import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.acme.user.UserRequirement;
import org.acme.user.control.ProfileUpdate;
import org.acme.user.control.SessionTokens;
import org.acme.user.control.Users;

@Path("/user")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class CurrentUserResource {

    @Inject
    Users users;

    @Inject
    SessionTokens tokens;

    @GET
    @PermitAll
    @UserRequirement({ R3_1, R3_2 })
    public UserResponse getCurrentUser(@HeaderParam("Authorization") String authorization) {
        var user = users.find(tokens.verify(authorization));
        return UserResponse.of(user, tokens.issue(user));
    }

    @PUT
    @PermitAll
    @UserRequirement({ R4_1, R4_2, R4_5, R4_6, R4_7, R5_1 })
    public UserResponse updateUser(@HeaderParam("Authorization") String authorization,
            @Valid UpdateRequest request) {
        var update = request.user();
        var user = users.update(tokens.verify(authorization), new ProfileUpdate(update.email(), update.username(),
                update.password(), update.bio(), update.image()));
        return UserResponse.of(user, tokens.issue(user));
    }
}
