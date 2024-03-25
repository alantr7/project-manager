package com.github.alantr7.prepo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.io.File;
import java.util.UUID;

@Entity(name = "projects_files")
public class ProjectFileEntity extends PanacheEntityBase {

    @Id
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    ProjectEntity project;
    
    String name;

    long uploadTimestamp;

    long fileSize;

    ProjectFileEntity() {
    }

    ProjectFileEntity(UUID id) {
        this.id = id.toString();
    }

    public String getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @JsonIgnore
    public ProjectEntity getProject() {
        return project;
    }

    public long getUploadTimestamp() {
        return uploadTimestamp;
    }

    public long getSize() {
        return fileSize;
    }

    public void setSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getURL() {
        return "/api/v1/projects/" + project.getId() + "/files/" + id;
    }

    public String getDownloadURL() {
        return "/api/v1/projects/" + project.getId() + "/files/" + id + "/download";
    }

    @JsonIgnore
    public File getBinary() {
        return new File(System.getProperty("user.dir"), "files/" + id);
    }

}
