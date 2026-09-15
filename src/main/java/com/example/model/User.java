package com.example.model;

import java.util.EnumMap;
import java.util.Map;

public class User {
    private final String userId;
    private final Map<NotificationType, Boolean> preferences;

    public User(String userId) {
        this.userId = userId;
        this.preferences = new EnumMap<>(NotificationType.class);
        for (NotificationType type : NotificationType.values()) {
            this.preferences.put(type, true);
        }
    }

    public User(String userId, Map<NotificationType, Boolean> preferences) {
        this.userId = userId;
        this.preferences = new EnumMap<>(NotificationType.class);
        for (NotificationType type : NotificationType.values()) {
            this.preferences.put(type, true);
        }
        this.preferences.putAll(preferences);
    }

    public String getUserId() {
        return userId;
    }

    public Map<NotificationType, Boolean> getPreferences() {
        return new EnumMap<>(this.preferences);
    }

    public void updatePreference(NotificationType notificationType, boolean enabled) {
        this.preferences.put(notificationType, enabled);
    }
}
