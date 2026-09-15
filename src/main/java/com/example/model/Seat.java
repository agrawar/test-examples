package com.example.model;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class Seat {
    private final String seatId;
    private final AtomicReference<Holder> holder = new AtomicReference<>();

    public Seat(String seatId) {
        this.seatId = Objects.requireNonNull(seatId, "seatId is required");
    }

    public String getSeatId() {
        return seatId;
    }

    public String getReservationId() {
        Holder current = holder.get();
        return current == null ? null : current.reservationId;
    }

    public String getUserId() {
        Holder current = holder.get();
        return current == null ? null : current.userId;
    }

    public boolean isReserved() {
        return holder.get() != null;
    }

    public boolean tryReserve(String reservationId, String userId) {
        Objects.requireNonNull(reservationId, "reservationId is required");
        Objects.requireNonNull(userId, "userId is required");
        return holder.compareAndSet(null, new Holder(reservationId, userId));
    }

    public boolean releaseIfReservedBy(String reservationId) {
        Objects.requireNonNull(reservationId, "reservationId is required");
        Holder current = holder.get();
        if (current == null || !reservationId.equals(current.reservationId)) {
            return false;
        }
        return holder.compareAndSet(current, null);
    }

    private record Holder(String reservationId, String userId) {
    }
}
