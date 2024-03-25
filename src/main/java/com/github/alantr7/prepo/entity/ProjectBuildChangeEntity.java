package com.github.alantr7.prepo.entity;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

@Entity(name = "builds_changelogs")
public class ProjectBuildChangeEntity extends PanacheEntity {

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "build_id")
    ProjectBuildEntity build;

    @ManyToOne
    @JoinColumn(name = "issue_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    ProjectIssueEntity issue;

    String text;

    @ColumnDefault("text")
    String type = "text";

    public void setBuild(ProjectBuildEntity build) {
        this.build = build;
    }

    public void setIssue(ProjectIssueEntity issue) {
        this.issue = issue;
        this.type = "issue_ref";
        this.text = null;
    }

    public void setText(String content) {
        this.text = content;
        this.type = "text";
        this.issue = null;
    }

    public ProjectIssueEntity getIssue() {
        return issue;
    }

    public String getText() {
        return text;
    }

    public String getType() {
        return type;
    }

}
