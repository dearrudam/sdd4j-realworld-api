package org.acme.user.boundary;

import static org.acme.user.UserRequirement.Rn.R3_1;
import static org.acme.user.UserRequirement.Rn.R3_2;
import static org.acme.user.UserRequirement.Rn.R4_1;
import static org.acme.user.UserRequirement.Rn.R4_10;
import static org.acme.user.UserRequirement.Rn.R4_11;
import static org.acme.user.UserRequirement.Rn.R4_2;
import static org.acme.user.UserRequirement.Rn.R4_5;
import static org.acme.user.UserRequirement.Rn.R4_6;
import static org.acme.user.UserRequirement.Rn.R4_7;
import static org.acme.user.UserRequirement.Rn.R4_8;
import static org.acme.user.UserRequirement.Rn.R4_9;
import static org.acme.user.UserRequirement.Rn.R5_1;

import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.acme.user.UserRequirement;
import org.acme.user.control.FieldUpdate;
import org.acme.user.control.ProfileUpdate;
import org.acme.user.control.Rejection;
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
    @UserRequirement({ R4_1, R4_2, R4_5, R4_6, R4_7, R4_8, R4_9, R4_10, R4_11, R5_1 })
    public UserResponse updateUser(@HeaderParam("Authorization") String authorization, JsonObject body) {
        var update = updateFields(body);
        var user = users.update(tokens.verify(authorization), update);
        return UserResponse.of(user, tokens.issue(user));
    }

    static ProfileUpdate updateFields(JsonObject body) {
        if (body == null || !(body.get("user") instanceof JsonObject user)) {
            throw Rejection.invalid("user", "is required");
        }
        var email = required(user, "email");
        if (email != null && !email.contains("@")) {
            throw Rejection.invalid("email", "must be a well-formed email address");
        }
        var password = required(user, "password");
        if (password != null && password.length() < 8) {
            throw Rejection.invalid("password", "must be at least 8 characters");
        }
        return new ProfileUpdate(email, required(user, "username"), password, nullable(user, "bio"),
                nullable(user, "image"));
    }

    static String required(JsonObject user, String field) {
        if (!user.containsKey(field)) {
            return null;
        }
        if (user.isNull(field)) {
            throw Rejection.invalid(field, "can't be blank");
        }
        var value = string(user, field);
        if (value.isBlank()) {
            throw Rejection.invalid(field, "can't be blank");
        }
        return value;
    }

    static FieldUpdate<String> nullable(JsonObject user, String field) {
        if (!user.containsKey(field)) {
            return FieldUpdate.unset();
        }
        var value = user.isNull(field) ? null : string(user, field);
        return FieldUpdate.set(value == null || value.isBlank() ? null : value);
    }

    static String string(JsonObject user, String field) {
        if (!(user.get(field) instanceof JsonString string)) {
            throw Rejection.invalid(field, "must be a string");
        }
        return string.getString();
    }
}
