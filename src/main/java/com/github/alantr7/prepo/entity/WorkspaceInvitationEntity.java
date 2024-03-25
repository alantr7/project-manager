package com.github.alantr7.prepo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.quarkus.hibernate.orm.panache.PanacheEntity;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity(name = "workspaces_invitations")
public class WorkspaceInvitationEntity extends PanacheEntity {

    @JoinColumn(name = "workspace_id")
    @ManyToOne
    WorkspaceEntity workspace;

    @ManyToOne
    @JoinColumn(name = "user_id")
    UserEntity user;

    @ManyToOne
    @JoinColumn(name = "author_id")
    UserEntity author;

    protected WorkspaceInvitationEntity() {
    }

    public WorkspaceInvitationEntity(WorkspaceEntity workspace, UserEntity author, UserEntity user) {
        this.workspace = workspace;
        this.user = user;
        this.author = author;
    }

    public UserEntity getAuthor() {
        return author;
    }

    @JsonIgnore
    public UserEntity getUser() {
        return user;
    }

    public WorkspaceEntity getWorkspace() {
        return workspace;
    }

    public void setAuthor(UserEntity author) {
        this.author = author;
    }

}
