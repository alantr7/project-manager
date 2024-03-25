package com.github.alantr7.prepo.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.hibernate.orm.panache.PanacheQuery;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;

import org.hibernate.annotations.ColumnDefault;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity(name = "users")
public class UserEntity extends PanacheEntityBase {

    @Id
    String id;

    @OneToOne(mappedBy = "user")
    UserAvatarEntity avatar;

    String name;

    String email;

    @ColumnDefault("0")
    Long lastSeenNotification;
    
    public static final String DEFAULT_AVATAR = "/default-avatar.png";

    public UserEntity(String id) {
        this.id = id;
        this.name = id;
    }

    UserEntity() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id.toLowerCase();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatar() {
        return avatar != null ? avatar.getURL() : UserAvatarEntity.DEFAULT_AVATAR.getURL();
    }

    @JsonIgnore
    public Long getLastSeenNotification() {
        return lastSeenNotification;
    }

    public void setLastSeenNotification(Long lastSeenNotification) {
        this.lastSeenNotification = lastSeenNotification;
    }

    public static PanacheQuery<UserEntity> findByUsername(String username) {
        return find("username = ?1", username);
    }

}
