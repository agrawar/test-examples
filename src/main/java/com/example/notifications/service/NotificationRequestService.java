package com.example.notifications.service;

import java.util.List;

import com.example.notifications.model.Notification;

public class NotificationRequestService {

    private final NotificationRepository repository;

    public NotificationRequestService(NotificationRepository repository) {
        this.repository = repository;
    }

    public Notification receive(Notification notification) {
        if (notification == null) {
            throw new IllegalArgumentException("notification is required");
        }
        if (repository.findById(notification.id()).isPresent()) {
            throw new IllegalArgumentException("duplicate notification");
        }
        Notification received = notification.withChannelStatuses(List.of());
        repository.save(received);
        return received;
    }
}
