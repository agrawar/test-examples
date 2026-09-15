package com.example.repository;

import com.example.model.Cart;

import java.util.HashMap;
import java.util.Map;

public class UserCartRepository {
    private final Map<String, Cart> cartsByUserId = new HashMap<>();

    public void save(String userId, Cart cart) {
        cartsByUserId.put(userId, cart);
    }

    public Cart findByUserId(String userId) {
        return cartsByUserId.get(userId);
    }

    public boolean existsByUserId(String userId) {
        return cartsByUserId.containsKey(userId);
    }

    public void deleteByUserId(String userId) {
        cartsByUserId.remove(userId);
    }
}
