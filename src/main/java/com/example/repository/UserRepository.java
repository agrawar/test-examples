package com.example.repository;

import java.util.HashMap;
import java.util.Map;

import com.example.model.NotificationType;
import com.example.model.User;

public class UserRepository {
    private final Map<String, User> users = new HashMap<>();

    public void save(User user) {
        users.put(user.getUserId(), user);
    }

    public User findById(String userId) {
        User user = users.get(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found: " + userId);
        }
        return user;
    }

    public Map<NotificationType, Boolean> getPreferences(String userId) {
        return findById(userId).getPreferences();
    }

    public void updatePreference(String userId, NotificationType notificationType, boolean enabled) {
        findById(userId).updatePreference(notificationType, enabled);
    }
}
