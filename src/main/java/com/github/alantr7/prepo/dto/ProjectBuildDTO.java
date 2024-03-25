package com.github.alantr7.prepo.dto;

import java.util.Collection;
import java.util.Date;

import com.github.alantr7.prepo.entity.ProjectBuildEntity;
import com.github.alantr7.prepo.entity.ProjectEntity;
import com.github.alantr7.prepo.entity.ProjectIssueEntity;
import com.github.alantr7.prepo.entity.UserEntity;
import com.github.alantr7.prepo.entity.ProjectBuildEntity.BuildChannel;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class ProjectBuildDTO {
    
    public long id;

    public int build;

    public BuildChannel channel;

    public String version;

    public Date creationDate;

    public Object changelog;

    public Collection<?> files;

    public ProjectEntity project;

    public UserEntity author;

    public ProjectBuildDTO(ProjectBuildEntity build) {
        this.id = build.id;
        this.build = build.getBuild();
        this.channel = build.getChannel();
        this.version = build.getVersion();
        this.creationDate = build.getCreationDate();
        this.changelog = createChangelog(build).getList();
        this.files = build.getFiles();
        this.project = build.getProject();
        this.author = build.getAuthor();
    }

    private JsonArray createChangelog(ProjectBuildEntity build) {
        var array = new JsonArray();
        for (var change : build.getChangelog()) {
            var changeJson = new JsonObject();
            changeJson.put("id", change.id);
            changeJson.put("issue", createIssueDetails(change.getIssue()));
            changeJson.put("text", change.getText());
            changeJson.put("type", change.getType());

            array.add(changeJson.getMap());
        }

        return array;
    }

    private Object createIssueDetails(ProjectIssueEntity issue) {
        if (issue == null)
            return null;

        var object = new JsonObject();
        object.put("id", issue.id);
        object.put("title", issue.getTitle());
        object.put("description", issue.getDescription());
        object.put("labels", issue.getLabels());
        object.put("tasks", issue.getTasks());
        object.put("author", issue.getAuthor());
        object.put("resolved", issue.isResolved());
        object.put("index", issue.getIndex());
        object.put("creationDate", issue.getCreationDate());
        
        // var object = JsonObject.mapFrom(issue);
        // object.remove("project");
        // object.remove("attachments");

        return object.getMap();
    }

}
