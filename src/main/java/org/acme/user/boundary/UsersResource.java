package org.acme.user.boundary;

import static org.acme.user.UserRequirement.Rn.R1_1;
import static org.acme.user.UserRequirement.Rn.R1_2;
import static org.acme.user.UserRequirement.Rn.R1_3;
import static org.acme.user.UserRequirement.Rn.R1_6;
import static org.acme.user.UserRequirement.Rn.R1_7;
import static org.acme.user.UserRequirement.Rn.R2_1;
import static org.acme.user.UserRequirement.Rn.R2_2;
import static org.acme.user.UserRequirement.Rn.R2_3;
import static org.acme.user.UserRequirement.Rn.R5_1;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.user.UserRequirement;
import org.acme.user.control.SessionTokens;
import org.acme.user.control.Users;

@Path("/users")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class UsersResource {

    @Inject
    Users users;

    @Inject
    SessionTokens tokens;

    @POST
    @UserRequirement({ R1_1, R1_2, R1_3, R1_6, R1_7, R5_1 })
    public Response registerUser(@Valid RegistrationRequest request) {
        var newUser = request.user();
        var user = users.register(newUser.username(), newUser.email(), newUser.password());
        return Response.status(Response.Status.CREATED)
                .entity(UserResponse.of(user, tokens.issue(user)))
                .build();
    }

    @POST
    @Path("/login")
    @UserRequirement({ R2_1, R2_2, R2_3 })
    public UserResponse authenticateUser(@Valid LoginRequest request) {
        var credentials = request.user();
        var user = users.authenticate(credentials.email(), credentials.password());
        return UserResponse.of(user, tokens.issue(user));
    }
}
