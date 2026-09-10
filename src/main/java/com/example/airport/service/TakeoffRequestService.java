package com.example.airport.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import com.example.airport.model.Aircraft;
import com.example.airport.model.AircraftStatus;
import com.example.airport.model.AircraftRequest;
import com.example.airport.model.Runway;
import com.example.airport.model.RunwayAssignment;
import com.example.airport.model.RunwayStatus;

public class TakeoffRequestService {

    private static final Comparator<AircraftRequest> BY_PRIORITY_THEN_TIME =
            Comparator.comparingInt((AircraftRequest request) -> request.getPriority().getLevel())
                    .thenComparing(AircraftRequest::getRequestTime);

    private final List<Runway> runways;
    private final List<AircraftRequest> takeoffQueue = new ArrayList<>();

    public TakeoffRequestService(List<Runway> runways) {
        this.runways = new ArrayList<>(Objects.requireNonNull(runways, "runways"));
    }

    public void addRequest(AircraftRequest request) {
        takeoffQueue.add(Objects.requireNonNull(request, "request"));
    }

    public RunwayAssignment assignRunway() {
        AircraftRequest request = nextTakeoffRequestWithOccupiedRunway();
        if (request == null) {
            return null;
        }

        Runway runway = findOccupiedRunway(request.getAircraft());
        takeoffQueue.remove(request);
        runway.setStatus(RunwayStatus.AVAILABLE);
        request.getAircraft().setStatus(AircraftStatus.TAKING_OFF);
        return new RunwayAssignment(request, runway);
    }

    private AircraftRequest nextTakeoffRequestWithOccupiedRunway() {
        return takeoffQueue.stream()
                .sorted(BY_PRIORITY_THEN_TIME)
                .filter(request -> findOccupiedRunway(request.getAircraft()) != null)
                .findFirst()
                .orElse(null);
    }

    private Runway findOccupiedRunway(Aircraft aircraft) {
        return runways.stream()
                .filter(runway -> runway.getStatus() == RunwayStatus.OCCUPIED)
                .filter(runway -> runway.supports(aircraft))
                .findFirst()
                .orElse(null);
    }
}
