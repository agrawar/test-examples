package com.example.model;

import java.util.Objects;

public class User {
    private final String userId;
    private String reservationId;

    public User(String userId, String seatId, String reservationId) {
        this.userId = Objects.requireNonNull(userId, "userId is required");
        this.reservationId = reservationId;
    }

    public String getUserId() {
        return userId;
    }

    public String getReservationId() {
        return reservationId;
    }

    public void setReservationId(String reservationId) {
        this.reservationId = reservationId;
    }
}
