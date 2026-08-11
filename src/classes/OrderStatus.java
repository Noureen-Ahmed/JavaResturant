package classes;

/**
 * OrderStatus enumeration representing the lifecycle states of an order.
 */
public enum OrderStatus {
    PENDING,
    PREPARING,
    OUT_FOR_DELIVERY,
    COMPLETED,
    CANCELLED
}