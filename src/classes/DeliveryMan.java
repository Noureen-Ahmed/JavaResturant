package classes;

/**
 * Represents a DeliveryMan in the Restaurant Management System.
 */
public class DeliveryMan extends Employee {

    private boolean isAvailable;


    public DeliveryMan() {
        super();
        this.setRole(Role.DELIVERY);
        this.isAvailable = true;
    }

   
    public DeliveryMan(String name, String phone, String username, String password) {
        super(name, phone, username, password, Role.DELIVERY);
        this.isAvailable = true;
    }

  
    public DeliveryMan(int id, String name, String phone, String username, String password) {
        super(id, name, phone, username, password, Role.DELIVERY);
        this.isAvailable = true;
    }

    public DeliveryMan(int id, String name, String phone, String username, String password, boolean isAvailable) {
        super(id, name, phone, username, password, Role.DELIVERY);
        this.isAvailable = isAvailable;
    }

  public void updateOrderStatus(Order order, OrderStatus status) {
    if (!isLoggedIn()) {
        throw new IllegalStateException("DeliveryMan must be logged in to update order status.");
    }
    if (order == null) {
        throw new IllegalArgumentException("Order cannot be null.");
    }
    order.setStatus(status);
}

   
    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }
}