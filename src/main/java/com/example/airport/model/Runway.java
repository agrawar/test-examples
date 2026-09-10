package com.example.airport.model;

import java.util.Objects;
import java.util.Set;

public class Runway {
    private final String id;
    private final RunwayType type;
    private RunwayStatus status;
    private AircraftSize size;

    public Runway(String id, RunwayStatus status, AircraftSize size) {
        this.id = Objects.requireNonNull(id, "id");
        this.type = RunwayType.STANDARD;
        this.status = Objects.requireNonNull(status, "status");
        this.size = Objects.requireNonNull(size, "size");
    }

    public String getId() {
        return id;
    }

    public RunwayType getType() {
        return type;
    }

    public RunwayStatus getStatus() {
        return status;
    }

    public void setStatus(RunwayStatus status) {
        this.status = Objects.requireNonNull(status, "status");
    }

    public AircraftSize getSize() {
        return size;
    }

    public void setSize(AircraftSize size) {
        this.size = Objects.requireNonNull(size, "size");
    }

    public Set<AircraftSize> getSupportedAircraft() {
        return size.compatibleAircraftSizes();
    }

    public boolean supports(Aircraft aircraft) {
        Objects.requireNonNull(aircraft, "aircraft");
        return aircraft.getSize().canUseRunwayOfSize(size);
    }
}
