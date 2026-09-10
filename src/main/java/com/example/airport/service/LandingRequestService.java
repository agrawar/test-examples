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

public class LandingRequestService {

    private static final Comparator<AircraftRequest> BY_PRIORITY_THEN_TIME =
            Comparator.comparingInt((AircraftRequest request) -> request.getPriority().getLevel())
                    .thenComparing(AircraftRequest::getRequestTime);

    private final List<Runway> runways;
    private final List<AircraftRequest> landingQueue = new ArrayList<>();

    public LandingRequestService(List<Runway> runways) {
        this.runways = new ArrayList<>(Objects.requireNonNull(runways, "runways"));
    }

    public void addRequest(AircraftRequest request) {
        landingQueue.add(Objects.requireNonNull(request, "request"));
    }

    public RunwayAssignment assignRunway() {
        AircraftRequest request = nextLandingRequestWithAvailableRunway();
        if (request == null) {
            return null;
        }

        Runway runway = findAvailableRunway(request.getAircraft());
        landingQueue.remove(request);
        runway.setStatus(RunwayStatus.OCCUPIED);
        request.getAircraft().setStatus(AircraftStatus.LANDED);
        return new RunwayAssignment(request, runway);
    }

    private AircraftRequest nextLandingRequestWithAvailableRunway() {
        return landingQueue.stream()
                .sorted(BY_PRIORITY_THEN_TIME)
                .filter(request -> findAvailableRunway(request.getAircraft()) != null)
                .findFirst()
                .orElse(null);
    }

    private Runway findAvailableRunway(Aircraft aircraft) {
        return runways.stream()
                .filter(runway -> runway.getStatus() == RunwayStatus.AVAILABLE)
                .filter(runway -> runway.supports(aircraft))
                .findFirst()
                .orElse(null);
    }
}
