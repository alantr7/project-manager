package com.github.alantr7.prepo.entity;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

@Entity(name = "issue_attachments")
public class ProjectIssueAttachmentEntity extends PanacheEntity {
    
    @JoinColumn(name = "issue_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    @ManyToOne
    @JsonIgnore
    public ProjectIssueEntity issue;

    @ManyToOne
    @JoinColumn(name = "file_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    public ProjectFileEntity file;

    public ProjectIssueAttachmentEntity(ProjectIssueEntity issue, ProjectFileEntity file) {
        this.issue = issue;
        this.file = file;
    }

    ProjectIssueAttachmentEntity() {
    }

}
