package com.github.alantr7.prepo.entity;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

@Entity(name = "notifications_recipients")
public class UserNotificationRecipientEntity extends PanacheEntity {

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "notification_id")
    UserNotificationEntity notification;

    @JoinColumn(name = "recipient_id")
    @OneToOne
    UserEntity user;

    UserNotificationRecipientEntity() {
    }

    public UserNotificationRecipientEntity(UserNotificationEntity notification, UserEntity user) {
        this.notification = notification;
        this.user = user;
    }

    public UserNotificationEntity getNotification() {
        return notification;
    }

    public UserEntity getUser() {
        return user;
    }
    
}
