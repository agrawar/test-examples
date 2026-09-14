package com.example.notifications.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record Notification(
        String id,
        String recipient,
        String message,
        NotificationType type,
        Instant timestamp,
        List<NotificationDeliveryStatus> channelStatuses
) {
    public Notification {
        required(id, "id");
        required(recipient, "recipient");
        required(message, "message");
        required(type, "type");
        required(timestamp, "timestamp");
        Objects.requireNonNull(channelStatuses, "channelStatuses is required");
        channelStatuses = List.copyOf(channelStatuses);
        for (NotificationDeliveryStatus channelStatus : channelStatuses) {
            if (channelStatus.type() != type) {
                throw new IllegalArgumentException("channel status type must match notification type");
            }
        }
    }

    public Notification(
            String id,
            String recipient,
            String message,
            NotificationType type,
            Instant timestamp
    ) {
        this(id, recipient, message, type, timestamp, List.of());
    }

    public Notification withChannelStatuses(List<NotificationDeliveryStatus> channelStatuses) {
        return new Notification(id, recipient, message, type, timestamp, channelStatuses);
    }

    public Notification withChannelStatus(DeliveryChannel channel, NotificationStatus status) {
        List<NotificationDeliveryStatus> updated = new ArrayList<>();
        boolean replaced = false;
        for (NotificationDeliveryStatus existing : channelStatuses) {
            if (existing.channel() == channel) {
                updated.add(new NotificationDeliveryStatus(channel, type, status));
                replaced = true;
            } else {
                updated.add(existing);
            }
        }
        if (!replaced) {
            updated.add(new NotificationDeliveryStatus(channel, type, status));
        }
        return withChannelStatuses(updated);
    }

    public Optional<NotificationStatus> statusFor(DeliveryChannel channel) {
        return channelStatuses.stream()
                .filter(channelStatus -> channelStatus.channel() == channel)
                .map(NotificationDeliveryStatus::status)
                .findFirst();
    }

    private static void required(Object value, String field) {
        if (value == null || (value instanceof String text && text.isBlank())) {
            throw new IllegalArgumentException(field + " is required");
        }
    }
}
