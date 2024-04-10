package com.github.alantr7.prepo.util;

import java.util.function.Function;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.github.alantr7.prepo.dto.logs.BuildPOJO;
import com.github.alantr7.prepo.dto.logs.IssuePOJO;
import com.github.alantr7.prepo.entity.*;
import com.github.alantr7.prepo.entity.LogEntryEntity.Action;
import com.fasterxml.jackson.databind.ObjectMapper;

@Singleton
public class LogUtils {

    @Inject
    ObjectMapper mapper;

    public LogEntryEntity createEntryForIssueOpen(UserEntity user, ProjectIssueEntity issue) {
        return _attemptEntryPersist(
                new IssuePOJO(issue.getTitle(), issue.getDescription(), issue.getProject().getName(), issue.getIndex()),
                contextual -> new LogEntryEntity(user, Action.OPEN_ISSUE, issue.id, contextual),
                issue.getProject());
    }

    public LogEntryEntity createEntryForIssueClose(UserEntity user, ProjectIssueEntity issue) {
        return _attemptEntryPersist(
                new IssuePOJO(issue.getTitle(), issue.getDescription(), issue.getProject().getName(), issue.getIndex()),
                contextual -> new LogEntryEntity(user, Action.CLOSE_ISSUE, issue.id, contextual),
                issue.getProject());
    }

    public LogEntryEntity createEntryForBuildCreate(UserEntity user, ProjectBuildEntity build) {
        return _attemptEntryPersist(
                new BuildPOJO(null, build.getVersion(), build.getProject().getName()),
                contextual -> new LogEntryEntity(user, Action.CREATE_BUILD, build.id, contextual),
                build.getProject());
    }

    private LogEntryEntity _attemptEntryPersist(Object pojo, Function<String, LogEntryEntity> consumer, ProjectEntity project) {
        String json;

        try {
            json = mapper.writeValueAsString(pojo);
        } catch (Exception e) {
            return null;
        }

        if (json == null)
            return null;

        var entry = consumer.apply(json);
        if (entry != null) {
            entry.persist();

            if (project != null)
                entry.setProjectName(project.getName());
        }

        return entry;
    }

}
