package com.example.airport.model;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

public enum AircraftSize {
    SMALL,
    MEDIUM,
    LARGE;

    public boolean canUseRunwayOfSize(AircraftSize runwaySize) {
        Objects.requireNonNull(runwaySize, "runwaySize");
        return switch (this) {
            case SMALL -> true;
            case MEDIUM -> runwaySize != SMALL;
            case LARGE -> runwaySize == LARGE;
        };
    }

    public Set<AircraftSize> compatibleAircraftSizes() {
        return switch (this) {
            case SMALL -> EnumSet.of(SMALL);
            case MEDIUM -> EnumSet.of(SMALL, MEDIUM);
            case LARGE -> EnumSet.of(SMALL, MEDIUM, LARGE);
        };
    }
}
