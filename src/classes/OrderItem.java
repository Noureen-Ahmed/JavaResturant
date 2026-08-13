package classes;

public class OrderItem {
    private FoodItem item;
    private int quantity;

    public OrderItem() {
        this.quantity = 1;
    }

    public OrderItem(FoodItem item, int quantity) {
        setItem(item);
        setQuantity(quantity);
    }

    public FoodItem getItem() {
        return item;
    }

    public void setItem(FoodItem item) {
        if (item == null) {
            throw new IllegalArgumentException("FoodItem cannot be null.");
        }
        this.item = item;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero.");
        }
        this.quantity = quantity;
    }


    public double getSubtotal() {
        return (item != null) ? item.getPrice() * quantity : 0.0;
    }

    @Override
    public String toString() {
        return "OrderItem{" +
                "item=" + (item != null ? item.getName() : "null") +
                ", quantity=" + quantity +
                ", subtotal=" + getSubtotal() +
                '}';
    }
}