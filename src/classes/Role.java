package classes;

/**
 * Enumeration representing the specific roles an employee can hold in the Restaurant Management System.
 * 
 * <p>Purpose: Allows quick role identification (e.g. for access control checks, UI display,
 * or persistence) without relying on {@code instanceof} or reflection.</p>
 */
public enum Role {
    MANAGER,
    CASHIER,
    DELIVERY_MAN
}
