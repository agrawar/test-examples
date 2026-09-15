package com.example.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class Reservation {
    private final String reservationId;
    private final String seatId;
    private final String userId;
    private final Instant timestamp;

    public Reservation(String seatId, String userId, Instant timestamp) {
        this.reservationId = UUID.randomUUID().toString();
        this.seatId = Objects.requireNonNull(seatId, "seatId is required");
        this.userId = Objects.requireNonNull(userId, "userId is required");
        this.timestamp = Objects.requireNonNull(timestamp, "timestamp is required");
    }

    public String getReservationId() {
        return reservationId;
    }

    public String getSeatId() {
        return seatId;
    }

    public String getUserId() {
        return userId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}
