package com.github.alantr7.prepo.dto;

import java.util.List;
import java.util.Objects;

import com.github.alantr7.prepo.entity.ProjectGroupEntity;
import com.github.alantr7.prepo.entity.WorkspaceEntity;

public class ListGroupsGroupDTO {

    public long id;

    public String name;

    private String icon;

/*
    public List<ProjectEntity> projects;

    public ListGroupsGroupDTO(ProjectGroupEntity group) {
        this.id = group.id;
        this.name = group.getName();
        this.icon = group.getIcon();
        this.projects = ProjectGroupEntity.DEFAULT == group
            ? ProjectEntity.find("group_id = null").list()
            : ProjectEntity.find("group_id = ?1", group.id).list();
    }*/

    public List<ListGroupsProjectDTO> projects;

    public ListGroupsGroupDTO(WorkspaceEntity workspace, ProjectGroupEntity group) {
        this.id = group.id;
        this.name = group.getName();
        this.icon = group.getIcon();
        String query = "SELECT NEW com.alantrumic.ednevnik.dto.ListGroupsProjectDTO(p.id, p.name, p.icon, :groupId, p.latestBuild, p.version, " +
                "SUM(CASE WHEN i.isResolved = true THEN 1 ELSE 0 END), " +
                "SUM(CASE WHEN i.isResolved = false THEN 1 ELSE 0 END), " +
                "COUNT(b)" +
                ") " +
                "FROM projects p " +
                "LEFT JOIN issues i ON p.id = i.project.id " +
                "LEFT JOIN builds b ON p.id = b.project.id " +
                "WHERE p.workspace.id = :workspaceId AND p.group.id :groupFilter " +
                "GROUP BY p.id, p.name, p.icon, p.latestBuild, p.version";

        query = query.replace(":groupId", String.valueOf(id)).replace(":groupFilter", Objects.equals(group.id, 0L) ? "IS NULL" : ("= " + group.id.toString()));

        var results = ProjectGroupEntity.getEntityManager().createQuery(query, ListGroupsProjectDTO.class)
                .setParameter("workspaceId", workspace.id)
                .getResultList();

        projects = (List<ListGroupsProjectDTO>) results;
    }

    public String getIcon() {
        return icon != null ? icon : ProjectGroupEntity.DEFAULT.getIcon();
    }

}
