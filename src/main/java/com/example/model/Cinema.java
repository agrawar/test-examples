package com.example.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Cinema {
    private final List<Seat> seats;

    public Cinema(List<Seat> seats) {
        Objects.requireNonNull(seats, "seats is required");
        this.seats = new ArrayList<>(seats);
    }

    public List<Seat> getSeats() {
        return Collections.unmodifiableList(seats);
    }

    public Seat findSeat(String seatId) {
        return seats.stream()
                .filter(seat -> seat.getSeatId().equals(seatId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown seat: " + seatId));
    }
}
