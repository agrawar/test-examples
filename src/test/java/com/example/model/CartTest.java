package com.example.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

class CartTest {

    private Cart cartWith(String productName, int count) {
        Cart cart = new Cart("cart1", new HashMap<>(), "user1");
        cart.addProduct(productName, count);
        return cart;
    }

    @Test
    void removeProductThrowsWhenProductNotInCart() {
        Cart cart = cartWith("apple", 3);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> cart.removeProduct("banana", 1));

        assertEquals("Product not found in cart", exception.getMessage());
    }

    @Test
    void removeProductThrowsWhenCartIsEmpty() {
        Cart cart = new Cart("cart1", new HashMap<>(), "user1");

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> cart.removeProduct("apple", 1));

        assertEquals("Product not found in cart", exception.getMessage());
    }

    @Test
    void removeProductRejectsNullOrBlankProductName() {
        Cart cart = cartWith("apple", 3);

        assertEquals("Product name must not be null or blank",
                assertThrows(IllegalArgumentException.class, () -> cart.removeProduct(null, 1)).getMessage());
        assertEquals("Product name must not be null or blank",
                assertThrows(IllegalArgumentException.class, () -> cart.removeProduct("  ", 1)).getMessage());
    }

    @Test
    void removeProductRejectsNonPositiveCount() {
        Cart cart = cartWith("apple", 3);

        assertEquals("Count must be greater than 0",
                assertThrows(IllegalArgumentException.class, () -> cart.removeProduct("apple", 0)).getMessage());
        assertEquals("Count must be greater than 0",
                assertThrows(IllegalArgumentException.class, () -> cart.removeProduct("apple", -2)).getMessage());
        assertEquals(Map.of("apple", 3), cart.getProducts());
    }

    @Test
    void addProductRejectsNullOrBlankProductName() {
        Cart cart = new Cart("cart1", new HashMap<>(), "user1");

        assertEquals("Product name must not be null or blank",
                assertThrows(IllegalArgumentException.class, () -> cart.addProduct(null, 1)).getMessage());
        assertEquals("Product name must not be null or blank",
                assertThrows(IllegalArgumentException.class, () -> cart.addProduct("  ", 1)).getMessage());
        assertEquals(Map.of(), cart.getProducts());
    }

    @Test
    void addProductRejectsNonPositiveCount() {
        Cart cart = new Cart("cart1", new HashMap<>(), "user1");

        assertEquals("Count must be greater than 0",
                assertThrows(IllegalArgumentException.class, () -> cart.addProduct("apple", 0)).getMessage());
        assertEquals("Count must be greater than 0",
                assertThrows(IllegalArgumentException.class, () -> cart.addProduct("apple", -3)).getMessage());
        assertEquals(Map.of(), cart.getProducts());
    }

    @Test
    void removeProductThrowsWhenCountExceedsQuantity() {
        Cart cart = cartWith("apple", 2);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> cart.removeProduct("apple", 3));

        assertEquals("Not enough products in cart", exception.getMessage());
    }

    @Test
    void removeProductLeavesCartUnchangedWhenItThrows() {
        Cart cart = cartWith("apple", 2);

        assertThrows(RuntimeException.class, () -> cart.removeProduct("apple", 5));
        assertThrows(RuntimeException.class, () -> cart.removeProduct("banana", 1));

        assertEquals(Map.of("apple", 2), cart.getProducts());
    }

    @Test
    void removeProductRemovesEntryWhenCountEqualsQuantity() {
        Cart cart = cartWith("apple", 2);

        cart.removeProduct("apple", 2);

        assertEquals(Map.of(), cart.getProducts());
    }

    @Test
    void removeProductKeepsRemainderWhenCountIsBelowQuantity() {
        Cart cart = cartWith("apple", 3);

        cart.removeProduct("apple", 1);

        assertEquals(Map.of("apple", 2), cart.getProducts());
    }

    @Test
    void addProductTreatsDifferentCasingsAsTheSameProduct() {
        Cart cart = new Cart("cart1", new HashMap<>(), "user1");

        cart.addProduct("APPLE", 1);
        cart.addProduct("apPle", 2);
        cart.addProduct("apple", 3);

        assertEquals(Map.of("apple", 6), cart.getProducts());
    }

    @Test
    void removeProductTreatsDifferentCasingsAsTheSameProduct() {
        Cart cart = cartWith("apple", 5);

        cart.removeProduct("APPLE", 2);
        cart.removeProduct("apPle", 1);

        assertEquals(Map.of("apple", 2), cart.getProducts());
    }

    @Test
    void removeProductOnlyAffectsTheTargetedProduct() {
        Cart cart = cartWith("apple", 3);
        cart.addProduct("banana", 2);

        cart.removeProduct("apple", 3);

        assertEquals(Map.of("banana", 2), cart.getProducts());
    }
}
