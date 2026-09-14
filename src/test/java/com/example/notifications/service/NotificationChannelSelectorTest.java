package com.example.notifications.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import com.example.notifications.model.DeliveryChannel;
import com.example.notifications.model.Notification;
import com.example.notifications.model.NotificationType;
import com.example.notifications.model.User;

class NotificationChannelSelectorTest {

    private static final Instant TIMESTAMP = Instant.parse("2026-09-14T08:00:00Z");
    private static final String USER_ID = "user-1";

    private final NotificationChannelSelector selector = new NotificationChannelSelector();

    @ParameterizedTest
    @EnumSource(DeliveryChannel.class)
    void usesOnlyTheSingleSelectedChannel(DeliveryChannel selectedChannel) {
        User user = new User(USER_ID, Map.of(
                NotificationType.MENTION, List.of(selectedChannel)));
        Notification notification = mention("You were mentioned");

        assertEquals(List.of(selectedChannel), selector.select(notification, user));
    }

    @Test
    void usesBothSelectedChannels() {
        User user = new User(USER_ID, Map.of(
                NotificationType.SHARE, List.of(DeliveryChannel.IN_APP, DeliveryChannel.EMAIL)));
        Notification notification = share("A file was shared with you");

        assertEquals(
                List.of(DeliveryChannel.IN_APP, DeliveryChannel.EMAIL),
                selector.select(notification, user));
    }

    @Test
    void appliesOnlyTheChannelsChosenForThatNotificationType() {
        User user = new User(USER_ID, Map.of(
                NotificationType.MENTION, List.of(DeliveryChannel.IN_APP),
                NotificationType.SHARE, List.of(DeliveryChannel.IN_APP, DeliveryChannel.EMAIL),
                NotificationType.COMMENT, List.of(DeliveryChannel.EMAIL)));

        assertEquals(
                List.of(DeliveryChannel.IN_APP),
                selector.select(mention("Alex mentioned you"), user));
        assertEquals(
                List.of(DeliveryChannel.IN_APP, DeliveryChannel.EMAIL),
                selector.select(share("Alex shared a doc"), user));
        assertEquals(
                List.of(DeliveryChannel.EMAIL),
                selector.select(comment("Alex commented"), user));
    }

    @Test
    void doesNotUseChannelsSelectedForADifferentNotificationType() {
        User user = new User(USER_ID, Map.of(
                NotificationType.MENTION, List.of(DeliveryChannel.IN_APP),
                NotificationType.SHARE, List.of(DeliveryChannel.EMAIL)));

        assertEquals(List.of(DeliveryChannel.IN_APP), selector.select(mention("mentioned"), user));
        assertEquals(List.of(DeliveryChannel.EMAIL), selector.select(share("shared"), user));
    }

    @Test
    void rejectsMismatchedRecipient() {
        User user = new User(USER_ID, Map.of(
                NotificationType.MENTION, List.of(DeliveryChannel.IN_APP)));
        Notification notification = new Notification(
                "n-other",
                "someone-else",
                "Alex mentioned you",
                NotificationType.MENTION,
                TIMESTAMP);

        assertThrows(IllegalArgumentException.class, () -> selector.select(notification, user));
    }

    private static Notification mention(String message) {
        return new Notification("mention-1", USER_ID, message, NotificationType.MENTION, TIMESTAMP);
    }

    private static Notification share(String message) {
        return new Notification("share-1", USER_ID, message, NotificationType.SHARE, TIMESTAMP);
    }

    private static Notification comment(String message) {
        return new Notification("comment-1", USER_ID, message, NotificationType.COMMENT, TIMESTAMP);
    }
}
