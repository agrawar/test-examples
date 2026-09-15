package com.example;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.example.model.Cart;
import com.example.repository.UserCartRepository;
import com.example.service.ShoppingService;

class HelloWorldTest {


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
}
