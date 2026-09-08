package com.example;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class HelloWorldTest {

    @Test
    void greetingReturnsHelloWorld() {
        // Arrange
        HelloWorld helloWorld = new HelloWorld();

        // Act
        String message = helloWorld.greeting();

        // Assert
        assertEquals("Hello, World!", message);
    }
}
