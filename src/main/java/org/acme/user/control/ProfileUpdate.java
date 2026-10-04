package org.acme.user.control;

public record ProfileUpdate(String email, String username, String password, String bio, String image) {

    public boolean isEmpty() {
        return email == null && username == null && password == null && bio == null && image == null;
    }
}
