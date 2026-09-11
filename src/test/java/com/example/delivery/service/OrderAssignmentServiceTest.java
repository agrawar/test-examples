package com.example.delivery.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.example.delivery.model.DeliveryAssignment;
import com.example.delivery.model.DeliveryDriver;
import com.example.delivery.model.DriverAvailability;
import com.example.delivery.model.Location;
import com.example.delivery.model.Order;
import com.example.delivery.model.Priority;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OrderAssignmentServiceTest {

    private OrderAssignmentService service;
    private Instant now;

    @BeforeEach
    void setUp() {
        service = new OrderAssignmentService();
        now = Instant.parse("2026-09-11T11:00:00Z");
    }

    @Test
    void assignsDriverClosestToRestaurant() {
        Location restaurant = new Location(0.0, 0.0);
        Order order = order("order-1", restaurant, Priority.PRIORITY_2);

        DeliveryDriver farDriver = new DeliveryDriver("far", new Location(10.0, 10.0));
        DeliveryDriver closeDriver = new DeliveryDriver("close", new Location(1.0, 1.0));

        DeliveryAssignment assignment = service.assignDriver(order, List.of(farDriver, closeDriver));

        assertNotNull(assignment);
        assertEquals("close", assignment.getDriver().getId());
        assertEquals("order-1", assignment.getOrder().getId());
    }

    @Test
    void assignsOnlyAvailableDriversToAnOrder() {
        Location restaurant = new Location(0.0, 0.0);
        Order order = order("order-1", restaurant, Priority.PRIORITY_1);

        DeliveryDriver busyCloser = new DeliveryDriver(
                "busy-closer", new Location(0.1, 0.1), DriverAvailability.BUSY);
        DeliveryDriver availableFarther = new DeliveryDriver(
                "available-farther", new Location(5.0, 5.0), DriverAvailability.AVAILABLE);

        DeliveryAssignment assignment =
                service.assignDriver(order, List.of(busyCloser, availableFarther));

        assertNotNull(assignment);
        assertEquals("available-farther", assignment.getDriver().getId());
        assertFalse(assignment.getDriver().isAvailable());
        assertEquals(DriverAvailability.BUSY, assignment.getDriver().getAvailability());
    }

    @Test
    void markAvailableLetsABusyDriverTakeAnotherOrder() {
        Order first = order("order-1", new Location(0.0, 0.0), Priority.PRIORITY_1);
        Order second = order("order-2", new Location(0.0, 0.0), Priority.PRIORITY_1);
        DeliveryDriver driver = new DeliveryDriver("driver-1", new Location(0.0, 0.0));

        assertNotNull(service.assignDriver(first, List.of(driver)));
        assertNull(service.assignDriver(second, List.of(driver)));

        driver.markAvailable();
        DeliveryAssignment next = service.assignDriver(second, List.of(driver));
        assertNotNull(next);
        assertEquals("order-2", next.getOrder().getId());
        assertEquals(DriverAvailability.BUSY, driver.getAvailability());
    }

    @Test
    void returnsNullWhenNoDriverIsAvailable() {
        Order order = order("order-1", new Location(0.0, 0.0), Priority.PRIORITY_1);
        DeliveryDriver busy = new DeliveryDriver("busy", new Location(0.0, 0.0), DriverAvailability.BUSY);

        assertNull(service.assignDriver(order, List.of(busy)));
    }

    @Test
    void priorityQueuePicksFirstPriority1OrderOverPriority2And3() {
        Order firstPriority1 = order("p1-first", new Location(0.0, 0.0), Priority.PRIORITY_1);
        Order laterPriority1 = order("p1-later", new Location(1.0, 0.0), Priority.PRIORITY_1);
        Order priority2 = order("p2", new Location(2.0, 0.0), Priority.PRIORITY_2);
        Order priority3 = order("p3", new Location(3.0, 0.0), Priority.PRIORITY_3);

        DeliveryDriver driverA = new DeliveryDriver("driver-a", new Location(0.0, 0.0));
        DeliveryDriver driverB = new DeliveryDriver("driver-b", new Location(1.0, 0.0));
        DeliveryDriver driverC = new DeliveryDriver("driver-c", new Location(2.0, 0.0));
        DeliveryDriver driverD = new DeliveryDriver("driver-d", new Location(3.0, 0.0));

        List<DeliveryAssignment> assignments = service.assignDrivers(
                List.of(priority3, firstPriority1, priority2, laterPriority1),
                List.of(driverA, driverB, driverC, driverD));

        assertEquals(4, assignments.size());
        assertEquals("p1-first", assignments.get(0).getOrder().getId());
        assertEquals("p1-later", assignments.get(1).getOrder().getId());
        assertEquals("p2", assignments.get(2).getOrder().getId());
        assertEquals("p3", assignments.get(3).getOrder().getId());
    }

    @Test
    void highestPriorityOrdersAreChosenBeforeLowerPriorityWhenDriversAreLimited() {
        Order priority3 = order("p3", new Location(0.0, 0.0), Priority.PRIORITY_3);
        Order priority1 = order("p1", new Location(10.0, 0.0), Priority.PRIORITY_1);
        Order priority2 = order("p2", new Location(20.0, 0.0), Priority.PRIORITY_2);

        DeliveryDriver onlyDriver = new DeliveryDriver("only", new Location(0.0, 0.0));

        List<DeliveryAssignment> assignments =
                service.assignDrivers(List.of(priority3, priority2, priority1), List.of(onlyDriver));

        assertEquals(1, assignments.size());
        assertEquals("p1", assignments.get(0).getOrder().getId());
        assertEquals("only", assignments.get(0).getDriver().getId());
    }

    @Test
    void prefersDriverCloserInKilometersEvenWhenDegreeDeltasLookLarger() {
        Location restaurant = new Location(70.0, 0.0);
        Order order = order("order-1", restaurant, Priority.PRIORITY_1);

        DeliveryDriver fartherNorth = new DeliveryDriver("farther-north", new Location(75.0, 0.0));
        DeliveryDriver closerEast = new DeliveryDriver("closer-east", new Location(70.0, 5.0));

        DeliveryAssignment assignment =
                service.assignDriver(order, List.of(fartherNorth, closerEast));

        assertNotNull(assignment);
        assertEquals("closer-east", assignment.getDriver().getId());
    }

    @Test
    void prefersDriverInsideFiveMinuteRadiusOverFartherDriver() {
        Location restaurant = new Location(40.0, -74.0);
        Order order = order("order-1", restaurant, Priority.PRIORITY_1);

        DeliveryDriver withinFiveMinutes =
                new DeliveryDriver("within-5", offsetKm(restaurant, 1.0, 0.0));
        DeliveryDriver withinTenMinutes =
                new DeliveryDriver("within-10", offsetKm(restaurant, 4.0, 0.0));

        DeliveryAssignment assignment =
                service.assignDriver(order, List.of(withinTenMinutes, withinFiveMinutes));

        assertNotNull(assignment);
        assertEquals("within-5", assignment.getDriver().getId());
    }

    @Test
    void expandsSearchToTenMinutesWhenNobodyIsWithinFive() {
        Location restaurant = new Location(40.0, -74.0);
        Order order = order("order-1", restaurant, Priority.PRIORITY_1);

        DeliveryDriver withinTenMinutes =
                new DeliveryDriver("within-10", offsetKm(restaurant, 4.0, 0.0));
        DeliveryDriver farther =
                new DeliveryDriver("farther", offsetKm(restaurant, 8.0, 0.0));

        DeliveryAssignment assignment =
                service.assignDriver(order, List.of(farther, withinTenMinutes));

        assertNotNull(assignment);
        assertEquals("within-10", assignment.getDriver().getId());
    }

    @Test
    void skipsBusyDriverInInnerRadiusAndExpandsToNextAvailable() {
        Location restaurant = new Location(40.0, -74.0);
        Order order = order("order-1", restaurant, Priority.PRIORITY_1);

        DeliveryDriver busyCloser = new DeliveryDriver(
                "busy-closer",
                offsetKm(restaurant, 1.0, 0.0),
                DriverAvailability.BUSY);
        DeliveryDriver availableFarther =
                new DeliveryDriver("available-farther", offsetKm(restaurant, 4.0, 0.0));

        DeliveryAssignment assignment =
                service.assignDriver(order, List.of(busyCloser, availableFarther));

        assertNotNull(assignment);
        assertEquals("available-farther", assignment.getDriver().getId());
    }

    @Test
    void findsNearbyDriverWithoutNeedingEveryFarAwayDriverToWin() {
        Location restaurant = new Location(40.0, -74.0);
        Order order = order("order-1", restaurant, Priority.PRIORITY_1);

        List<DeliveryDriver> drivers = new ArrayList<>();
        Location far = offsetKm(restaurant, 80.0, 0.0);
        for (int i = 0; i < 1_000; i++) {
            drivers.add(new DeliveryDriver("far-" + i, far));
        }
        drivers.add(new DeliveryDriver("nearby", offsetKm(restaurant, 1.0, 0.0)));

        DeliveryAssignment assignment = service.assignDriver(order, drivers);

        assertNotNull(assignment);
        assertEquals("nearby", assignment.getDriver().getId());
    }

    @Test
    void expandsToFifteenMinutesWhenCloserRingsAreEmpty() {
        Location restaurant = new Location(40.0, -74.0);
        Order order = order("order-1", restaurant, Priority.PRIORITY_1);

        DeliveryDriver withinFifteen =
                new DeliveryDriver("within-15", offsetKm(restaurant, 6.0, 0.0));
        DeliveryDriver farther =
                new DeliveryDriver("farther", offsetKm(restaurant, 12.0, 0.0));

        DeliveryAssignment assignment =
                service.assignDriver(order, List.of(farther, withinFifteen));

        assertNotNull(assignment);
        assertEquals("within-15", assignment.getDriver().getId());
    }

    @Test
    void picksClosestDriverAmongSeveralInTheSameTimeRing() {
        Location restaurant = new Location(40.0, -74.0);
        Order order = order("order-1", restaurant, Priority.PRIORITY_1);

        DeliveryDriver closerInTen =
                new DeliveryDriver("closer-in-10", offsetKm(restaurant, 3.2, 0.0));
        DeliveryDriver fartherInTen =
                new DeliveryDriver("farther-in-10", offsetKm(restaurant, 4.5, 0.0));

        DeliveryAssignment assignment =
                service.assignDriver(order, List.of(fartherInTen, closerInTen));

        assertNotNull(assignment);
        assertEquals("closer-in-10", assignment.getDriver().getId());
    }

    @Test
    void fallsBackToDriversBeyondTwoHourRadius() {
        Location restaurant = new Location(40.0, -74.0);
        Order order = order("order-1", restaurant, Priority.PRIORITY_1);

        DeliveryDriver beyondTwoHours =
                new DeliveryDriver("beyond-2h", offsetKm(restaurant, 70.0, 0.0));

        DeliveryAssignment assignment = service.assignDriver(order, List.of(beyondTwoHours));

        assertNotNull(assignment);
        assertEquals("beyond-2h", assignment.getDriver().getId());
        assertEquals(DriverAvailability.BUSY, beyondTwoHours.getAvailability());
    }

    @Test
    void secondOrderExpandsAfterNearbyDriverIsMarkedBusy() {
        Location restaurant = new Location(40.0, -74.0);
        Order first = order("order-1", restaurant, Priority.PRIORITY_1);
        Order second = order("order-2", restaurant, Priority.PRIORITY_1);

        DeliveryDriver withinFive =
                new DeliveryDriver("within-5", offsetKm(restaurant, 1.0, 0.0));
        DeliveryDriver withinTen =
                new DeliveryDriver("within-10", offsetKm(restaurant, 4.0, 0.0));

        List<DeliveryAssignment> assignments =
                service.assignDrivers(List.of(first, second), List.of(withinFive, withinTen));

        assertEquals(2, assignments.size());
        assertEquals("within-5", assignments.get(0).getDriver().getId());
        assertEquals("within-10", assignments.get(1).getDriver().getId());
    }

    @Test
    void assignsDriverAtTheRestaurantInTheFirstFiveMinuteRing() {
        Location restaurant = new Location(40.0, -74.0);
        Order order = order("order-1", restaurant, Priority.PRIORITY_1);
        DeliveryDriver atRestaurant = new DeliveryDriver("at-restaurant", restaurant);
        DeliveryDriver farther = new DeliveryDriver("farther", offsetKm(restaurant, 4.0, 0.0));

        DeliveryAssignment assignment =
                service.assignDriver(order, List.of(farther, atRestaurant));

        assertNotNull(assignment);
        assertEquals("at-restaurant", assignment.getDriver().getId());
    }

    @Test
    void returnsNullWhenDriverListIsEmpty() {
        Order order = order("order-1", new Location(40.0, -74.0), Priority.PRIORITY_1);
        assertNull(service.assignDriver(order, List.of()));
    }

    @Test
    void fiveAndTenMinuteRadiiMatchAssumedDrivingSpeed() {
        assertEquals(2.5, OrderAssignmentService.radiusKm(5));
        assertEquals(5.0, OrderAssignmentService.radiusKm(10));
        assertEquals(7.5, OrderAssignmentService.radiusKm(15));
        assertEquals(60.0, OrderAssignmentService.radiusKm(OrderAssignmentService.MAX_RADIUS_MINUTES));
    }

    private Order order(String id, Location restaurant, Priority priority) {
        return new Order(id, now, restaurant, priority);
    }

    private static Location offsetKm(Location from, double northKm, double eastKm) {
        double dLat = northKm / 111.32;
        double cosLat = Math.cos(Math.toRadians(from.getLatitude()));
        double dLon = eastKm / (111.32 * Math.max(Math.abs(cosLat), 0.01));
        return new Location(from.getLatitude() + dLat, from.getLongitude() + dLon);
    }
}
