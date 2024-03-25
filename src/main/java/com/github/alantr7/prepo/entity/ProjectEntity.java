package com.github.alantr7.prepo.entity;

import com.github.alantr7.prepo.dto.ProjectMetaDTO;
import com.github.alantr7.prepo.entity.ProjectBuildEntity.BuildChannel;
import com.fasterxml.jackson.annotation.JsonIgnore;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;

import java.math.BigInteger;
import java.sql.Date;
import java.time.Instant;
import java.util.Collection;
import java.util.UUID;

@Entity(name = "projects")
public class ProjectEntity extends PanacheEntityBase {

    @Id
    private String id;

    private String icon;

    private String name;

    @Column(name = "short_description", columnDefinition = "VARCHAR(128) NOT NULL DEFAULT ''")
    private String shortDescription;

    private long lastUpdated;

    private int latestBuild;

    private int latestIssue = 1;

    @OneToMany
    @LazyCollection(LazyCollectionOption.TRUE)
    @JoinColumn(name = "project_id")
    private Collection<ProjectFileEntity> files;

    @OneToMany(mappedBy = "project", fetch = FetchType.LAZY)
    private Collection<ProjectBuildEntity> builds;

    @ManyToOne
    @JoinColumn(name = "group_id")
    ProjectGroupEntity group = ProjectGroupEntity.DEFAULT;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id")
    WorkspaceEntity workspace;

    @Column(columnDefinition = "VARCHAR(255) NOT NULL DEFAULT '0.1.0'")
    String version = "0.1.0";

    ProjectEntity() {
    }

    public ProjectEntity(String id) {
        this.id = id;
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

    public String getIcon() {
        return icon != null ? icon : group != null ? group.getIcon() : ProjectGroupEntity.DEFAULT.icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public int getLatestBuild() {
        return latestBuild;
    }

    public ProjectMetaDTO getMeta() {
        return new ProjectMetaDTO(this);
    }

    @JsonIgnore
    public int getBuildsCount() {
        var query = getEntityManager().createNativeQuery("SELECT COUNT(*) FROM builds WHERE project_id = :pid");
        query.setParameter("pid", id);

        return ((BigInteger) query.getSingleResult()).intValue();
    }

    public ProjectFileEntity createFile() {
        var file = new ProjectFileEntity(UUID.randomUUID());
        file.project = this;
        file.uploadTimestamp = lastUpdated = System.currentTimeMillis();
        file.persist();

        return file;
    }

    @JsonIgnore
    public Collection<ProjectFileEntity> getFiles() {
        return files;
    }

    public ProjectIssueEntity createIssue(UserEntity user, String title) {
        var issue = new ProjectIssueEntity();
        issue.project = this;
        issue.author = user;
        issue.ind = latestIssue++;
        issue.setTitle(title);
        issue.creationDate = Date.from(Instant.now());
        issue.setState(ProjectIssueEntity.State.DEFAULT);

        issue.persist();
        return issue;
    }

    public ProjectBuildEntity createBuild() {
        var build = new ProjectBuildEntity(this, BuildChannel.RELEASE);
        build.setBuild(++this.latestBuild);
        build.creationDate = Date.from(Instant.now());

        build.persist();
        return build;
    }

    @JsonIgnore
    public Collection<ProjectIssueEntity> getIssues() {
        return getIssues(false);
    }

    @JsonIgnore
    public Collection<ProjectIssueEntity> getIssues(boolean resolved) {
        return ProjectIssueEntity.find("project_id = ?1 and isResolved = ?2 order by id desc", id, resolved).list();
    }

    @JsonIgnore
    public long getIssuesCount() {
        return ProjectIssueEntity.count("project_id = ?1 order by id desc", id);
    }

    public long getIssuesCount(boolean resolved) {
        return ProjectIssueEntity.count("project_id = ?1 and isResolved = ?2 order by id desc", id, resolved);
    }

    public long getOpenIssues() {
        return getIssuesCount(false);
    }

    public long getClosedIssues() {
        return getIssuesCount(true);
    }

    public long getActiveMilestones() {
        return ProjectMilestoneEntity.count("project_id = ?1", id);
    }

    public ProjectGroupEntity getGroup() {
        return group != null ? group : ProjectGroupEntity.DEFAULT;
    }

    public void setGroup(ProjectGroupEntity group) {
        this.group = group;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @JsonIgnore
    public Collection<ProjectBuildEntity> getBuilds() {
        return builds;
    }

    @JsonIgnore
    public WorkspaceEntity getWorkspace() {
        return workspace;
    }

    public void setWorkspace(WorkspaceEntity workspace) {
        this.workspace = workspace;
    }

    public static String getIdFromName(String name) {
        return name.toLowerCase().replace(" ", "-");
    }

}
