package org.acme.user.boundary;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegistrationRequest(@NotNull @Valid NewUser user) {

    public record NewUser(@NotBlank(message = "can't be blank") String username,
            @NotBlank(message = "can't be blank") @Email String email,
            @NotBlank(message = "can't be blank") String password) {
    }
}
