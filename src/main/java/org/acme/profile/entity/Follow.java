package org.acme.profile.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.acme.user.entity.User;

@Entity
@Table(name = "follows", uniqueConstraints = @UniqueConstraint(columnNames = { "follower_id", "followed_id" }))
public class Follow {

    @Id
    @GeneratedValue
    public Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "follower_id")
    public User follower;

    @ManyToOne(optional = false)
    @JoinColumn(name = "followed_id")
    public User followed;

    protected Follow() {
    }

    Follow(User follower, User followed) {
        this.follower = follower;
        this.followed = followed;
    }

    public static Follow between(User follower, User followed) {
        return new Follow(follower, followed);
    }
}
