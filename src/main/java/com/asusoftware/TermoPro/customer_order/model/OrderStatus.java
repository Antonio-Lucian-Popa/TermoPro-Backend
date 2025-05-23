package com.asusoftware.TermoPro.customer_order.model;

public enum OrderStatus {
    PENDING,
    SCHEDULED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED;

    public static OrderStatus fromString(String status) {
        for (OrderStatus orderStatus : OrderStatus.values()) {
            if (orderStatus.name().equalsIgnoreCase(status)) {
                return orderStatus;
            }
        }
        throw new IllegalArgumentException("Unknown status: " + status);
    }
}
