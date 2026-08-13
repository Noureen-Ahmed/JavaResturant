package classes;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Order {
    private int orderId;
    private Date orderDate;
    private Employee cashier;
    private List<OrderItem> items;
    private OrderType orderType;
    private OrderStatus status;
    private double totalAmount;
    private String customerName;
    private String customerPhone;
    private String deliveryAddress;
    private double deliveryFee;
    private int tableNumber;

    public Order() {
        this.items = new ArrayList<>();
        this.orderDate = new Date();
        this.status = OrderStatus.PENDING;
        this.orderType = OrderType.DINE_IN;
        this.totalAmount = 0.0;
        this.deliveryFee = 0.0;
        this.tableNumber = 0;
    }

    public Order(OrderType orderType, Employee cashier) {
        this();
        setOrderType(orderType);
        this.cashier = cashier;
    }

    public Order(int orderId, Date orderDate, OrderType orderType, OrderStatus status) {
        this();
        setOrderId(orderId);
        setOrderDate(orderDate);
        setOrderType(orderType);
        setStatus(status);
    }

    public void addItem(FoodItem item, int quantity) {
        if (item == null) {
            throw new IllegalArgumentException("Food item cannot be null.");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero.");
        }

        for (OrderItem orderItem : items) {
            if (orderItem != null && orderItem.getItem() != null) {
                if (orderItem.getItem().getId() > 0 && orderItem.getItem().getId() == item.getId()) {
                    orderItem.setQuantity(orderItem.getQuantity() + quantity);
                    calculateTotal();
                    return;
                }
                if (orderItem.getItem().getName() != null && item.getName() != null
                        && orderItem.getItem().getName().equalsIgnoreCase(item.getName())) {
                    orderItem.setQuantity(orderItem.getQuantity() + quantity);
                    calculateTotal();
                    return;
                }
            }
        }

        items.add(new OrderItem(item, quantity));
        calculateTotal();
    }

    public void addItem(OrderItem orderItem) {
        if (orderItem != null && orderItem.getItem() != null) {
            addItem(orderItem.getItem(), orderItem.getQuantity());
        }
    }

    public void removeItem(FoodItem item) {
        if (item == null) {
            throw new IllegalArgumentException("Food item cannot be null.");
        }

        boolean removed = items.removeIf(orderItem -> orderItem != null && orderItem.getItem() != null
                && ((orderItem.getItem().getId() > 0 && orderItem.getItem().getId() == item.getId()) ||
                        (orderItem.getItem().getName() != null && item.getName() != null
                                && orderItem.getItem().getName().equalsIgnoreCase(item.getName()))));

        if (!removed) {
            throw new IllegalArgumentException("Food item is not in this order.");
        }

        calculateTotal();
    }

    public double calculateTotal() {
        double sum = 0.0;
        if (items != null) {
            for (OrderItem oi : items) {
                if (oi != null) {
                    sum += oi.getSubtotal();
                }
            }
        }
        if (orderType == OrderType.DELIVERY) {
            sum += deliveryFee;
        }
        this.totalAmount = sum;
        return this.totalAmount;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        if (orderId < 0) {
            throw new IllegalArgumentException("Order ID cannot be negative.");
        }
        this.orderId = orderId;
    }

    public Date getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate != null ? orderDate : new Date();
    }

    public Employee getCashier() {
        return cashier;
    }

    public void setCashier(Employee cashier) {
        this.cashier = cashier;
    }

    public List<OrderItem> getItems() {
        return new ArrayList<>(items);
    }

    public void setItems(List<OrderItem> items) {
        this.items = items != null ? new ArrayList<>(items) : new ArrayList<>();
        calculateTotal();
    }

    public OrderType getOrderType() {
        return orderType;
    }

    public void setOrderType(OrderType orderType) {
        if (orderType == null) {
            throw new IllegalArgumentException("Order type cannot be null.");
        }
        this.orderType = orderType;
        calculateTotal();
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Order status cannot be null.");
        }
        this.status = status;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        if (totalAmount < 0) {
            throw new IllegalArgumentException("Total amount cannot be negative.");
        }
        this.totalAmount = totalAmount;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public double getDeliveryFee() {
        return deliveryFee;
    }

    public void setDeliveryFee(double deliveryFee) {
        if (deliveryFee < 0) {
            throw new IllegalArgumentException("Delivery fee cannot be negative.");
        }
        this.deliveryFee = deliveryFee;
        calculateTotal();
    }

    public int getTableNumber() {
        return tableNumber;
    }

    public void setTableNumber(int tableNumber) {
        if (tableNumber < 0) {
            throw new IllegalArgumentException("Table number cannot be negative.");
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