package com.example.notifications.service;

import java.util.ArrayList;
import java.util.List;

import com.example.notifications.model.DeliveryChannel;
import com.example.notifications.model.Notification;
import com.example.notifications.model.NotificationStatus;
import com.example.notifications.model.User;

public class NotificationDeliveryService {

    private final NotificationChannelSelector channelSelector;
    private final NotificationSender sender;
    private final NotificationRepository repository;

    public NotificationDeliveryService(
            NotificationChannelSelector channelSelector,
            NotificationSender sender,
            NotificationRepository repository
    ) {
        this.channelSelector = channelSelector;
        this.sender = sender;
        this.repository = repository;
    }

    public List<DeliveryChannel> deliver(Notification notification, User user) {
        List<DeliveryChannel> delivered = new ArrayList<>();
        Notification current = notification;
        for (DeliveryChannel channel : channelSelector.select(notification, user)) {
            current = current.withChannelStatus(channel, NotificationStatus.RECEIVED);
            if (sender.send(current, channel)) {
                delivered.add(channel);
                current = current.withChannelStatus(channel, NotificationStatus.DELIVERED);
            }
        }
        repository.save(current);
        return List.copyOf(delivered);
    }
}
