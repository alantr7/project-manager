package com.github.alantr7.prepo.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.Date;

@RegisterForReflection
public class ProjectIssueTaskDTO {

    public String id;

    public String text;

    public Date creationDate;

    public Boolean completed;

    @JsonIgnore
    public Long issueId;

    public ProjectIssueTaskDTO(String id, String text, Date creationDate, Boolean isCompleted, Long issueId) {
        this.id = id;
        this.text = text;
        this.creationDate = creationDate;
        this.completed = isCompleted;
        this.issueId = issueId;
    }

}
