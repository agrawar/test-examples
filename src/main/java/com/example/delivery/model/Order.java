package com.example.delivery.model;

import java.time.Instant;
import java.util.Objects;

public class Order {
    private final String id;
    private Instant orderTime;
    private Location restaurantLocation;
    private Priority priority;

    public Order(String id, Instant orderTime, Location restaurantLocation, Priority priority) {
        this.id = Objects.requireNonNull(id, "id");
        this.orderTime = Objects.requireNonNull(orderTime, "orderTime");
        this.restaurantLocation = Objects.requireNonNull(restaurantLocation, "restaurantLocation");
        this.priority = Objects.requireNonNull(priority, "priority");
    }

    public String getId() {
        return id;
    }

    public Instant getOrderTime() {
        return orderTime;
    }

    public void setOrderTime(Instant orderTime) {
        this.orderTime = Objects.requireNonNull(orderTime, "orderTime");
    }

    public Location getRestaurantLocation() {
        return restaurantLocation;
    }

    public void setRestaurantLocation(Location restaurantLocation) {
        this.restaurantLocation = Objects.requireNonNull(restaurantLocation, "restaurantLocation");
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = Objects.requireNonNull(priority, "priority");
    }
}
