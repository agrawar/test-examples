package com.example.airport.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.EnumSet;

import org.junit.jupiter.api.Test;

class AirportModelsTest {

    @Test
    void aircraftStoresIdSizePriorityStatusAndRequirements() {
        Aircraft aircraft = new Aircraft(
                "AA101",
                AircraftSize.LARGE,
                Priority.PRIORITY_1,
                AircraftStatus.LANDING,
                EnumSet.of(AircraftRequirement.LONG_RUNWAY, AircraftRequirement.INSTRUMENT_LANDING));

        assertEquals("AA101", aircraft.getId());
        assertEquals(AircraftSize.LARGE, aircraft.getSize());
        assertEquals(Priority.PRIORITY_1, aircraft.getPriority());
        assertEquals(AircraftStatus.LANDING, aircraft.getStatus());
        assertEquals(
                EnumSet.of(AircraftRequirement.LONG_RUNWAY, AircraftRequirement.INSTRUMENT_LANDING),
                aircraft.getRequirements());
    }

    @Test
    void runwayStoresIdTypeStatusAndSize() {
        Runway runway = new Runway("RWY-09", RunwayStatus.AVAILABLE, AircraftSize.LARGE);

        assertEquals("RWY-09", runway.getId());
        assertEquals(RunwayType.STANDARD, runway.getType());
        assertEquals(RunwayStatus.AVAILABLE, runway.getStatus());
        assertEquals(AircraftSize.LARGE, runway.getSize());
        assertEquals(
                EnumSet.of(AircraftSize.SMALL, AircraftSize.MEDIUM, AircraftSize.LARGE),
                runway.getSupportedAircraft());
    }

    @Test
    void smallAircraftCanUseAnyRunwaySize() {
        assertTrue(smallAircraft().canUseRunwayOfSize(AircraftSize.SMALL));
        assertTrue(smallAircraft().canUseRunwayOfSize(AircraftSize.MEDIUM));
        assertTrue(smallAircraft().canUseRunwayOfSize(AircraftSize.LARGE));
        assertTrue(runway(AircraftSize.SMALL).supports(aircraft(AircraftSize.SMALL)));
        assertTrue(runway(AircraftSize.MEDIUM).supports(aircraft(AircraftSize.SMALL)));
        assertTrue(runway(AircraftSize.LARGE).supports(aircraft(AircraftSize.SMALL)));
    }

    @Test
    void mediumAircraftCanUseMediumOrLargeRunways() {
        assertFalse(runway(AircraftSize.SMALL).supports(aircraft(AircraftSize.MEDIUM)));
        assertTrue(runway(AircraftSize.MEDIUM).supports(aircraft(AircraftSize.MEDIUM)));
        assertTrue(runway(AircraftSize.LARGE).supports(aircraft(AircraftSize.MEDIUM)));
    }

    @Test
    void largeAircraftCanUseOnlyLargeRunways() {
        assertFalse(runway(AircraftSize.SMALL).supports(aircraft(AircraftSize.LARGE)));
        assertFalse(runway(AircraftSize.MEDIUM).supports(aircraft(AircraftSize.LARGE)));
        assertTrue(runway(AircraftSize.LARGE).supports(aircraft(AircraftSize.LARGE)));
    }

    private static AircraftSize smallAircraft() {
        return AircraftSize.SMALL;
    }

    private static Runway runway(AircraftSize size) {
        return new Runway("RWY-27", RunwayStatus.AVAILABLE, size);
    }

    private static Aircraft aircraft(AircraftSize size) {
        return new Aircraft(
                "AC-" + size,
                size,
                Priority.PRIORITY_2,
                AircraftStatus.LANDING,
                EnumSet.noneOf(AircraftRequirement.class));
    }

    @Test
    void aircraftRequestStoresAircraftTimeAndPriority() {
        Aircraft aircraft = new Aircraft(
                "DD404",
                AircraftSize.SMALL,
                Priority.PRIORITY_1,
                AircraftStatus.LANDING,
                EnumSet.noneOf(AircraftRequirement.class));
        Instant requestedAt = Instant.parse("2026-09-10T08:00:00Z");

        AircraftRequest request = new AircraftRequest(aircraft, requestedAt, Priority.PRIORITY_1);

        assertEquals(aircraft, request.getAircraft());
        assertEquals(requestedAt, request.getRequestTime());
        assertEquals(Priority.PRIORITY_1, request.getPriority());
    }
}
