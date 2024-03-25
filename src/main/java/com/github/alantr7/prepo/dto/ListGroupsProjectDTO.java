package com.github.alantr7.prepo.dto;

import com.github.alantr7.prepo.entity.ProjectGroupEntity;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class ListGroupsProjectDTO {

    public String id;

    public String name;

    public String icon;

    public int latestBuild;

    public String version;

    public Meta meta;

    public Group group;

    public ListGroupsProjectDTO(String id, String name, String icon, int group, int latestBuild, String version, long openIssues, long closedIssues, long builds) {
        this.id = id;
        this.name = name;
        this.icon = icon;
        this.latestBuild = latestBuild;
        this.version = version;
        this.meta = new Meta();
        this.group = new Group();
        this.group.id = group;

        meta.openIssues = openIssues;
        meta.closedIssues = closedIssues;
        meta.builds = builds;
    }

    public String getIcon() {
        return icon != null ? icon : ProjectGroupEntity.DEFAULT.getIcon();
    }

    static class Meta {

        public long openIssues;

        public long closedIssues;

        public long builds;

    }

    static class Group {

        public long id;

    }

}
