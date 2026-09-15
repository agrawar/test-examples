package com.example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.example.model.NotificationType;
import com.example.model.User;
import com.example.repository.UserRepository;
import com.example.service.NotificationService;

class NotificationPreferenceTest {

    @Test
    void defaultNotificationPreferencesAreEnabled() {
        User user = new User("John Doe");

        Map<NotificationType, Boolean> preferences = user.getPreferences();

        assertTrue(preferences.get(NotificationType.EMAIL));
        assertTrue(preferences.get(NotificationType.PUSH));
        assertTrue(preferences.get(NotificationType.SMS));
        assertEquals(NotificationType.values().length, preferences.size());
    }

    @Test
    void viewPreferences() {
        // Arrange
        Map<NotificationType, Boolean> preferences = new HashMap<>();
        preferences.put(NotificationType.EMAIL, true);
        preferences.put(NotificationType.PUSH, true);
        preferences.put(NotificationType.SMS, false);
        User user = new User("John Doe", preferences);

        // Act
        Map<NotificationType, Boolean> calculatedPreferences = user.getPreferences();

        // Assert
        assertEquals(calculatedPreferences, preferences);
    }

    @Test
    void updatePreferencesSuccessfullyUpdatesUserPreferences() {
        // Arrange
        Map<NotificationType, Boolean> preferences = new HashMap<>();
        preferences.put(NotificationType.EMAIL, true);
        preferences.put(NotificationType.PUSH, true);
        preferences.put(NotificationType.SMS, false);
        UserRepository userRepository = new UserRepository();
        User user = new User("John Doe", preferences);
        userRepository.save(user);

        Map<NotificationType, Boolean> expectedPreferences = new HashMap<>();
        expectedPreferences.put(NotificationType.EMAIL, false);
        expectedPreferences.put(NotificationType.PUSH, false);
        expectedPreferences.put(NotificationType.SMS, true);

        NotificationService notificationService = new NotificationService(userRepository);

        // Act
        notificationService.updatePreference(user.getUserId(), NotificationType.EMAIL, false);
        notificationService.updatePreference(user.getUserId(), NotificationType.PUSH, false);
        notificationService.updatePreference(user.getUserId(), NotificationType.SMS, true);

        Map<NotificationType, Boolean> updatedPreferences = user.getPreferences();

        // Assert
        assertEquals(expectedPreferences, updatedPreferences);
    }

    @Test
    void getPreferencesAlwaysReturnsAllNotificationTypes() {
        Map<NotificationType, Boolean> incompletePreferences = new HashMap<>();
        incompletePreferences.put(NotificationType.EMAIL, false);
        User userWithPartialPreferences = new User("partial-user", incompletePreferences);
        User userWithDefaults = new User("default-user");

        assertContainsAllNotificationTypes(userWithPartialPreferences.getPreferences());
        assertContainsAllNotificationTypes(userWithDefaults.getPreferences());

        UserRepository userRepository = new UserRepository();
        userRepository.save(userWithPartialPreferences);
        assertContainsAllNotificationTypes(userRepository.getPreferences("partial-user"));
    }

    @Test
    void omittedNotificationTypesDefaultToEnabled() {
        Map<NotificationType, Boolean> incompletePreferences = new HashMap<>();
        incompletePreferences.put(NotificationType.EMAIL, false);
        User user = new User("partial-user", incompletePreferences);

        Map<NotificationType, Boolean> preferences = user.getPreferences();

        assertFalse(preferences.get(NotificationType.EMAIL));
        assertTrue(preferences.get(NotificationType.PUSH));
        assertTrue(preferences.get(NotificationType.SMS));
    }

    @Test
    void updatingOnePreferenceLeavesOthersUnchanged() {
        UserRepository userRepository = new UserRepository();
        User user = new User("John Doe");
        userRepository.save(user);
        NotificationService notificationService = new NotificationService(userRepository);

        notificationService.updatePreference(user.getUserId(), NotificationType.SMS, false);

        Map<NotificationType, Boolean> preferences = user.getPreferences();
        assertTrue(preferences.get(NotificationType.EMAIL));
        assertTrue(preferences.get(NotificationType.PUSH));
        assertFalse(preferences.get(NotificationType.SMS));
    }

