package org.acme.user.control;

import java.util.List;
import java.util.Map;

public class Rejection extends RuntimeException {

    public int status;
    public Map<String, List<String>> errors;

    Rejection(int status, String field, String message) {
        super("%s: %s".formatted(field, message));
        this.status = status;
        this.errors = Map.of(field, List.of(message));
    }

    public static Rejection invalid(String field, String message) {
        return new Rejection(422, field, message);
    }

    public static Rejection taken(int status, String field) {
        return new Rejection(status, field, "has already been taken");
    }

    public static Rejection credentials() {
        return new Rejection(401, "credentials", "is invalid");
    }

    public static Rejection tokenMissing() {
        return new Rejection(401, "token", "is missing");
    }

    public static Rejection tokenInvalid() {
        return new Rejection(401, "token", "is invalid");
    }
}
