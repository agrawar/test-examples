package com.example.notifications.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.example.notifications.model.DeliveryChannel;
import com.example.notifications.model.Notification;
import com.example.notifications.model.NotificationDeliveryStatus;
import com.example.notifications.model.NotificationStatus;
import com.example.notifications.model.NotificationType;

class NotificationRequestServiceTest {

    private static final Instant TIMESTAMP = Instant.parse("2026-09-14T08:00:00Z");

    private final NotificationRepository repository = new InMemoryNotificationRepository();
    private final NotificationRequestService service = new NotificationRequestService(repository);

    @Test
    void storesNewNotificationWithoutChannelStatuses() {
        Notification notification = mention("n-1");

        Notification received = service.receive(notification);

        assertEquals(List.of(), received.channelStatuses());
        assertEquals(received, repository.findById("n-1").orElseThrow());
    }

    @Test
    void clearsChannelStatusesOnReceive() {
        Notification notification = mention("n-1").withChannelStatus(
                DeliveryChannel.EMAIL, NotificationStatus.DELIVERED);

        Notification received = service.receive(notification);

        assertEquals(List.of(), received.channelStatuses());
        assertEquals(List.of(), repository.findById("n-1").orElseThrow().channelStatuses());
    }

    @Test
    void rejectsDuplicateIdBeforeSavingWhenAlreadyReceived() {
        Notification first = mention("n-1");
        service.receive(first);

        assertThrows(IllegalArgumentException.class, () -> service.receive(mention("n-1")));
        assertEquals(first.withChannelStatuses(List.of()), repository.findById("n-1").orElseThrow());
    }

    @Test
    void rejectsDuplicateIdBeforeSavingWhenAChannelWasDelivered() {
        Notification stored = mention("n-1").withChannelStatus(
                DeliveryChannel.IN_APP, NotificationStatus.DELIVERED);
        repository.save(stored);

        assertThrows(IllegalArgumentException.class, () -> service.receive(mention("n-1")));
        assertEquals(
                List.of(new NotificationDeliveryStatus(
                        DeliveryChannel.IN_APP, NotificationType.MENTION, NotificationStatus.DELIVERED)),
                repository.findById("n-1").orElseThrow().channelStatuses());
    }

    @Test
    void acceptsADifferentNotificationId() {
        service.receive(mention("n-1"));

        Notification second = service.receive(mention("n-2"));

        assertEquals(List.of(), repository.findById("n-1").orElseThrow().channelStatuses());
        assertEquals(List.of(), second.channelStatuses());
        assertEquals(second, repository.findById("n-2").orElseThrow());
    }

    @Test
    void rejectsNullNotification() {
        assertThrows(IllegalArgumentException.class, () -> service.receive(null));
    }

    private static Notification mention(String id) {
        return new Notification(id, "user-1", "Alex mentioned you", NotificationType.MENTION, TIMESTAMP);
    }
}
