package com.example.delivery.model;

import java.util.Objects;

public class DeliveryDriver {
    private final String id;
    private Location location;
    private DriverAvailability availability;

    public DeliveryDriver(String id, Location location) {
        this(id, location, DriverAvailability.AVAILABLE);
    }

    public DeliveryDriver(String id, Location location, DriverAvailability availability) {
        this.id = Objects.requireNonNull(id, "id");
        this.location = Objects.requireNonNull(location, "location");
        this.availability = Objects.requireNonNull(availability, "availability");
    }

    public String getId() {
        return id;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = Objects.requireNonNull(location, "location");
    }

    public DriverAvailability getAvailability() {
        return availability;
    }

    public void setAvailability(DriverAvailability availability) {
        this.availability = Objects.requireNonNull(availability, "availability");
    }

    public boolean isAvailable() {
        return availability == DriverAvailability.AVAILABLE;
    }

    public void markAvailable() {
        this.availability = DriverAvailability.AVAILABLE;
    }

    public void markBusy() {
        this.availability = DriverAvailability.BUSY;
    }
}
