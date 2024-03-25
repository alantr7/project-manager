package com.github.alantr7.prepo.entity;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.ws.rs.DefaultValue;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.quarkus.hibernate.orm.panache.PanacheEntity;

@Entity
public class ProjectGroupEntity extends PanacheEntity {

    public static final ProjectGroupEntity DEFAULT = new ProjectGroupEntity();

    static {
        DEFAULT.id = 0L;
        DEFAULT.icon = "https://stats.invincibleanarchy.net/static/images/favicon.gif";
        DEFAULT.name = "default-group";
    }

    String name;

    @DefaultValue("")
    String icon;

    @JoinColumn(name = "workspace_id")
    @JsonIgnore
    @ManyToOne
    WorkspaceEntity workspace;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIcon() {
        return icon != null ? icon : DEFAULT.icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public void setWorkspace(WorkspaceEntity workspace) {
        this.workspace = workspace;
    }

}
