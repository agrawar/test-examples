package com.example.notifications.model;

import java.util.Objects;

public record NotificationDeliveryStatus(
        DeliveryChannel channel,
        NotificationType type,
        NotificationStatus status
) {
    public NotificationDeliveryStatus {
        Objects.requireNonNull(channel, "channel is required");
        Objects.requireNonNull(type, "type is required");
        Objects.requireNonNull(status, "status is required");
    }
}
