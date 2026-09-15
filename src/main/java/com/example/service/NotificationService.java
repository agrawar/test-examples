package com.example.service;

import java.util.Map;

import com.example.model.NotificationType;
import com.example.model.User;
import com.example.repository.UserRepository;

public class NotificationService {
    private final UserRepository userRepository;

    public NotificationService() {
        this(new UserRepository());
    }

    public NotificationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void updatePreference(String userId, NotificationType notificationType, boolean enabled) {
        userRepository.updatePreference(userId, notificationType, enabled);
    }

    public Map<NotificationType, Boolean> retrievePreferences(User user) {
        return userRepository.getPreferences(user.getUserId());
    }

    public Map<NotificationType, Boolean> retrievePreferences(String userId) {
        return userRepository.getPreferences(userId);
    }
}
