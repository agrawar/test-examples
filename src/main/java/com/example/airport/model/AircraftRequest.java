package com.example.airport.model;

import java.time.Instant;
import java.util.Objects;

public class AircraftRequest {
    private Aircraft aircraft;
    private Instant requestTime;
    private Priority priority;

    public AircraftRequest(Aircraft aircraft, Instant requestTime, Priority priority) {
        this.aircraft = Objects.requireNonNull(aircraft, "aircraft");
        this.requestTime = Objects.requireNonNull(requestTime, "requestTime");
        this.priority = Objects.requireNonNull(priority, "priority");
    }

    public Aircraft getAircraft() {
        return aircraft;
    }

    public void setAircraft(Aircraft aircraft) {
        this.aircraft = Objects.requireNonNull(aircraft, "aircraft");
    }

    public Instant getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(Instant requestTime) {
        this.requestTime = Objects.requireNonNull(requestTime, "requestTime");
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = Objects.requireNonNull(priority, "priority");
    }
}
