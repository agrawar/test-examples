package com.example.airport.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.Instant;
import java.util.EnumSet;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.example.airport.model.Aircraft;
import com.example.airport.model.AircraftRequirement;
import com.example.airport.model.AircraftSize;
import com.example.airport.model.AircraftStatus;
import com.example.airport.model.AircraftRequest;
import com.example.airport.model.Priority;
import com.example.airport.model.Runway;
import com.example.airport.model.RunwayAssignment;
import com.example.airport.model.RunwayStatus;

class LandingRequestServiceTest {

    @Test
    void priorityOneLandingIsAssignedBeforeLowerPriority() {
        Runway runway = availableRunway("RWY-09");
        LandingRequestService service = new LandingRequestService(List.of(runway));
        AircraftRequest priorityTwo = landingRequest("P2", Priority.PRIORITY_2, Instant.parse("2026-09-10T08:00:00Z"));
        AircraftRequest priorityOne = landingRequest("P1", Priority.PRIORITY_1, Instant.parse("2026-09-10T08:01:00Z"));

        service.addRequest(priorityTwo);
        service.addRequest(priorityOne);

        RunwayAssignment assignment = service.assignRunway();

        assertNotNull(assignment);
        assertEquals("P1", assignment.getRequest().getAircraft().getId());
        assertEquals(runway, assignment.getRunway());
    }

    @Test
    void landingUpdatesRunwayFromAvailableToOccupied() {
        Runway runway = availableRunway("RWY-09");
        LandingRequestService service = new LandingRequestService(List.of(runway));
        service.addRequest(landingRequest("AA101", Priority.PRIORITY_1, Instant.parse("2026-09-10T08:00:00Z")));

        RunwayAssignment assignment = service.assignRunway();

        assertNotNull(assignment);
        assertEquals(RunwayStatus.OCCUPIED, assignment.getRunway().getStatus());
        assertEquals(RunwayStatus.OCCUPIED, runway.getStatus());
        assertEquals(AircraftStatus.LANDED, assignment.getRequest().getAircraft().getStatus());
    }

    @Test
    void landingIsAssignedOnlyToAnAvailableRunway() {
        Runway occupied = occupiedRunway("RWY-09");
        Runway available = availableRunway("RWY-27");
        LandingRequestService service = new LandingRequestService(List.of(occupied, available));
        service.addRequest(landingRequest("AA101", Priority.PRIORITY_1, Instant.parse("2026-09-10T08:00:00Z")));

        RunwayAssignment assignment = service.assignRunway();

        assertNotNull(assignment);
        assertEquals("RWY-27", assignment.getRunway().getId());
        assertEquals(RunwayStatus.OCCUPIED, available.getStatus());
        assertEquals(RunwayStatus.OCCUPIED, occupied.getStatus());
    }

    @Test
    void landingIsNotAssignedWhenNoRunwayIsAvailable() {
        Runway occupied = occupiedRunway("RWY-09");
        LandingRequestService service = new LandingRequestService(List.of(occupied));
        service.addRequest(landingRequest("AA101", Priority.PRIORITY_1, Instant.parse("2026-09-10T08:00:00Z")));

        assertNull(service.assignRunway());
        assertEquals(RunwayStatus.OCCUPIED, occupied.getStatus());
    }

    @Test
    void landingIsNotAssignedWhenRunwayIsTooSmallForAircraft() {
        Runway small = new Runway("RWY-09", RunwayStatus.AVAILABLE, AircraftSize.SMALL);
        LandingRequestService service = new LandingRequestService(List.of(small));
        service.addRequest(landingRequest("AA101", AircraftSize.LARGE, Priority.PRIORITY_1, Instant.parse("2026-09-10T08:00:00Z")));

        assertNull(service.assignRunway());
        assertEquals(RunwayStatus.AVAILABLE, small.getStatus());
    }

    @Test
    void landingPrefersARunwayLargeEnoughForTheAircraft() {
        Runway small = new Runway("RWY-09", RunwayStatus.AVAILABLE, AircraftSize.SMALL);
        Runway large = new Runway("RWY-27", RunwayStatus.AVAILABLE, AircraftSize.LARGE);
        LandingRequestService service = new LandingRequestService(List.of(small, large));
        service.addRequest(landingRequest("AA101", AircraftSize.LARGE, Priority.PRIORITY_1, Instant.parse("2026-09-10T08:00:00Z")));

        RunwayAssignment assignment = service.assignRunway();

        assertNotNull(assignment);
        assertEquals("RWY-27", assignment.getRunway().getId());
        assertEquals(RunwayStatus.AVAILABLE, small.getStatus());
        assertEquals(RunwayStatus.OCCUPIED, large.getStatus());
    }

    private static Runway availableRunway(String id) {
        return new Runway(id, RunwayStatus.AVAILABLE, AircraftSize.LARGE);
    }

    private static Runway occupiedRunway(String id) {
        return new Runway(id, RunwayStatus.OCCUPIED, AircraftSize.LARGE);
    }

    private static AircraftRequest landingRequest(String aircraftId, Priority priority, Instant requestedAt) {
        return landingRequest(aircraftId, AircraftSize.MEDIUM, priority, requestedAt);
    }

    private static AircraftRequest landingRequest(
            String aircraftId, AircraftSize size, Priority priority, Instant requestedAt) {
        Aircraft aircraft = new Aircraft(
                aircraftId,
                size,
                priority,
                AircraftStatus.LANDING,
                EnumSet.noneOf(AircraftRequirement.class));
        return new AircraftRequest(aircraft, requestedAt, priority);
    }
}
