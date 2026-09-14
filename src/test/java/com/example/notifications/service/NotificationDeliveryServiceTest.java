package com.example.notifications.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.example.notifications.model.DeliveryChannel;
import com.example.notifications.model.Notification;
import com.example.notifications.model.NotificationStatus;
import com.example.notifications.model.NotificationType;
import com.example.notifications.model.User;

class NotificationDeliveryServiceTest {

    private static final Instant TIMESTAMP = Instant.parse("2026-09-14T08:00:00Z");
    private static final String USER_ID = "user-1";

    @Test
    void retriesNextChosenChannelWhenDeliveryFails() {
        Fixture fixture = fixture(RecordingSender.failing(DeliveryChannel.IN_APP));
        User user = userWith(NotificationType.MENTION, DeliveryChannel.IN_APP, DeliveryChannel.EMAIL);
        Notification notification = fixture.received(mention());

        List<DeliveryChannel> delivered = fixture.service.deliver(notification, user);

        assertEquals(List.of(DeliveryChannel.IN_APP, DeliveryChannel.EMAIL), fixture.sender.attempted);
        assertEquals(List.of(DeliveryChannel.EMAIL), delivered);
        assertEquals(NotificationStatus.RECEIVED, fixture.statusFor(notification, DeliveryChannel.IN_APP));
        assertEquals(NotificationStatus.DELIVERED, fixture.statusFor(notification, DeliveryChannel.EMAIL));
    }

    @Test
    void doesNotAttemptAChannelThatWasNotChosenForTheNotificationType() {
        Fixture fixture = fixture(RecordingSender.failing(DeliveryChannel.IN_APP));
        User user = new User(USER_ID, Map.of(
                NotificationType.MENTION, List.of(DeliveryChannel.IN_APP),
                NotificationType.SHARE, List.of(DeliveryChannel.EMAIL)));
        Notification notification = fixture.received(mention());

        List<DeliveryChannel> delivered = fixture.service.deliver(notification, user);

        assertEquals(List.of(DeliveryChannel.IN_APP), fixture.sender.attempted);
        assertEquals(List.of(), delivered);
        assertEquals(NotificationStatus.RECEIVED, fixture.statusFor(notification, DeliveryChannel.IN_APP));
        assertEquals(Optional.empty(), fixture.stored(notification).statusFor(DeliveryChannel.EMAIL));
    }

    @Test
    void continuesThroughAllChosenChannelsWhenOneFails() {
        Fixture fixture = fixture(RecordingSender.failing(DeliveryChannel.IN_APP));
        User user = userWith(NotificationType.SHARE, DeliveryChannel.IN_APP, DeliveryChannel.EMAIL);
        Notification notification = fixture.received(share());

        List<DeliveryChannel> delivered = fixture.service.deliver(notification, user);

        assertEquals(List.of(DeliveryChannel.IN_APP, DeliveryChannel.EMAIL), fixture.sender.attempted);
        assertEquals(List.of(DeliveryChannel.EMAIL), delivered);
        assertEquals(NotificationStatus.RECEIVED, fixture.statusFor(notification, DeliveryChannel.IN_APP));
        assertEquals(NotificationStatus.DELIVERED, fixture.statusFor(notification, DeliveryChannel.EMAIL));
    }

    @Test
    void doesNotFallBackToUnselectedChannelEvenWhenItCouldSucceed() {
        Fixture fixture = fixture(RecordingSender.alwaysSucceeds());
        User user = userWith(NotificationType.COMMENT, DeliveryChannel.EMAIL);
        Notification notification = fixture.received(comment());

        List<DeliveryChannel> delivered = fixture.service.deliver(notification, user);

        assertEquals(List.of(DeliveryChannel.EMAIL), fixture.sender.attempted);
        assertEquals(List.of(DeliveryChannel.EMAIL), delivered);
        assertEquals(NotificationStatus.DELIVERED, fixture.statusFor(notification, DeliveryChannel.EMAIL));
        assertEquals(Optional.empty(), fixture.stored(notification).statusFor(DeliveryChannel.IN_APP));
    }

