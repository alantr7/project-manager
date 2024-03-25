package com.github.alantr7.prepo.util;

import java.util.function.Function;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.github.alantr7.prepo.dto.logs.BuildPOJO;
import com.github.alantr7.prepo.dto.logs.IssuePOJO;
import com.github.alantr7.prepo.entity.LogEntryEntity;
import com.github.alantr7.prepo.entity.ProjectBuildEntity;
import com.github.alantr7.prepo.entity.ProjectIssueEntity;
import com.github.alantr7.prepo.entity.UserEntity;
import com.github.alantr7.prepo.entity.LogEntryEntity.Action;
import com.fasterxml.jackson.databind.ObjectMapper;

@Singleton
public class LogUtils {

    @Inject
    ObjectMapper mapper;

    public LogEntryEntity createEntryForIssueOpen(UserEntity user, ProjectIssueEntity issue) {
        return _attemptEntryPersist(
                new IssuePOJO(issue.getTitle(), issue.getDescription(), issue.getProject().getName(), issue.getIndex()),
                contextual -> new LogEntryEntity(user, Action.OPEN_ISSUE, issue.id, contextual));
    }

    public LogEntryEntity createEntryForIssueClose(UserEntity user, ProjectIssueEntity issue) {
        return _attemptEntryPersist(
                new IssuePOJO(issue.getTitle(), issue.getDescription(), issue.getProject().getName(), issue.getIndex()),
                contextual -> new LogEntryEntity(user, Action.CLOSE_ISSUE, issue.id, contextual));
    }

    public LogEntryEntity createEntryForBuildCreate(UserEntity user, ProjectBuildEntity build) {
        return _attemptEntryPersist(
                new BuildPOJO(null, build.getVersion(), build.getProject().getName()),
                contextual -> new LogEntryEntity(user, Action.CREATE_BUILD, build.id, contextual));
    }

    private LogEntryEntity _attemptEntryPersist(Object pojo, Function<String, LogEntryEntity> consumer) {
        String json;

        try {
            json = mapper.writeValueAsString(pojo);
        } catch (Exception e) {
            return null;
        }

        if (json == null)
            return null;

        var entry = consumer.apply(json);
        if (entry != null)
            entry.persist();

        return entry;
    }

}
