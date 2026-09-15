# hello-world

Build a notification preference service.

Requirements
1. Users can view preferences
2. Users can enable or disable notification types
3. Users can update preferences 
3. Users can choose if a notification should be sent 
 
Models
1. Notification types - Email, Push, SMS
2. User - userid, Map<NotificationType, boolean> preferences

Services
1. NotificationService - updatePreference(userid, notificationtype, boolean enabled), retreivepreferences (call user model to retrieve)

Assumption
1. User is authenticated

Out of scope
1. Ordering is out of scope