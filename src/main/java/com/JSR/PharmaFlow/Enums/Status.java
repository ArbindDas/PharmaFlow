package com.JSR.PharmaFlow.Enums;

public enum Status {
//    PLACED , APPROVED , SHIPPED , DELIVERD

    PLACED,       // Order placed
    APPROVED,     // Order approved
    SHIPPED,      // Order shipped
    DELIVERED,    // Order delivered
    CANCELLED,    // Order cancelled
    PENDING;     // Order returned
    // Optional: Add description for each status
    public String getDescription() {
        return switch (this) {
            case PLACED -> "Order has been placed";
            case APPROVED -> "Order has been approved";
            case SHIPPED -> "Order has been shipped";
            case DELIVERED -> "Order has been delivered";
            case CANCELLED -> "Order has been cancelled";
            case PENDING -> "Order has been pending";
        };
    }
}
