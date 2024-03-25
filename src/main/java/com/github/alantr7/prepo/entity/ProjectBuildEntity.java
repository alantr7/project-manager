package com.github.alantr7.prepo.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;

import javax.persistence.OneToMany;
import javax.ws.rs.DefaultValue;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

@Entity(name = "builds")
public class ProjectBuildEntity extends PanacheEntity {
    
    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "project_id")
    ProjectEntity project;

    @OneToMany(mappedBy = "build")
    Collection<ProjectBuildChangeEntity> changelog = new ArrayList<>(Collections.emptyList());

    @OneToMany(cascade = CascadeType.REMOVE)
    @JoinTable(name = "builds_files", joinColumns = @JoinColumn(name = "file_id"), inverseJoinColumns = @JoinColumn(name = "build_id"))
    @LazyCollection(LazyCollectionOption.FALSE)
    Collection<ProjectFileEntity> files = new ArrayList<>(Collections.emptyList());

    int build;

    Date creationDate;

    @ManyToOne
    @JoinColumn(name = "author_id")
    UserEntity author;

    @DefaultValue("0.1.0")
    String version;

    public enum BuildChannel {
        DEVELOPMENT, RELEASE
    }

    @DefaultValue("RELEASE")
    @Enumerated(EnumType.STRING)
    BuildChannel channel;

    ProjectBuildEntity() {
    }

    public ProjectBuildEntity(ProjectEntity project, BuildChannel channel) {
        this.project = project;
        this.channel = channel;
    }

    public ProjectBuildChangeEntity createChange(String text) {
        var change = new ProjectBuildChangeEntity();
        change.build = this;
        change.setText(text);

        change.persist();
        return change;
    }

    public ProjectBuildChangeEntity createChange(ProjectIssueEntity issue) {
        var change = new ProjectBuildChangeEntity();
        change.build = this;
        change.setIssue(issue);

        change.persist();
        return change;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public ProjectEntity getProject() {
        return project;
    }

    public UserEntity getAuthor() {
        return author;
    }

    public void setAuthor(UserEntity author) {
        this.author = author;
    }

    public Collection<ProjectBuildChangeEntity> getChangelog() {
        return changelog;
    }

    public Collection<ProjectFileEntity> getFiles() {
        return files;
    }

    public int getBuild() {
        return build;
    }

    public void setBuild(int build) {
        this.build = build;
    }

    public BuildChannel getChannel() {
        return channel;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

}
