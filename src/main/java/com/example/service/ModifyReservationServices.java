package com.example.service;

import com.example.model.Reservation;
import com.example.model.Seat;
import com.example.model.User;

public class ModifyReservationServices {
    private final ReservationStore store;

    public ModifyReservationServices(ReservationStore store) {
        this.store = store;
    }

    public void cancelReservation(String reservationId) {
        Reservation reservation = store.getReservations().remove(reservationId);
        if (reservation == null) {
            throw new IllegalArgumentException("Unknown reservation: " + reservationId);
        }
        Seat seat = store.getCinema().findSeat(reservation.getSeatId());
        seat.releaseIfReservedBy(reservationId);

        User user = store.getUsers().get(reservation.getUserId());
        if (user != null && reservationId.equals(user.getReservationId())) {
            user.setReservationId(null);
        }
    }
}
