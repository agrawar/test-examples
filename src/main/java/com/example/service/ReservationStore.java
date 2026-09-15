package com.example.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.example.model.Cinema;
import com.example.model.Reservation;
import com.example.model.User;

public class ReservationStore {
    private final Cinema cinema;
    private final Map<String, Reservation> reservations = new ConcurrentHashMap<>();
    private final Map<String, User> users = new ConcurrentHashMap<>();

    public ReservationStore(Cinema cinema) {
        this.cinema = cinema;
    }

    public Cinema getCinema() {
        return cinema;
    }

    public Map<String, Reservation> getReservations() {
        return reservations;
    }

    public Map<String, User> getUsers() {
        return users;
    }

    public User getOrCreateUser(String userId) {
        return users.computeIfAbsent(userId, id -> new User(id, null, null));
    }
}
