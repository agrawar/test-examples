Requirements - Notification System

1. Accept request and deliver to user 
2, Users can receive a notification - comments, mentions, shares
3. Determine which channels a notification should go through based on user preferences
4. If delivery fals through one channel - another channel should be attempted 


Considerations
1. de-duplication

Out of scope
1. ordering


DONE

Domain models
1. Notification - Recipient (required), message (required), notification type (must be a supported type), timestamp (required)
2. Delivery channels - enum of in-app, email
3. User - userid, preferences of delivery 