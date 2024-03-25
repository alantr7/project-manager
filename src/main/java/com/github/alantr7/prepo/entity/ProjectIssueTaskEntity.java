package com.github.alantr7.prepo.entity;

import java.time.Instant;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.fasterxml.jackson.annotation.JsonGetter;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

@Entity(name = "issue_tasks")
public class ProjectIssueTaskEntity extends PanacheEntityBase {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
        name = "UUID",
        strategy = "org.hibernate.id.UUIDGenerator"
    )
    private String id;

    @ManyToOne
    @JoinColumn(name = "issue_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private ProjectIssueEntity issue;

    @Column(columnDefinition = "VARCHAR(255) DEFAULT \"\" NOT NULL")
    private String text;

    private Date creationDate;

    @Column(columnDefinition = "BOOLEAN DEFAULT FALSE NOT NULL")
    private boolean isCompleted;

    ProjectIssueTaskEntity() {
    }
    
    public ProjectIssueTaskEntity(ProjectIssueEntity issue) {
        this.issue = issue;
        this.creationDate = Date.from(Instant.now());
    }

    public String getId() {
        return id;
    }
    
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    @JsonGetter("completed")
    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean isCompleted) {
        this.isCompleted = isCompleted;
    }

}
