package com.github.alantr7.prepo.entity;

import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import com.github.alantr7.prepo.dto.NotificationSerializer;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

@Entity(name = "notifications")
@JsonSerialize(using = NotificationSerializer.class)
public class UserNotificationEntity extends PanacheEntity {
    
    public enum NotificationType {
        NEW_BUILD, NEW_ISSUE, TEXT
    }

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "build_id")
    @ColumnDefault("NULL")
    ProjectBuildEntity new_build;

    String text;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "notification")
    Collection<UserNotificationRecipientEntity> recipients;

    @ColumnDefault("TEXT")
    NotificationType type = NotificationType.TEXT;

    Date date;

    @JsonIgnore
    public Collection<UserEntity> getRecipients() {
        return recipients.stream().map(UserNotificationRecipientEntity::getUser).collect(Collectors.toList());
    }

    public NotificationType getType() {
        return type;
    }

    @JsonIgnore
    public ProjectBuildEntity getNewBuild() {
        return new_build;
    }

    @JsonIgnore
    public String getText() {
        return text;
    }

    public Date getDate() {
        return date;
    }

    public static <T> UserNotificationEntity create(Type<T> type, T value, Collection<UserEntity> recipients) {
        var notification = new UserNotificationEntity();
        if (type == Type.NEW_BUILD) {
            notification.new_build = (ProjectBuildEntity) value;
            notification.type = NotificationType.NEW_BUILD;
        }
        else if (type == Type.TEXT) {
            notification.text = (String) value;
        }

        notification.date = Date.from(Instant.now());
        notification.persist();

        notification.recipients = recipients.stream().map(user -> {
            var recipient = new UserNotificationRecipientEntity(notification, user);
            recipient.persist();

            return recipient;
        }).collect(Collectors.toList());
        return notification;
    }

    public static class Type<T> {

        public static Type<ProjectBuildEntity> NEW_BUILD = new Type<>();

        public static Type<String> TEXT = new Type<>();

    }

}
