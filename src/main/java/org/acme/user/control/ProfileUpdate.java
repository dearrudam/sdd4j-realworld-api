package org.acme.user.control;

public record ProfileUpdate(String email, String username, String password, FieldUpdate<String> bio,
        FieldUpdate<String> image) {

    public boolean isEmpty() {
        return email == null && username == null && password == null && !bio.present() && !image.present();
    }
}
