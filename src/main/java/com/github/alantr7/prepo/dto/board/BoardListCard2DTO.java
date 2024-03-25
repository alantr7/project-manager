package com.github.alantr7.prepo.dto.board;

import com.github.alantr7.prepo.dto.ProjectIssueDTO;
import com.github.alantr7.prepo.entity.ProjectIssueEntity;
import com.github.alantr7.prepo.entity.UserEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.Date;

@RegisterForReflection
public class BoardListCard2DTO {

    public Long id;

    @JsonIgnore
    public Long listId;

    public String project;

    public ProjectIssueDTO issue;

    public BoardListCard2DTO(Long listId, Long id, Long issueId, String projectId, String issueTitle, String description, Date creationDate, ProjectIssueEntity.State state, int ind, UserEntity author) {
        this.listId = listId != null ? listId : 0;
        this.id = id != null ? id : issueId;
        this.project = projectId;
        this.issue = new ProjectIssueDTO(issueId, issueTitle, description, creationDate, state, ind, author);
    }

}
