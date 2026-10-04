package org.acme.user.boundary;

import org.acme.user.entity.User;

public record UserResponse(UserView user) {

    public static UserResponse of(User user, String token) {
        return new UserResponse(new UserView(user.email, token, user.username, user.bio, user.image));
    }

    public record UserView(String email, String token, String username, String bio, String image) {
    }
}
