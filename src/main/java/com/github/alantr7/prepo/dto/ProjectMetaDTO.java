package com.github.alantr7.prepo.dto;

import com.github.alantr7.prepo.entity.ProjectEntity;

public class ProjectMetaDTO {
    
    public int builds;

    public int openIssues;

    public int closedIssues;

    public int activeMilestones;

    public ProjectMetaDTO(ProjectEntity project) {
        this.builds = project.getBuildsCount();
        this.openIssues = (int) project.getOpenIssues();
        this.closedIssues = (int) project.getClosedIssues();
        this.activeMilestones = (int) project.getActiveMilestones();
    }

}
