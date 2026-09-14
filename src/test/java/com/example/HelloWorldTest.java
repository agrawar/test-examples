package com.example;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.example.notifications.model.DeliveryChannel;
import com.example.notifications.model.Notification;
import com.example.notifications.model.NotificationStatus;
import com.example.notifications.model.NotificationType;

class HelloWorldTest {

    @Test
    void runMentionRequestReceivesAndDeliversThroughChosenChannels() {
        Notification stored = new HelloWorld().runMentionRequest();

        assertEquals("n-1", stored.id());
        assertEquals("user-1", stored.recipient());
        assertEquals("Alex mentioned you", stored.message());
        assertEquals(NotificationType.MENTION, stored.type());
        assertEquals(
                List.of(DeliveryChannel.IN_APP, DeliveryChannel.EMAIL),
                stored.channelStatuses().stream()
                        .map(status -> status.channel())
                        .toList());
        assertEquals(NotificationStatus.DELIVERED, stored.statusFor(DeliveryChannel.IN_APP).orElseThrow());
        assertEquals(NotificationStatus.DELIVERED, stored.statusFor(DeliveryChannel.EMAIL).orElseThrow());
    }
}
