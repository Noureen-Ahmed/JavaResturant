import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Order {
    private int orderId;
    private Date orderDate;
    // private Cashier cashier;
    private List<OrderItem> items;
    private OrderType orderType;
    private OrderStatus status;
    private double totalAmount;
    private String customerName;
    private String customerPhone;
    private String deliveryAddress;
    private double deliveryFee;
    private int tableNumber;

    public Order(int orderId, Date orderDate, // Cashier cashier,
            OrderType orderType, String customerName,
            String customerPhone) {

        if (orderId <= 0) {
            throw new IllegalArgumentException("Order ID must be positive");
        }

        if (orderDate == null) {
            throw new IllegalArgumentException(
                    "Order date cannot be null");
        }

        if (orderType == null) {
            throw new IllegalArgumentException(
                    "Order type cannot be null");
        }

        this.orderId = orderId;
        this.orderDate = orderDate;
        // this.cashier = cashier;
        this.orderType = orderType;
        this.customerName = customerName;
        this.customerPhone = customerPhone;

        this.items = new ArrayList<>();
        this.status = OrderStatus.PENDING;
        this.totalAmount = 0.0;
        this.deliveryFee = 0.0;
        this.tableNumber = 0;
    }

    public void addItem(FoodItem item, int quantity) {
        if (item == null) {
            throw new IllegalArgumentException(
                    "Food item cannot be null");
        }

        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be greater than zero");
        }

        // If the item already exists in the order,
        // increase its quantity instead of adding a duplicate OrderItem.
        for (OrderItem orderItem : items) {
            if (orderItem.getItem().getId() == item.getId()) {
                orderItem.setQuantity(
                        orderItem.getQuantity() + quantity);
                calculateTotal();
                return;
            }
        }

        items.add(new OrderItem(item, quantity));
        calculateTotal();
    }

    public void removeItem(FoodItem item) {
        if (item == null) {
            throw new IllegalArgumentException(
                    "Food item cannot be null");
        }

        boolean removed = items.removeIf(
                orderItem -> orderItem.getItem().getId() == item.getId());

        if (!removed) {
            throw new IllegalArgumentException(
                    "Food item is not in this order");
        }

        calculateTotal();
    }

    public double calculateTotal() {
        double total = 0.0;

        for (OrderItem item : items) {
            total += item.getSubtotal();
        }

        if (orderType == OrderType.DELIVERY) {
            total += deliveryFee;
        }

        totalAmount = total;
        return totalAmount;
    }

    public void setStatus(OrderStatus status) {
        if (status == null) {
            throw new IllegalArgumentException(
                    "Order status cannot be null");
        }

        this.status = status;
    }

    public int getOrderId() {
        return orderId;
    }

    public Date getOrderDate() {
        return orderDate;
    }

    /*
     * public Cashier getCashier() {
     * return cashier;
     * }
     */

    public List<OrderItem> getItems() {
        return new ArrayList<>(items);
    }

    public OrderType getOrderType() {
        return orderType;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public double getDeliveryFee() {
        return deliveryFee;
    }

    public int getTableNumber() {
        return tableNumber;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public void setDeliveryFee(double deliveryFee) {
        if (deliveryFee < 0) {
            throw new IllegalArgumentException(
                    "Delivery fee cannot be negative");
        }

        this.deliveryFee = deliveryFee;
        calculateTotal();
    }

    public void setTableNumber(int tableNumber) {
        if (tableNumber <= 0) {
            throw new IllegalArgumentException(
                    "Table number must be positive");
        }

        this.tableNumber = tableNumber;
    }

    @Override
    public String toString() {
        return "Order{" +
                "orderId=" + orderId +
                ", orderDate=" + orderDate +
                ", orderType=" + orderType +
                ", status=" + status +
                ", totalAmount=" + totalAmount +
                ", customerName='" + customerName + '\'' +
                ", customerPhone='" + customerPhone + '\'' +
                '}';
    }
}