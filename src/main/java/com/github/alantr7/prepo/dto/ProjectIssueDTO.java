package com.github.alantr7.prepo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.alantr7.prepo.entity.ProjectFileEntity;
import com.github.alantr7.prepo.entity.ProjectIssueEntity;
import com.github.alantr7.prepo.entity.UserEntity;
import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

@RegisterForReflection
public class ProjectIssueDTO {

    public Long id;

    public String title;

    public String description;

    public boolean resolved;

    @JsonProperty("index")
    public int ind;

    public Date creationDate;

    public ProjectIssueEntity.State state;

    public List<ProjectIssueTaskDTO> tasks = new LinkedList<>();

    public List<Object> labels = new LinkedList<>();

    public List<ProjectFileEntity> attachments = new LinkedList<>();

    public UserEntity author;

    public ProjectIssueDTO(Long id, String title, String description, Date creationDate, ProjectIssueEntity.State state, int ind, UserEntity author) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.creationDate = creationDate;
        this.state = state;
        this.ind = ind;
        this.author = author;
    }

    public void fetchMeta() {

    }

}
