package com.example.notifications.model;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public record User(String userId, Map<NotificationType, List<DeliveryChannel>> deliveryPreferences) {
    public User {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId is required");
        }
        Objects.requireNonNull(deliveryPreferences, "deliveryPreferences is required");
        Map<NotificationType, List<DeliveryChannel>> copy = new EnumMap<>(NotificationType.class);
        deliveryPreferences.forEach((type, channels) -> {
            Objects.requireNonNull(type, "notification type is required");
            Objects.requireNonNull(channels, "channels are required");
            copy.put(type, List.copyOf(channels));
        });
        deliveryPreferences = Map.copyOf(copy);
    }

    public List<DeliveryChannel> channelsFor(NotificationType type) {
        Objects.requireNonNull(type, "notification type is required");
        return deliveryPreferences.getOrDefault(type, List.of());
    }
}
