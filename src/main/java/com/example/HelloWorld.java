package com.example;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import com.example.notifications.model.DeliveryChannel;
import com.example.notifications.model.Notification;
import com.example.notifications.model.NotificationType;
import com.example.notifications.model.User;
import com.example.notifications.service.InMemoryNotificationRepository;
import com.example.notifications.service.NotificationChannelSelector;
import com.example.notifications.service.NotificationDeliveryService;
import com.example.notifications.service.NotificationRequestService;

public class HelloWorld {

    public Notification runMentionRequest() {
        InMemoryNotificationRepository repository = new InMemoryNotificationRepository();
        NotificationRequestService requestService = new NotificationRequestService(repository);
        NotificationDeliveryService deliveryService = new NotificationDeliveryService(
                new NotificationChannelSelector(),
                (notification, channel) -> {
                    System.out.println("Delivering via " + channel + ": " + notification.message());
                    return true;
                },
                repository);

        User user = new User("user-1", Map.of(
                NotificationType.MENTION, List.of(DeliveryChannel.IN_APP, DeliveryChannel.EMAIL)));

        Notification request = new Notification(
                "n-1",
                user.userId(),
                "Alex mentioned you",
                NotificationType.MENTION,
                Instant.parse("2026-09-14T08:00:00Z"));

        Notification received = requestService.receive(request);
        deliveryService.deliver(received, user);
        return repository.findById(received.id()).orElseThrow();
    }

    public static void main(String[] args) {
        Notification stored = new HelloWorld().runMentionRequest();
        System.out.println("Stored notification: " + stored);
    }
}
