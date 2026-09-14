package com.example.notifications.service;

import java.util.List;
import java.util.Objects;

import com.example.notifications.model.DeliveryChannel;
import com.example.notifications.model.Notification;
import com.example.notifications.model.User;

public class NotificationChannelSelector {

    public List<DeliveryChannel> select(Notification notification, User user) {
        Objects.requireNonNull(notification, "notification is required");
        Objects.requireNonNull(user, "user is required");
        if (!user.userId().equals(notification.recipient())) {
            throw new IllegalArgumentException("notification recipient does not match user");
        }
        return user.channelsFor(notification.type());
    }
}
