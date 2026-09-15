package com.example.service;

import com.example.model.Cart;
import com.example.repository.UserCartRepository;

import java.util.HashMap;

public class ShoppingService {
    private final UserCartRepository userCartRepository;

    public ShoppingService(UserCartRepository userCartRepository) {
        this.userCartRepository = userCartRepository;
    }

    public void addProductToCart(String userId, String productName, int count) {
        Cart cart = userCartRepository.findByUserId(userId);
        if (cart == null) {
            cart = new Cart(userId, new HashMap<>(), userId);
        }
        cart.addProduct(productName, count);
        userCartRepository.save(userId, cart);
    }

    public Cart viewCart(String userId) {
        return userCartRepository.findByUserId(userId);
    }

    public void removeProductFromCart(String userId, String productName, int count) {
        Cart cart = userCartRepository.findByUserId(userId);
        if (cart == null) {
            cart = new Cart(userId, new HashMap<>(), userId);
        }
        cart.removeProduct(productName, count);
        userCartRepository.save(userId, cart);
    }   

    public void emptyCart(String userId) {
        Cart cart = userCartRepository.findByUserId(userId);
        if (cart == null) {
            cart = new Cart(userId, new HashMap<>(), userId);
        }
        cart.emptyCart();
        userCartRepository.save(userId, cart);
    }
}
