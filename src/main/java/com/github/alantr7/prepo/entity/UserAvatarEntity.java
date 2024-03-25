package com.github.alantr7.prepo.entity;

import java.io.File;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

@Entity(name = "avatar_files")
public class UserAvatarEntity extends PanacheEntityBase {

    @Id
    private String id;

    String name;

    @OneToOne
    UserEntity user;

    long uploadTimestamp;

    long fileSize;

    public static final UserAvatarEntity DEFAULT_AVATAR = new UserAvatarEntity("default.png", "default.png");

    UserAvatarEntity() {
    }

    UserAvatarEntity(UUID id, UserEntity user) {
        this.id = id.toString();
        this.user = user;
    }

    UserAvatarEntity(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public long getUploadTimestamp() {
        return uploadTimestamp;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getSize() {
        return fileSize;
    }

    public void setSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getURL() {
        return "/api/v1/avatars/" + id;
    }

    @JsonIgnore
    public File getBinary() {
        return new File(System.getProperty("user.dir"), "avatars/" + id);
    }

    public static UserAvatarEntity create(UserEntity user) {
        var avatar = new UserAvatarEntity(UUID.randomUUID(), user);
        return avatar;
    }

}
