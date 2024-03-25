package com.github.alantr7.prepo.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

import javax.persistence.*;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import com.fasterxml.jackson.annotation.JsonGetter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.stream.Collectors;

@Entity(name = "issues")
public class ProjectIssueEntity extends PanacheEntity {

    @ManyToOne
    @JoinColumn(name = "project_id")
    ProjectEntity project;

    String title;
    // Thread

    String description = "";

    @OneToMany(mappedBy = "issue", cascade = CascadeType.REMOVE)
    @LazyCollection(LazyCollectionOption.FALSE)
    Collection<ProjectIssueAttachmentEntity> attachments;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(joinColumns = @JoinColumn(name = "label_id"), inverseJoinColumns = @JoinColumn(name = "issue_id"))
    Collection<ProjectIssueLabelEntity> labels = new ArrayList<>();

    @OneToMany(mappedBy = "issue", cascade = CascadeType.REMOVE)
    @LazyCollection(LazyCollectionOption.FALSE)
    @OrderBy("creationDate")
    Collection<ProjectIssueTaskEntity> tasks = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "author_id")
    UserEntity author;

    @ManyToOne
    @JoinColumn(name = "milestone_id")
    ProjectMilestoneEntity milestone;

    int ind;

    Date creationDate;

    @Deprecated
    boolean isResolved;

    @Column(name = "state", columnDefinition = "VARCHAR(40) DEFAULT \"DEFAULT\" NOT NULL")
    @Enumerated(EnumType.STRING)
    State state;

    public enum State {
        DEFAULT, PROGRESS, RESOLVED
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Collection<ProjectFileEntity> getAttachments() {
        return attachments != null ? attachments.stream().map(att -> att.file).collect(Collectors.toList()) : Collections.emptyList();
    }

    public void setAttachments(Collection<ProjectIssueAttachmentEntity> attachments) {
        this.attachments = attachments;
    }

    public void addAttachment(ProjectIssueAttachmentEntity attachment) {
        attachments.add(attachment);
    }

    public Collection<ProjectIssueTaskEntity> getTasks() {
        return tasks;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public UserEntity getAuthor() {
        return author;
    }

    public int getIndex() {
        return ind;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    @Deprecated
    public boolean isResolved() {
        return isResolved;
    }

    @Deprecated
    public void setResolved(boolean resolved) {
        isResolved = resolved;
    }

    public State getState() {
        return state != null ? state : State.DEFAULT;
    }

    public void setState(State state) {
        this.state = state;
    }

    @JsonGetter
    public Collection<ProjectIssueLabelEntity> getLabels() {
        return labels != null ? labels : Collections.emptyList();
    }

    public void addLabel(ProjectIssueLabelEntity label) {
        labels.add(label);
    }

    public void removeLabel(ProjectIssueLabelEntity label) {
        labels.remove(label);
    }

    public void setMilestone(ProjectMilestoneEntity milestone) {
        this.milestone = milestone;
    }

    public ProjectEntity getProject() {
        return project;
    }

}
