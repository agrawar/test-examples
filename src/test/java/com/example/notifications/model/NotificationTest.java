package com.example.notifications.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class NotificationTest {

    private static final Instant TIMESTAMP = Instant.parse("2026-09-14T08:00:00Z");

    @Test
    void createsNotificationWithRequiredFieldsAndNoChannelStatuses() {
        Notification notification = new Notification(
                "n-1",
                "user-1",
                "Alex mentioned you in a comment",
                NotificationType.MENTION,
                TIMESTAMP);

        assertEquals("n-1", notification.id());
        assertEquals("user-1", notification.recipient());
        assertEquals("Alex mentioned you in a comment", notification.message());
        assertEquals(NotificationType.MENTION, notification.type());
        assertEquals(TIMESTAMP, notification.timestamp());
        assertEquals(List.of(), notification.channelStatuses());
    }

    @Test
    void tracksStatusPerDeliveryChannel() {
        Notification notification = new Notification(
                "n-1",
                "user-1",
                "hello",
                NotificationType.SHARE,
                TIMESTAMP)
                .withChannelStatus(DeliveryChannel.IN_APP, NotificationStatus.RECEIVED)
                .withChannelStatus(DeliveryChannel.EMAIL, NotificationStatus.DELIVERED);

        assertEquals(NotificationStatus.RECEIVED, notification.statusFor(DeliveryChannel.IN_APP).orElseThrow());
        assertEquals(NotificationStatus.DELIVERED, notification.statusFor(DeliveryChannel.EMAIL).orElseThrow());
        assertTrue(notification.statusFor(DeliveryChannel.IN_APP).isPresent());
    }

    @Test
    void withChannelStatusReplacesExistingChannel() {
        Notification notification = new Notification(
                "n-1",
                "user-1",
                "hello",
                NotificationType.MENTION,
                TIMESTAMP)
                .withChannelStatus(DeliveryChannel.IN_APP, NotificationStatus.RECEIVED)
                .withChannelStatus(DeliveryChannel.IN_APP, NotificationStatus.DELIVERED);

        assertEquals(
                List.of(new NotificationDeliveryStatus(
                        DeliveryChannel.IN_APP, NotificationType.MENTION, NotificationStatus.DELIVERED)),
                notification.channelStatuses());
    }

    @Test
    void rejectsChannelStatusForADifferentNotificationType() {
        assertThrows(IllegalArgumentException.class, () ->
                new Notification(
                        "n-1",
                        "user-1",
                        "hello",
                        NotificationType.MENTION,
                        TIMESTAMP,
                        List.of(new NotificationDeliveryStatus(
                                DeliveryChannel.EMAIL,
                                NotificationType.SHARE,
                                NotificationStatus.RECEIVED))));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t"})
    void requiresId(String id) {
        assertThrows(IllegalArgumentException.class, () ->
                new Notification(id, "user-1", "hello", NotificationType.COMMENT, TIMESTAMP));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t"})
    void requiresRecipient(String recipient) {
        assertThrows(IllegalArgumentException.class, () ->
                new Notification("n-1", recipient, "hello", NotificationType.COMMENT, TIMESTAMP));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t"})
    void requiresMessage(String message) {
        assertThrows(IllegalArgumentException.class, () ->
                new Notification("n-1", "user-1", message, NotificationType.SHARE, TIMESTAMP));
    }

    @Test
    void requiresSupportedType() {
        assertThrows(IllegalArgumentException.class, () ->
                new Notification("n-1", "user-1", "hello", null, TIMESTAMP));
    }

    @Test
    void requiresTimestamp() {
        assertThrows(IllegalArgumentException.class, () ->
                new Notification("n-1", "user-1", "hello", NotificationType.COMMENT, null));
    }
}