    @Test
    void deliversToEveryChosenChannelWhenAllSucceed() {
        Fixture fixture = fixture(RecordingSender.alwaysSucceeds());
        User user = userWith(NotificationType.SHARE, DeliveryChannel.IN_APP, DeliveryChannel.EMAIL);
        Notification notification = fixture.received(share());

        List<DeliveryChannel> delivered = fixture.service.deliver(notification, user);

        assertEquals(List.of(DeliveryChannel.IN_APP, DeliveryChannel.EMAIL), fixture.sender.attempted);
        assertEquals(List.of(DeliveryChannel.IN_APP, DeliveryChannel.EMAIL), delivered);
        assertEquals(NotificationStatus.DELIVERED, fixture.statusFor(notification, DeliveryChannel.IN_APP));
        assertEquals(NotificationStatus.DELIVERED, fixture.statusFor(notification, DeliveryChannel.EMAIL));
    }

    @Test
    void marksChosenChannelDeliveredAfterASuccessfulSend() {
        Fixture fixture = fixture(RecordingSender.alwaysSucceeds());
        Notification notification = fixture.received(mention());

        fixture.service.deliver(notification, userWith(NotificationType.MENTION, DeliveryChannel.IN_APP));

        assertEquals(NotificationStatus.DELIVERED, fixture.statusFor(notification, DeliveryChannel.IN_APP));
    }

    @Test
    void keepsChosenChannelReceivedWhenSendFails() {
        Fixture fixture = fixture(RecordingSender.failing(DeliveryChannel.IN_APP));
        Notification notification = fixture.received(mention());

        fixture.service.deliver(notification, userWith(NotificationType.MENTION, DeliveryChannel.IN_APP));

        assertEquals(NotificationStatus.RECEIVED, fixture.statusFor(notification, DeliveryChannel.IN_APP));
    }

    private static Fixture fixture(RecordingSender sender) {
        return new Fixture(sender);
    }

    private static User userWith(NotificationType type, DeliveryChannel... channels) {
        return new User(USER_ID, Map.of(type, List.of(channels)));
    }

    private static Notification mention() {
        return new Notification("mention-1", USER_ID, "Alex mentioned you", NotificationType.MENTION, TIMESTAMP);
    }

    private static Notification share() {
        return new Notification("share-1", USER_ID, "Alex shared a doc", NotificationType.SHARE, TIMESTAMP);
    }

    private static Notification comment() {
        return new Notification("comment-1", USER_ID, "Alex commented", NotificationType.COMMENT, TIMESTAMP);
    }

    private static final class Fixture {
        private final InMemoryNotificationRepository repository = new InMemoryNotificationRepository();
        private final RecordingSender sender;
        private final NotificationDeliveryService service;

        private Fixture(RecordingSender sender) {
            this.sender = sender;
            this.service = new NotificationDeliveryService(
                    new NotificationChannelSelector(), sender, repository);
        }

        private Notification received(Notification notification) {
            Notification received = notification.withChannelStatuses(List.of());
            repository.save(received);
            return received;
        }

        private Notification stored(Notification notification) {
            return repository.findById(notification.id()).orElseThrow();
        }

        private NotificationStatus statusFor(Notification notification, DeliveryChannel channel) {
            return stored(notification).statusFor(channel).orElseThrow();
        }
    }

    private static final class RecordingSender implements NotificationSender {
        private final Set<DeliveryChannel> failingChannels;
        private final List<DeliveryChannel> attempted = new ArrayList<>();

        private RecordingSender(Set<DeliveryChannel> failingChannels) {
            this.failingChannels = failingChannels;
        }

        static RecordingSender failing(DeliveryChannel... channels) {
            return new RecordingSender(EnumSet.copyOf(List.of(channels)));
        }

        static RecordingSender alwaysSucceeds() {
            return new RecordingSender(EnumSet.noneOf(DeliveryChannel.class));
        }

        @Override
        public boolean send(Notification notification, DeliveryChannel channel) {
            attempted.add(channel);
            return !failingChannels.contains(channel);
        }
    }
}
