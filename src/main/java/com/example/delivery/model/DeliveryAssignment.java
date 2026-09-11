package com.example.delivery.model;

import java.util.Objects;

public class DeliveryAssignment {
    private final Order order;
    private final DeliveryDriver driver;

    public DeliveryAssignment(Order order, DeliveryDriver driver) {
        this.order = Objects.requireNonNull(order, "order");
        this.driver = Objects.requireNonNull(driver, "driver");
    }

    public Order getOrder() {
        return order;
    }

    public DeliveryDriver getDriver() {
        return driver;
    }
}
