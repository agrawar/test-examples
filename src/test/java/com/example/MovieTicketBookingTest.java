package com.example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.model.Cinema;
import com.example.model.Reservation;
import com.example.model.Seat;
import com.example.model.User;
import com.example.service.ModifyReservationServices;
import com.example.service.ReservationRequestServices;
import com.example.service.ReservationStore;

class MovieTicketBookingTest {

    private Cinema cinema;
    private ReservationStore store;
    private ReservationRequestServices reservationRequestServices;
    private ModifyReservationServices modifyReservationServices;

    @BeforeEach
    void setUp() {
        cinema = new Cinema(List.of(new Seat("1"), new Seat("2"), new Seat("3")));
        store = new ReservationStore(cinema);
        reservationRequestServices = new ReservationRequestServices(store);
        modifyReservationServices = new ModifyReservationServices(store);
    }

    @Test
    void seatIsReservedOnceReservationIsCreated() {
        Reservation reservation = reservationRequestServices.createReservation("user-1", "1");

        Seat reservedSeat = cinema.findSeat("1");
        assertTrue(reservedSeat.isReserved());
        assertEquals(reservation.getReservationId(), reservedSeat.getReservationId());
        assertEquals("user-1", reservedSeat.getUserId());
        assertEquals("1", reservation.getSeatId());
        assertEquals("user-1", reservation.getUserId());
        assertFalse(reservation.getReservationId().isBlank());
        assertEquals(reservation, store.getReservations().get(reservation.getReservationId()));
        assertEquals(reservation.getReservationId(), store.getUsers().get("user-1").getReservationId());
    }

    @Test
    void userCanSeeAllAvailableSeatsBeforeChoosing() {
        List<Seat> availableSeats = reservationRequestServices.getAvailableSeats();

        assertEquals(3, availableSeats.size());
        assertTrue(availableSeats.stream().noneMatch(Seat::isReserved));
        assertEquals(List.of("1", "2", "3"), availableSeats.stream().map(Seat::getSeatId).toList());
    }

    @Test
    void userCanCancelReservationAndSeatBecomesAvailable() {
        Reservation reservation = reservationRequestServices.createReservation("user-1", "1");
        assertTrue(cinema.findSeat("1").isReserved());

        modifyReservationServices.cancelReservation(reservation.getReservationId());

        Seat seat = cinema.findSeat("1");
        assertFalse(seat.isReserved());
        assertNull(seat.getReservationId());
        assertNull(seat.getUserId());
        User user = store.getUsers().get("user-1");
        assertNull(user.getReservationId());
        assertTrue(reservationRequestServices.getAvailableSeats().stream()
                .anyMatch(available -> available.getSeatId().equals("1")));
    }

    @Test
    void reservedSeatIsNoLongerListedAsAvailable() {
        reservationRequestServices.createReservation("user-1", "1");

        List<Seat> availableSeats = reservationRequestServices.getAvailableSeats();

        assertEquals(List.of("2", "3"), availableSeats.stream().map(Seat::getSeatId).toList());
        assertTrue(availableSeats.stream().noneMatch(seat -> seat.getSeatId().equals("1")));
    }

    @Test
    void sequentialBookingOfSameSeatFails() {
        reservationRequestServices.createReservation("user-1", "1");

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> reservationRequestServices.createReservation("user-2", "1"));

        assertEquals("Seat is already reserved: 1", exception.getMessage());
        assertEquals(1, store.getReservations().size());
        assertEquals("user-1", cinema.findSeat("1").getUserId());
    }

    @Test
    void unknownSeatCannotBeReserved() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> reservationRequestServices.createReservation("user-1", "99"));

        assertEquals("Unknown seat: 99", exception.getMessage());
        assertTrue(store.getReservations().isEmpty());
    }

    @Test
    void blankReservationFieldsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> reservationRequestServices.createReservation(" ", "1"));
        assertThrows(IllegalArgumentException.class, () -> reservationRequestServices.createReservation("user-1", " "));
        assertTrue(store.getReservations().isEmpty());
        assertTrue(reservationRequestServices.getAvailableSeats().stream().noneMatch(Seat::isReserved));
    }

    @Test
    void unknownReservationCannotBeCancelled() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> modifyReservationServices.cancelReservation("missing-id"));

        assertEquals("Unknown reservation: missing-id", exception.getMessage());
    }

    @Test
    void seatCanBeBookedAgainAfterCancellation() {
        Reservation first = reservationRequestServices.createReservation("user-1", "1");
        modifyReservationServices.cancelReservation(first.getReservationId());

        Reservation second = reservationRequestServices.createReservation("user-2", "1");

        Seat seat = cinema.findSeat("1");
        assertTrue(seat.isReserved());
        assertEquals("user-2", seat.getUserId());
        assertEquals(second.getReservationId(), seat.getReservationId());
        assertFalse(store.getReservations().containsKey(first.getReservationId()));
        assertEquals(second, store.getReservations().get(second.getReservationId()));
    }

    @Test
    void differentUsersCanReserveDifferentSeats() {
        Reservation first = reservationRequestServices.createReservation("user-1", "1");
        Reservation second = reservationRequestServices.createReservation("user-2", "2");

        assertEquals(2, store.getReservations().size());
        assertEquals("user-1", cinema.findSeat("1").getUserId());
        assertEquals("user-2", cinema.findSeat("2").getUserId());
        assertEquals(List.of("3"), reservationRequestServices.getAvailableSeats().stream()
                .map(Seat::getSeatId)
                .toList());
        assertEquals(first.getReservationId(), store.getUsers().get("user-1").getReservationId());
        assertEquals(second.getReservationId(), store.getUsers().get("user-2").getReservationId());
    }

    @Test
    void cancellingOneReservationDoesNotReleaseAnotherSeat() {
        Reservation keep = reservationRequestServices.createReservation("user-1", "1");
        Reservation cancel = reservationRequestServices.createReservation("user-2", "2");

        modifyReservationServices.cancelReservation(cancel.getReservationId());

        assertTrue(cinema.findSeat("1").isReserved());
        assertEquals(keep.getReservationId(), cinema.findSeat("1").getReservationId());
        assertFalse(cinema.findSeat("2").isReserved());
        assertEquals(1, store.getReservations().size());
        assertEquals(keep, store.getReservations().get(keep.getReservationId()));
    }

    @Test
    void concurrentBookingsForSameSeatAllowOnlyOneReservation() throws Exception {
        int threads = 20;
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        List<Future<Boolean>> results = new ArrayList<>();

        try {
            for (int i = 0; i < threads; i++) {
                String userId = "user-" + i;
                results.add(executor.submit(() -> {
                    start.await();
                    try {
                        reservationRequestServices.createReservation(userId, "1");
                        return true;
                    } catch (IllegalStateException alreadyReserved) {
                        return false;
                    }
                }));
            }

            start.countDown();

            int successes = 0;
            for (Future<Boolean> result : results) {
                if (result.get(5, TimeUnit.SECONDS)) {
                    successes++;
                }
            }

            assertEquals(1, successes);
            assertEquals(1, store.getReservations().size());
            Seat seat = cinema.findSeat("1");
            assertTrue(seat.isReserved());
            assertEquals(seat.getReservationId(), store.getReservations().keySet().iterator().next());
        } finally {
            executor.shutdownNow();
        }
    }
}
