package com.example.notifications.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class UserTest {

    @Test
    void storesChannelsPerNotificationType() {
        User user = new User("user-1", Map.of(
                NotificationType.MENTION, List.of(DeliveryChannel.IN_APP, DeliveryChannel.EMAIL),
                NotificationType.COMMENT, List.of(DeliveryChannel.IN_APP),
                NotificationType.SHARE, List.of(DeliveryChannel.EMAIL)));

        assertEquals("user-1", user.userId());
        assertEquals(List.of(DeliveryChannel.IN_APP, DeliveryChannel.EMAIL), user.channelsFor(NotificationType.MENTION));
        assertEquals(List.of(DeliveryChannel.IN_APP), user.channelsFor(NotificationType.COMMENT));
        assertEquals(List.of(DeliveryChannel.EMAIL), user.channelsFor(NotificationType.SHARE));
    }

    @Test
    void returnsNoChannelsWhenTypeHasNoPreference() {
        User user = new User("user-1", Map.of(
                NotificationType.COMMENT, List.of(DeliveryChannel.IN_APP)));

        assertEquals(List.of(), user.channelsFor(NotificationType.SHARE));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t"})
    void requiresUserId(String userId) {
        assertThrows(IllegalArgumentException.class, () ->
                new User(userId, Map.of(NotificationType.COMMENT, List.of(DeliveryChannel.EMAIL))));
    }

    @Test
    void requiresDeliveryPreferences() {
        assertThrows(NullPointerException.class, () -> new User("user-1", null));
    }

    @Test
    void deliveryPreferencesAreImmutable() {
        List<DeliveryChannel> mentionChannels = new ArrayList<>();
        mentionChannels.add(DeliveryChannel.IN_APP);
        Map<NotificationType, List<DeliveryChannel>> preferences = new EnumMap<>(NotificationType.class);
        preferences.put(NotificationType.MENTION, mentionChannels);

        User user = new User("user-1", preferences);

        mentionChannels.add(DeliveryChannel.EMAIL);
        preferences.put(NotificationType.SHARE, List.of(DeliveryChannel.EMAIL));

        assertEquals(List.of(DeliveryChannel.IN_APP), user.channelsFor(NotificationType.MENTION));
        assertEquals(List.of(), user.channelsFor(NotificationType.SHARE));
        assertThrows(UnsupportedOperationException.class, () ->
                user.deliveryPreferences().put(NotificationType.COMMENT, List.of(DeliveryChannel.IN_APP)));
        assertThrows(UnsupportedOperationException.class, () ->
                user.channelsFor(NotificationType.MENTION).add(DeliveryChannel.EMAIL));
    }
}
