package com.example.airport.model;

public class RunwayAssignment {
    private final AircraftRequest request;
    private final Runway runway;

    public RunwayAssignment(AircraftRequest request, Runway runway) {
        this.request = request;
        this.runway = runway;
    }

    public AircraftRequest getRequest() {
        return request;
    }

    public Runway getRunway() {
        return runway;
    }
}
