package com.example.notifications.service;

import java.util.Optional;

import com.example.notifications.model.Notification;

public interface NotificationRepository {

    Optional<Notification> findById(String id);

    void save(Notification notification);
}
