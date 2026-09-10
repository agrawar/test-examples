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

class TakeoffRequestServiceTest {

    @Test
    void priorityOneTakeoffIsAssignedBeforeLowerPriority() {
        Runway runway = occupiedRunway("RWY-09");
        TakeoffRequestService service = new TakeoffRequestService(List.of(runway));
        AircraftRequest priorityTwo = takeoffRequest("P2", Priority.PRIORITY_2, Instant.parse("2026-09-10T08:00:00Z"));
        AircraftRequest priorityOne = takeoffRequest("P1", Priority.PRIORITY_1, Instant.parse("2026-09-10T08:01:00Z"));

        service.addRequest(priorityTwo);
        service.addRequest(priorityOne);

        RunwayAssignment assignment = service.assignRunway();

        assertNotNull(assignment);
        assertEquals("P1", assignment.getRequest().getAircraft().getId());
        assertEquals(runway, assignment.getRunway());
    }

    @Test
    void takeoffUpdatesRunwayFromOccupiedToAvailable() {
        Runway runway = occupiedRunway("RWY-09");
        TakeoffRequestService service = new TakeoffRequestService(List.of(runway));
        service.addRequest(takeoffRequest("AA101", Priority.PRIORITY_1, Instant.parse("2026-09-10T08:00:00Z")));

        RunwayAssignment assignment = service.assignRunway();

        assertNotNull(assignment);
        assertEquals(RunwayStatus.AVAILABLE, assignment.getRunway().getStatus());
        assertEquals(RunwayStatus.AVAILABLE, runway.getStatus());
        assertEquals(AircraftStatus.TAKING_OFF, assignment.getRequest().getAircraft().getStatus());
    }

    @Test
    void takeoffIsAssignedOnlyToAnOccupiedRunway() {
        Runway available = availableRunway("RWY-09");
        Runway occupied = occupiedRunway("RWY-27");
        TakeoffRequestService service = new TakeoffRequestService(List.of(available, occupied));
        service.addRequest(takeoffRequest("AA101", Priority.PRIORITY_1, Instant.parse("2026-09-10T08:00:00Z")));

        RunwayAssignment assignment = service.assignRunway();

        assertNotNull(assignment);
        assertEquals("RWY-27", assignment.getRunway().getId());
        assertEquals(RunwayStatus.AVAILABLE, occupied.getStatus());
        assertEquals(RunwayStatus.AVAILABLE, available.getStatus());
    }

    @Test
    void takeoffIsNotAssignedWhenNoRunwayIsOccupied() {
        Runway available = availableRunway("RWY-09");
        TakeoffRequestService service = new TakeoffRequestService(List.of(available));
        service.addRequest(takeoffRequest("AA101", Priority.PRIORITY_1, Instant.parse("2026-09-10T08:00:00Z")));

        assertNull(service.assignRunway());
        assertEquals(RunwayStatus.AVAILABLE, available.getStatus());
    }

    private static Runway availableRunway(String id) {
        return new Runway(id, RunwayStatus.AVAILABLE, AircraftSize.LARGE);
    }

    private static Runway occupiedRunway(String id) {
        return new Runway(id, RunwayStatus.OCCUPIED, AircraftSize.LARGE);
    }

    private static AircraftRequest takeoffRequest(String aircraftId, Priority priority, Instant requestedAt) {
        Aircraft aircraft = new Aircraft(
                aircraftId,
                AircraftSize.MEDIUM,
                priority,
                AircraftStatus.READY_FOR_TAKEOFF,
                EnumSet.noneOf(AircraftRequirement.class));
        return new AircraftRequest(aircraft, requestedAt, priority);
    }
}
