package org.acme.user.entity;

import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.acme.user.UserRequirement;

@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = "username"),
        @UniqueConstraint(columnNames = "email") })
public class User {

    @Id
    @GeneratedValue
    public Long id;

    @Column(nullable = false)
    public String username;

    @Column(nullable = false)
    public String email;

    @Column(nullable = false)
    String passwordHash;

    public String bio;
    public String image;

    protected User() {
    }

    User(String username, String email, String passwordHash) {
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    @UserRequirement(UserRequirement.Rn.R5_1)
    public static User withCredentials(String username, String email, String password) {
        return new User(username, email, BcryptUtil.bcryptHash(password));
    }

    public boolean matchesPassword(String password) {
        return BcryptUtil.matches(password, this.passwordHash);
    }

    @UserRequirement(UserRequirement.Rn.R5_1)
    public void changePassword(String password) {
        this.passwordHash = BcryptUtil.bcryptHash(password);
    }
}
