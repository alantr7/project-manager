package com.github.alantr7.prepo.entity;

import java.util.Collection;
import java.util.Collections;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

@Entity(name = "workspaces")
public class WorkspaceEntity extends PanacheEntity {

    @OneToMany(mappedBy = "workspace")
    @JsonIgnore
    Collection<ProjectEntity> projects = Collections.emptyList();

    @Column(name = "board_next_pos")
    int nextBoardPosition = 1000;

    String name;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    UserEntity owner;

    @OneToMany(mappedBy = "workspace")
    @LazyCollection(LazyCollectionOption.FALSE)
    Collection<WorkspaceCollaboratorEntity> collaborators = Collections.emptyList();

    @OneToMany(mappedBy = "workspace")
    @LazyCollection(LazyCollectionOption.FALSE)
    Collection<WorkspaceInvitationEntity> invitations = Collections.emptyList();

    @Column(name = "weak_id")
    String weakId;

    public WorkspaceEntity() {
    }

    public WorkspaceEntity(String name, UserEntity owner) {
        this.name = name;
        this.owner = owner;
    }

    public String getName() {
        return name;
    }

    public Collection<ProjectEntity> getProjects() {
        return projects;
    }

    public int getNextBoardListPosition() {
        return nextBoardPosition;
    }

    public void setNextBoardListPosition(int nextBoardPosition) {
        this.nextBoardPosition = nextBoardPosition;
    }

    public Collection<WorkspaceCollaboratorEntity> getCollaborators() {
        return collaborators;
    }

    @JsonIgnore
    public Collection<WorkspaceInvitationEntity> getInvitations() {
        return invitations;
    }

    @JsonGetter("weak_id")
    public String getWeakId() {
        return weakId;
    }

    public void setWeakId(String weakId) {
        this.weakId = weakId;
    }

    public UserEntity getOwner() {
        return owner;
    }

}
