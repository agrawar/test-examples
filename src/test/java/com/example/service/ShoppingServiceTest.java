package com.example.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.example.model.Cart;
import com.example.repository.UserCartRepository;

class ShoppingServiceTest {


    @Test
    void addProductToCartSuccessfullyAddsIt() {
        // Arrange
        ShoppingService shoppingService = new ShoppingService(new UserCartRepository());
        Map<String, Integer> expectedProducts = Map.of("apple", 4, "banana", 2);

        // Act
        shoppingService.addProductToCart("user1", "apple", 1);
        shoppingService.addProductToCart("user1", "banana", 2);
        shoppingService.addProductToCart("user1", "apple", 3);

        // Assert
        assertEquals(expectedProducts, shoppingService.viewCart("user1").getProducts());
    }

    @Test
    void viewCart() {
        // Arrange
        ShoppingService shoppingService = new ShoppingService(new UserCartRepository());
        shoppingService.addProductToCart("user1", "apple", 1);
        shoppingService.addProductToCart("user1", "banana", 2);
        shoppingService.addProductToCart("user1", "orange", 3);
        Map<String, Integer> expectedProducts = new HashMap<>();
        expectedProducts.put("apple", 1);
        expectedProducts.put("banana", 2);
        expectedProducts.put("orange", 3);

        // Act
        Cart cart = shoppingService.viewCart("user1");

        // Assert
        assertEquals(expectedProducts, cart.getProducts());
    }

    @Test
    void removeProductFromCartDecrementsCount() {
        ShoppingService shoppingService = new ShoppingService(new UserCartRepository());
        shoppingService.addProductToCart("user1", "apple", 4);
        shoppingService.addProductToCart("user1", "banana", 2);

        shoppingService.removeProductFromCart("user1", "apple", 3);
        shoppingService.removeProductFromCart("user1", "banana", 2);

        Map<String, Integer> expectedProducts = new HashMap<>();
        expectedProducts.put("apple", 1);
        assertEquals(expectedProducts, shoppingService.viewCart("user1").getProducts());
    }

    @Test
    void removeProductFromCartThrowsWhenProductNotInCart() {
        ShoppingService shoppingService = new ShoppingService(new UserCartRepository());
        shoppingService.addProductToCart("user1", "apple", 4);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> shoppingService.removeProductFromCart("user1", "banana", 1));

        assertEquals("Product not found in cart", exception.getMessage());
        assertEquals(Map.of("apple", 4), shoppingService.viewCart("user1").getProducts());
    }

    @Test
    void removeProductFromCartThrowsWhenCountExceedsQuantity() {
        ShoppingService shoppingService = new ShoppingService(new UserCartRepository());
        shoppingService.addProductToCart("user1", "apple", 4);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> shoppingService.removeProductFromCart("user1", "apple", 5));

        assertEquals("Not enough products in cart", exception.getMessage());
        assertEquals(Map.of("apple", 4), shoppingService.viewCart("user1").getProducts());
    }

    @Test
    void removeProductFromCartThrowsForUserWithoutACart() {
        ShoppingService shoppingService = new ShoppingService(new UserCartRepository());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> shoppingService.removeProductFromCart("newUser", "apple", 1));

        assertEquals("Product not found in cart", exception.getMessage());
    }

    @Test
    void removeProductFromCartRemovesEntryWhenCountEqualsQuantity() {
        ShoppingService shoppingService = new ShoppingService(new UserCartRepository());
        shoppingService.addProductToCart("user1", "apple", 4);
        shoppingService.addProductToCart("user1", "banana", 2);

        shoppingService.removeProductFromCart("user1", "apple", 4);

        assertEquals(Map.of("banana", 2), shoppingService.viewCart("user1").getProducts());
    }

    @Test
    void emptyCartRemovesAllProducts() {
        ShoppingService shoppingService = new ShoppingService(new UserCartRepository());
        shoppingService.addProductToCart("user1", "apple", 4);
        shoppingService.addProductToCart("user1", "banana", 2);

        shoppingService.emptyCart("user1");

        assertEquals(new HashMap<>(), shoppingService.viewCart("user1").getProducts());
    }
}
