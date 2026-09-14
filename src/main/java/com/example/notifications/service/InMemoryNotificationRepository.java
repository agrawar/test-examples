package com.example.notifications.service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.example.notifications.model.Notification;

public class InMemoryNotificationRepository implements NotificationRepository {

    private final Map<String, Notification> notifications = new ConcurrentHashMap<>();

    @Override
    public Optional<Notification> findById(String id) {
        return Optional.ofNullable(notifications.get(id));
    }

    @Override
    public void save(Notification notification) {
        notifications.put(notification.id(), notification);
    }
}
