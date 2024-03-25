package com.github.alantr7.prepo.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;

@Entity(name = "milestones")
public class ProjectMilestoneEntity extends PanacheEntityBase {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    private String id;

    @JoinColumn(name = "project_id")
    @ManyToOne
    private ProjectEntity project;

    @OneToMany(mappedBy = "milestone", fetch = FetchType.EAGER)
    private Collection<ProjectIssueEntity> issues;

    private String name;

    private String version;

    private Date creationDate;

    protected ProjectMilestoneEntity() {
    }

    public ProjectMilestoneEntity(ProjectEntity project) {
        this.project = project;
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

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public ProjectEntity getProject() {
        return project;
    }

    public Collection<ProjectIssueEntity> getIssues() {
        return issues != null ? issues : Collections.emptyList();
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

}
