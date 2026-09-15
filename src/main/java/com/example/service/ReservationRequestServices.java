package com.example.service;

import java.time.Instant;
import java.util.List;

import com.example.model.Reservation;
import com.example.model.Seat;

public class ReservationRequestServices {
    private final ReservationStore store;

    public ReservationRequestServices(ReservationStore store) {
        this.store = store;
    }

    public List<Seat> getAvailableSeats() {
        return store.getCinema().getSeats().stream()
                .filter(seat -> !seat.isReserved())
                .toList();
    }

    public Reservation createReservation(String userId, String seatId) {
        required(userId, "userId");
        required(seatId, "seatId");

        Seat seat = store.getCinema().findSeat(seatId);
        Reservation reservation = new Reservation(seatId, userId, Instant.now());
        if (!seat.tryReserve(reservation.getReservationId(), userId)) {
            throw new IllegalStateException("Seat is already reserved: " + seatId);
        }

        store.getOrCreateUser(userId).setReservationId(reservation.getReservationId());
        store.getReservations().put(reservation.getReservationId(), reservation);
        return reservation;
    }

    private static void required(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
    }
}
