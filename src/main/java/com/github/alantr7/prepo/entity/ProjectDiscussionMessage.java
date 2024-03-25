package com.github.alantr7.prepo.entity;

import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

@Entity(name = "projects_discussions")
public class ProjectDiscussionMessage extends PanacheEntityBase {
    
    @Id
    public String id;

    @ManyToOne
    @JoinColumn(name = "author_id")
    private UserEntity author;

    @ManyToOne
    @JoinColumn(name = "project_id")
    private ProjectEntity project;

    private String content;

    private long timestamp;

    ProjectDiscussionMessage() {
    }

    public ProjectDiscussionMessage(ProjectEntity project, String content) {
        this.id = UUID.randomUUID().toString();
        this.project = project;
        this.content = content;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getContent() {
        return content;
    }

}
