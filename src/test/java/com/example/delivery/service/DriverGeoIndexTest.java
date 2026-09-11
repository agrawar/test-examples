package com.example.delivery.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.delivery.model.DeliveryDriver;
import com.example.delivery.model.Location;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class DriverGeoIndexTest {

    private static final Location RESTAURANT = new Location(40.0, -74.0);

    @Test
    void returnsOnlyDriversInsideTheRequestedRadius() {
        DeliveryDriver inside = new DeliveryDriver("inside", offsetKm(RESTAURANT, 1.0, 0.0));
        DeliveryDriver outside = new DeliveryDriver("outside", offsetKm(RESTAURANT, 4.0, 0.0));
        DriverGeoIndex index = new DriverGeoIndex(List.of(inside, outside), 1.0);

        List<DeliveryDriver> withinFiveMinutes =
                index.withinRadius(RESTAURANT, OrderAssignmentService.radiusKm(5));

        assertEquals(Set.of("inside"), driverIds(withinFiveMinutes));
    }

    @Test
    void includesADriverExactlyOnTheRadiusBoundary() {
        double fiveMinuteKm = OrderAssignmentService.radiusKm(5);
        DeliveryDriver onBoundary =
                new DeliveryDriver("on-boundary", offsetKm(RESTAURANT, fiveMinuteKm, 0.0));
        DriverGeoIndex index = new DriverGeoIndex(List.of(onBoundary), 1.0);

        List<DeliveryDriver> matches = index.withinRadius(RESTAURANT, fiveMinuteKm);

        assertEquals(1, matches.size());
        assertEquals("on-boundary", matches.get(0).getId());
        assertTrue(RESTAURANT.distanceTo(onBoundary.getLocation()) <= fiveMinuteKm + 0.02);
    }

    @Test
    void findsDriversEastOfTheRestaurantUsingLongitude() {
        DeliveryDriver east = new DeliveryDriver("east", offsetKm(RESTAURANT, 0.0, 1.0));
        DeliveryDriver tooFarEast = new DeliveryDriver("too-far-east", offsetKm(RESTAURANT, 0.0, 4.0));
        DriverGeoIndex index = new DriverGeoIndex(List.of(east, tooFarEast), 1.0);

        List<DeliveryDriver> withinFiveMinutes =
                index.withinRadius(RESTAURANT, OrderAssignmentService.radiusKm(5));

        assertEquals(Set.of("east"), driverIds(withinFiveMinutes));
    }

    @Test
    void allDriversReturnsEveryIndexedDriver() {
        DeliveryDriver a = new DeliveryDriver("a", offsetKm(RESTAURANT, 1.0, 0.0));
        DeliveryDriver b = new DeliveryDriver("b", offsetKm(RESTAURANT, 80.0, 0.0));
        DriverGeoIndex index = new DriverGeoIndex(List.of(a, b), 1.0);

        assertEquals(Set.of("a", "b"), driverIds(index.allDrivers()));
    }

    @Test
    void rejectsNonPositiveCellSize() {
        DeliveryDriver driver = new DeliveryDriver("d", RESTAURANT);
        assertThrows(IllegalArgumentException.class, () -> new DriverGeoIndex(List.of(driver), 0.0));
        assertThrows(IllegalArgumentException.class, () -> new DriverGeoIndex(List.of(driver), -1.0));
    }

    @Test
    void rejectsNegativeRadius() {
        DriverGeoIndex index = new DriverGeoIndex(List.of(new DeliveryDriver("d", RESTAURANT)), 1.0);
        assertThrows(IllegalArgumentException.class, () -> index.withinRadius(RESTAURANT, -0.1));
    }

    private static Set<String> driverIds(List<DeliveryDriver> drivers) {
        return drivers.stream().map(DeliveryDriver::getId).collect(Collectors.toSet());
    }

    private static Location offsetKm(Location from, double northKm, double eastKm) {
        double dLat = northKm / 111.32;
        double cosLat = Math.cos(Math.toRadians(from.getLatitude()));
        double dLon = eastKm / (111.32 * Math.max(Math.abs(cosLat), 0.01));
        return new Location(from.getLatitude() + dLat, from.getLongitude() + dLon);
    }
}
