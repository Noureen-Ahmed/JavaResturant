package main;

/**
 * @author Filo
 */
public class DeliveryMan extends Employee {

    private boolean isAvailable;

  

    
    public DeliveryMan() {
        super();
        this.setRole(Role.DELIVERY_MAN);
        this.isAvailable = true;
    }

    
    public DeliveryMan(String name, String phone, String username, String password) {
        super(name, phone, username, password, Role.DELIVERY_MAN);
        this.isAvailable = true;
    }

    
    public DeliveryMan(int id, String name, String phone, String username, String password, boolean isAvailable) {
        super(id, name, phone, username, password, Role.DELIVERY_MAN, isAvailable);
        this.isAvailable = isAvailable;
    }

    
    public void updateOrderStatus(Order order, OrderStatus status) {
        
        if (order != null) {
            order.setStatus(status);
        }
    }

    public boolean getIsAvailable() {
        return isAvailable;
    }
     public void setIsAvailable(boolean isAvailable) {
        this.isAvailable = isAvailable;
    }
}