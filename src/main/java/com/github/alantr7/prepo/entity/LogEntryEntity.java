package com.github.alantr7.prepo.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.fasterxml.jackson.annotation.JsonGetter;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

@Entity(name = "activity_logs")
public class LogEntryEntity extends PanacheEntity {
    
    public enum Action {
        OPEN_ISSUE, CLOSE_ISSUE,
        CREATE_BUILD
    }

    @JoinColumn(name = "user_id")
    @ManyToOne
    UserEntity user;

    @Enumerated(EnumType.STRING)
    Action action;

    @Column(name = "contextual_data")
    String contextualData;

    long timestamp;

    @Column(name = "contextual_id")
    String contextualId;

    LogEntryEntity() {
    }   

    public LogEntryEntity(UserEntity user, Action action, String contextualData) {
        this(user, action, null, contextualData);
    }

    public LogEntryEntity(UserEntity user, Action action, Object contextualId, String contextualData) {
        this.user = user;
        this.action = action;
        this.contextualId = contextualId != null ? contextualId.toString() : null;
        this.contextualData = contextualData;
        this.timestamp = System.currentTimeMillis();
    }

    public UserEntity getUser() {
        return user;
    }

    public Action getAction() {
        return action;
    }

    @JsonGetter("contextual_data")
    public String getContextualData() {
        return contextualData;
    }

    @JsonGetter("contextual_id")
    public String getContextualId() {
        return contextualId;
    }

    public long getTimestamp() {
        return timestamp;
    }

}
