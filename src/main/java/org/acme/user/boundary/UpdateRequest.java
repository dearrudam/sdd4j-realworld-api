package org.acme.user.boundary;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public record UpdateRequest(@NotNull @Valid UpdateUser user) {

    public record UpdateUser(@Email String email, String password, String username, String bio, String image) {
    }
}
