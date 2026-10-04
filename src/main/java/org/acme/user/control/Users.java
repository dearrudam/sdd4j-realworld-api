package org.acme.user.control;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.user.entity.User;
import org.hibernate.Session;

@ApplicationScoped
public class Users {

    @Inject
    Session session;

    @Transactional
    public User register(String username, String email, String password) {
        rejectIfTaken(username, "username", 409);
        rejectIfTaken(email, "email", 409);
        var user = User.withCredentials(username, email, password);
        session.persist(user);
        return user;
    }

    @Transactional
    public User authenticate(String email, String password) {
        var user = findBy("email", email);
        if (user == null || !user.matchesPassword(password)) {
            throw Rejection.credentials();
        }
        return user;
    }

    @Transactional
    public User find(long id) {
        var user = session.find(User.class, id);
        if (user == null) {
            throw Rejection.tokenInvalid();
        }
        return user;
    }

    @Transactional
    public User update(long id, ProfileUpdate update) {
        var user = find(id);
        if (update.isEmpty()) {
            throw Rejection.invalid("user", "at least one field must be provided");
        }
        rejectIfTakenByOther(id, update.username(), "username");
        rejectIfTakenByOther(id, update.email(), "email");
        if (update.username() != null) {
            user.username = update.username();
        }
        if (update.email() != null) {
            user.email = update.email();
        }
        if (update.password() != null) {
            user.changePassword(update.password());
        }
        if (update.bio() != null) {
            user.bio = update.bio();
        }
        if (update.image() != null) {
            user.image = update.image();
        }
        return user;
    }

    void rejectIfTaken(String value, String field, int status) {
        if (findBy(field, value) != null) {
            throw Rejection.taken(status, field);
        }
    }

    void rejectIfTakenByOther(long id, String value, String field) {
        var other = value == null ? null : findBy(field, value);
        if (other != null && other.id != id) {
            throw Rejection.taken(422, field);
        }
    }

    User findBy(String field, String value) {
        return session.createSelectionQuery("from User where %s = :value".formatted(field), User.class)
                .setParameter("value", value)
                .getSingleResultOrNull();
    }
}
