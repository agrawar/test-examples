package com.example.airport.model;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

public class Aircraft {
    private final String id;
    private AircraftSize size;
    private Priority priority;
    private AircraftStatus status;
    private Set<AircraftRequirement> requirements;

    public Aircraft(
            String id,
            AircraftSize size,
            Priority priority,
            AircraftStatus status,
            Set<AircraftRequirement> requirements) {
        this.id = Objects.requireNonNull(id, "id");
        this.size = Objects.requireNonNull(size, "size");
        this.priority = Objects.requireNonNull(priority, "priority");
        this.status = Objects.requireNonNull(status, "status");
        this.requirements = copyRequirements(requirements);
    }

    public String getId() {
        return id;
    }

    public AircraftSize getSize() {
        return size;
    }

    public void setSize(AircraftSize size) {
        this.size = Objects.requireNonNull(size, "size");
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = Objects.requireNonNull(priority, "priority");
    }

    public AircraftStatus getStatus() {
        return status;
    }

    public void setStatus(AircraftStatus status) {
        this.status = Objects.requireNonNull(status, "status");
    }

    public Set<AircraftRequirement> getRequirements() {
        return copyRequirements(requirements);
    }

    public void setRequirements(Set<AircraftRequirement> requirements) {
        this.requirements = copyRequirements(requirements);
    }

    private static Set<AircraftRequirement> copyRequirements(Set<AircraftRequirement> requirements) {
        if (requirements == null || requirements.isEmpty()) {
            return EnumSet.noneOf(AircraftRequirement.class);
        }
        return EnumSet.copyOf(requirements);
    }
}
