package com.example.delivery.service;

import com.example.delivery.model.DeliveryAssignment;
import com.example.delivery.model.DeliveryDriver;
import com.example.delivery.model.Location;
import com.example.delivery.model.Order;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.PriorityQueue;

public class OrderAssignmentService {

    static final double AVERAGE_SPEED_KMH = 30.0;
    static final int RADIUS_STEP_MINUTES = 5;
    static final int MAX_RADIUS_MINUTES = 120;
    private static final double GRID_CELL_SIZE_KM = 1.0;

    /**
     * Assigns drivers to orders using the path from the driver to the restaurant
     * ({@code order.getRestaurantLocation()}).
     *
     * <p>Orders are taken from a priority queue that surfaces priority 1 first, then 2,
     * then 3. When several orders share a priority, the earliest one in the input list
     * is assigned first. Each order is given the closest available driver to its
     * restaurant, searching first in a 5-minute radius and expanding by 5 minutes
     * until a candidate is found. That driver is marked busy.
     */
    public List<DeliveryAssignment> assignDrivers(List<Order> orders, List<DeliveryDriver> drivers) {
        Objects.requireNonNull(orders, "orders");
        Objects.requireNonNull(drivers, "drivers");

        PriorityQueue<IndexedOrder> queue = new PriorityQueue<>(
                Comparator.comparingInt((IndexedOrder io) -> io.order.getPriority().getLevel())
                        .thenComparingInt(io -> io.index));
        for (int i = 0; i < orders.size(); i++) {
            queue.add(new IndexedOrder(orders.get(i), i));
        }

        DriverGeoIndex index = new DriverGeoIndex(drivers, GRID_CELL_SIZE_KM);
        List<DeliveryAssignment> assignments = new ArrayList<>();
        while (!queue.isEmpty()) {
            Order order = queue.poll().order;
            DeliveryAssignment assignment = assignDriver(order, index);
            if (assignment != null) {
                assignments.add(assignment);
            }
        }
        return assignments;
    }

    /**
     * Assigns a driver to a single order by searching available drivers in expanding
     * travel-time radii (5 minutes, then 10, and so on) and marking the closest match busy.
     */
    public DeliveryAssignment assignDriver(Order order, List<DeliveryDriver> drivers) {
        Objects.requireNonNull(drivers, "drivers");
        return assignDriver(order, new DriverGeoIndex(drivers, GRID_CELL_SIZE_KM));
    }

    private DeliveryAssignment assignDriver(Order order, DriverGeoIndex index) {
        Objects.requireNonNull(order, "order");
        Location restaurant = order.getRestaurantLocation();

        for (int minutes = RADIUS_STEP_MINUTES; minutes <= MAX_RADIUS_MINUTES; minutes += RADIUS_STEP_MINUTES) {
            DeliveryDriver closest = closestAvailable(index.withinRadius(restaurant, radiusKm(minutes)), restaurant);
            if (closest != null) {
                closest.markBusy();
                return new DeliveryAssignment(order, closest);
            }
        }

        DeliveryDriver fallback = closestAvailable(index.allDrivers(), restaurant);
        if (fallback == null) {
            return null;
        }
        fallback.markBusy();
        return new DeliveryAssignment(order, fallback);
    }

    static double radiusKm(int minutes) {
        return AVERAGE_SPEED_KMH * minutes / 60.0;
    }

    private static DeliveryDriver closestAvailable(List<DeliveryDriver> candidates, Location restaurant) {
        DeliveryDriver closest = null;
        double closestDistance = Double.POSITIVE_INFINITY;
        for (DeliveryDriver driver : candidates) {
            if (!driver.isAvailable()) {
                continue;
            }
            double distance = driver.getLocation().distanceTo(restaurant);
            if (distance < closestDistance) {
                closestDistance = distance;
                closest = driver;
            }
        }
        return closest;
    }

    private record IndexedOrder(Order order, int index) {}
}