    @Test
    void updatingPreferenceToTheSameValueIsIdempotent() {
        UserRepository userRepository = new UserRepository();
        User user = new User("John Doe");
        userRepository.save(user);
        NotificationService notificationService = new NotificationService(userRepository);

        notificationService.updatePreference(user.getUserId(), NotificationType.EMAIL, false);
        notificationService.updatePreference(user.getUserId(), NotificationType.EMAIL, false);

        assertFalse(user.getPreferences().get(NotificationType.EMAIL));
    }

    @Test
    void retrievePreferencesReturnsStoredPreferences() {
        Map<NotificationType, Boolean> preferences = new HashMap<>();
        preferences.put(NotificationType.EMAIL, false);
        preferences.put(NotificationType.PUSH, true);
        preferences.put(NotificationType.SMS, false);
        UserRepository userRepository = new UserRepository();
        User user = new User("John Doe", preferences);
        userRepository.save(user);
        NotificationService notificationService = new NotificationService(userRepository);

        assertEquals(preferences, notificationService.retrievePreferences(user));
        assertEquals(preferences, notificationService.retrievePreferences(user.getUserId()));
    }

    @Test
    void mutatingReturnedPreferencesDoesNotChangeStoredPreferences() {
        User user = new User("John Doe");
        Map<NotificationType, Boolean> returnedPreferences = user.getPreferences();
        returnedPreferences.put(NotificationType.EMAIL, false);

        assertTrue(user.getPreferences().get(NotificationType.EMAIL));
    }

    @Test
    void findByIdThrowsWhenUserDoesNotExist() {
        UserRepository userRepository = new UserRepository();
        String userId = "missing-user";

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userRepository.findById(userId));

        assertEquals("User not found: " + userId, exception.getMessage());
    }

    @Test
    void repositoryThrowsWhenGettingOrUpdatingMissingUser() {
        UserRepository userRepository = new UserRepository();
        String userId = "missing-user";

        IllegalArgumentException getException = assertThrows(
                IllegalArgumentException.class,
                () -> userRepository.getPreferences(userId));
        assertEquals("User not found: " + userId, getException.getMessage());

        IllegalArgumentException updateException = assertThrows(
                IllegalArgumentException.class,
                () -> userRepository.updatePreference(userId, NotificationType.EMAIL, false));
        assertEquals("User not found: " + userId, updateException.getMessage());
    }

    @Test
    void notificationServiceThrowsWhenUserDoesNotExist() {
        NotificationService notificationService = new NotificationService(new UserRepository());
        String userId = "missing-user";

        IllegalArgumentException updateException = assertThrows(
                IllegalArgumentException.class,
                () -> notificationService.updatePreference(userId, NotificationType.EMAIL, false));
        assertEquals("User not found: " + userId, updateException.getMessage());

        IllegalArgumentException retrieveException = assertThrows(
                IllegalArgumentException.class,
                () -> notificationService.retrievePreferences(userId));
        assertEquals("User not found: " + userId, retrieveException.getMessage());
    }

    @Test
    void saveOverwritesExistingUserWithSameId() {
        UserRepository userRepository = new UserRepository();
        userRepository.save(new User("same-id"));

        Map<NotificationType, Boolean> replacementPreferences = new HashMap<>();
        replacementPreferences.put(NotificationType.EMAIL, false);
        replacementPreferences.put(NotificationType.PUSH, false);
        replacementPreferences.put(NotificationType.SMS, false);
        userRepository.save(new User("same-id", replacementPreferences));

        assertEquals(replacementPreferences, userRepository.getPreferences("same-id"));
    }

    @Test
    void constructorRejectsNullPreferences() {
        assertThrows(NullPointerException.class, () -> new User("John Doe", null));
    }

    @Test
    void updatePreferenceRejectsNullNotificationType() {
        UserRepository userRepository = new UserRepository();
        User user = new User("John Doe");
        userRepository.save(user);
        NotificationService notificationService = new NotificationService(userRepository);

        assertThrows(
                NullPointerException.class,
                () -> notificationService.updatePreference(user.getUserId(), null, true));
    }

    private void assertContainsAllNotificationTypes(Map<NotificationType, Boolean> preferences) {
        assertEquals(NotificationType.values().length, preferences.size());
        assertTrue(preferences.keySet().containsAll(EnumSet.allOf(NotificationType.class)));
    }
}
