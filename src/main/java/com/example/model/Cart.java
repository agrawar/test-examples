package com.example.model;

import java.util.HashMap;
import java.util.Locale;

public class Cart {
    private String cartId;
    private HashMap<String, Integer> products;
    private String userId;

    public Cart() {
        this.products = new HashMap<>();
    }

    public Cart(String cartId, HashMap<String, Integer> products, String userId) {
        this.cartId = cartId;
        this.products = products != null ? products : new HashMap<>();
        this.userId = userId;
    }

    public String getCartId() {
        return cartId;
    }

    public void setCartId(String cartId) {
        this.cartId = cartId;
    }

    public void addProduct(String productName, int count) {
        validateProductName(productName);
        validateCount(count);
        products.merge(normalize(productName), count, Integer::sum);
    }

    public void emptyCart() {
        products.clear();
    }

    private static void validateProductName(String productName) {
        if (productName == null || productName.isBlank()) {
            throw new IllegalArgumentException("Product name must not be null or blank");
        }
    }

    private static void validateCount(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("Count must be greater than 0");
        }
    }

    // Locale.ROOT keeps the mapping stable: a Turkish default locale lowercases "I" to a dotless "ı".
    private static String normalize(String productName) {
        return productName.trim().toLowerCase(Locale.ROOT);
    }

    public void removeProduct(String productName, int count) {
        validateProductName(productName);
        validateCount(count);
        String key = normalize(productName);
        Integer current = products.get(key);
        if (current == null) {
            throw new RuntimeException("Product not found in cart");
        }
        if (current < count) {
            throw new RuntimeException("Not enough products in cart");
        }
        if (current == count) {
            products.remove(key);
        } else {
            products.put(key, current - count);
        }
    }

    public HashMap<String, Integer> getProducts() {
        return products;
    }

    public void setProducts(HashMap<String, Integer> products) {
        this.products = products;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
