package com.example.notifications.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class NotificationDeliveryStatusTest {

    @Test
    void mapsChannelTypeAndStatus() {
        NotificationDeliveryStatus status = new NotificationDeliveryStatus(
                DeliveryChannel.EMAIL,
                NotificationType.MENTION,
                NotificationStatus.DELIVERED);

        assertEquals(DeliveryChannel.EMAIL, status.channel());
        assertEquals(NotificationType.MENTION, status.type());
        assertEquals(NotificationStatus.DELIVERED, status.status());
    }

    @Test
    void requiresChannel() {
        assertThrows(NullPointerException.class, () ->
                new NotificationDeliveryStatus(null, NotificationType.COMMENT, NotificationStatus.RECEIVED));
    }

    @Test
    void requiresType() {
        assertThrows(NullPointerException.class, () ->
                new NotificationDeliveryStatus(DeliveryChannel.IN_APP, null, NotificationStatus.RECEIVED));
    }

    @Test
    void requiresStatus() {
        assertThrows(NullPointerException.class, () ->
                new NotificationDeliveryStatus(DeliveryChannel.IN_APP, NotificationType.SHARE, null));
    }
}
