package com.github.alantr7.prepo.dto;

import com.github.alantr7.prepo.entity.ProjectEntity;
import com.github.alantr7.prepo.entity.ProjectGroupEntity;

public class ListProjectsProjectDTO {

    public String id;

    public String icon;

    public String name;

    public String description;
    
    public ProjectGroupEntity group;

    public ListProjectsProjectDTO(ProjectEntity project) {
        id = project.getId();
        name = project.getName();
        icon = project.getIcon();
        group = project.getGroup();
        description = project.getShortDescription() != null ? project.getShortDescription() : "";
    }

}
