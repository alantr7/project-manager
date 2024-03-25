package com.github.alantr7.prepo.entity;

import javax.persistence.Entity;
import javax.persistence.Id;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

@Entity(name = "labels")
public class ProjectIssueLabelEntity extends PanacheEntityBase {
    
    @Id
    String id;

    String title;

    public String backgroundColor = "white";

    public String textColor = "black";

    ProjectIssueLabelEntity() {
    }

    public ProjectIssueLabelEntity(String id) {
        this.id = id;
        this.title = id;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    
}
