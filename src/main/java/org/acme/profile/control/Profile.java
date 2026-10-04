package org.acme.profile.control;

import org.acme.user.entity.User;

public record Profile(User user, boolean following) {
}
