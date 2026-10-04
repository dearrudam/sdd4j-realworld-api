package org.acme.profile.control;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;
import org.acme.profile.entity.Follow;
import org.acme.user.control.Rejection;
import org.acme.user.control.Users;
import org.acme.user.entity.User;
import org.hibernate.Session;

@ApplicationScoped
public class Profiles {

    @Inject
    Session session;

    @Inject
    Users users;

    @Transactional
    public Profile get(User caller, String username) {
        var target = target(username);
        return new Profile(target, caller != null && follows(caller, target));
    }

    @Transactional
    public Profile follow(User caller, String username) {
        var target = target(username);
        rejectSelfTarget(caller, target);
        if (!follows(caller, target)) {
            session.persist(Follow.between(caller, target));
        }
        return new Profile(target, true);
    }

    @Transactional
    public Profile unfollow(User caller, String username) {
        var target = target(username);
        rejectSelfTarget(caller, target);
        var follow = findFollow(caller, target);
        if (follow != null) {
            session.remove(follow);
        }
        return new Profile(target, false);
    }

    User target(String username) {
        var target = users.findByUsername(username);
        if (target == null) {
            throw Rejection.notFound("profile");
        }
        return target;
    }

    void rejectSelfTarget(User caller, User target) {
        if (caller.id.equals(target.id)) {
            throw Rejection.invalid("profile", "cannot be the caller");
        }
    }

    @Transactional
    public List<User> followedBy(User caller) {
        return session
                .createSelectionQuery("select f.followed from Follow f where f.follower = :caller", User.class)
                .setParameter("caller", caller)
                .getResultList();
    }

    boolean follows(User caller, User target) {
        return findFollow(caller, target) != null;
    }

    Follow findFollow(User caller, User target) {
        return session
                .createSelectionQuery("from Follow where follower = :caller and followed = :target", Follow.class)
                .setParameter("caller", caller)
                .setParameter("target", target)
                .getSingleResultOrNull();
    }
}
