package com.github.alantr7.prepo.dto;

import java.util.Collection;

import com.github.alantr7.prepo.entity.UserNotificationEntity;

public class NotificationsDTO {
    
    public Collection<UserNotificationEntity> notifications;

    public Long last_seen = 0L;

    public int unseen_count = 0;

    public NotificationsDTO(Collection<UserNotificationEntity> notifications, long last_seen, int unseen_count) {
        this.notifications = notifications;
        this.last_seen = last_seen;
        this.unseen_count = unseen_count;
    }

}
