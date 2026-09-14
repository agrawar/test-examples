package com.example.notifications.service;

import com.example.notifications.model.DeliveryChannel;
import com.example.notifications.model.Notification;

public interface NotificationSender {

    boolean send(Notification notification, DeliveryChannel channel);
}
