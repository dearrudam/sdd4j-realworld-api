package org.acme.user.boundary;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LoginRequest(@NotNull @Valid LoginUser user) {

    public record LoginUser(@NotBlank(message = "can't be blank") @Email String email,
            @NotBlank(message = "can't be blank") String password) {
    }
}
