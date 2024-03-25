package com.github.alantr7.prepo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.quarkus.hibernate.orm.panache.PanacheEntity;

import javax.persistence.*;

@Entity(name = "workspaces_collaborators")
public class WorkspaceCollaboratorEntity extends PanacheEntity {

    @JoinColumn(name = "workspace_id")
    @ManyToOne
    WorkspaceEntity workspace;

    @ManyToOne
    @JoinColumn(name = "user_id")
    UserEntity user;

    @Column(name = "role", columnDefinition = "VARCHAR(16) NOT NULL DEFAULT 'COLLABORATOR'")
    @Enumerated(EnumType.STRING)
    Role role = Role.COLLABORATOR;

    public enum Role {
        ADMIN, COLLABORATOR
    }

    protected WorkspaceCollaboratorEntity() {
    }

    public WorkspaceCollaboratorEntity(WorkspaceEntity workspace, UserEntity user) {
        this.workspace = workspace;
        this.user = user;
    }

    @JsonIgnore
    public UserEntity getUser() {
        return user;
    }

    public Role getRole() {
        return role;
    }

    @JsonIgnore
    public WorkspaceEntity getWorkspace() {
        return workspace;
    }

}
