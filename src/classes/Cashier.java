package classes;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a Cashier in the Restaurant Management System.
 */
public class Cashier extends Employee {

    private final List<Order> orderRegistry;
    private final List<Reservation> reservationRegistry;
    private final List<Table> tableRegistry;
    private int nextInvoiceId;

    
    public Cashier() {
        super();
        this.setRole(Role.CASHIER);
        this.orderRegistry = new ArrayList<>();
        this.reservationRegistry = new ArrayList<>();
        this.tableRegistry = new ArrayList<>();
        this.nextInvoiceId = 1001;
    }

   
    public Cashier(String name, String phone, String username, String password) {
        super(name, phone, username, password, Role.CASHIER);
        this.orderRegistry = new ArrayList<>();
        this.reservationRegistry = new ArrayList<>();
        this.tableRegistry = new ArrayList<>();
        this.nextInvoiceId = 1001;
    }


    public Cashier(int id, String name, String phone, String username, String password) {
        super(id, name, phone, username, password, Role.CASHIER);
        this.orderRegistry = new ArrayList<>();
        this.reservationRegistry = new ArrayList<>();
        this.tableRegistry = new ArrayList<>();
        this.nextInvoiceId = 1001;
    }

    
    public Cashier(int id, String name, String phone, String username, String password,
                   List<Order> orderRegistry, List<Reservation> reservationRegistry, List<Table> tableRegistry) {
        super(id, name, phone, username, password, Role.CASHIER);
        this.orderRegistry = orderRegistry != null ? orderRegistry : new ArrayList<>();
        this.reservationRegistry = reservationRegistry != null ? reservationRegistry : new ArrayList<>();
        this.tableRegistry = tableRegistry != null ? tableRegistry : new ArrayList<>();
        this.nextInvoiceId = 1001;
    }

    private void checkAuthenticated() {
        if (!isLoggedIn()) {
            throw new IllegalStateException("Authentication required: Cashier must be logged in to perform operations.");
        }
    }

    public Order createOrder(Order order) {
        checkAuthenticated();
        if (order == null) {
            throw new IllegalArgumentException("Order cannot be null.");
        }
        order.setCashier(this);
        order.setStatus(OrderStatus.PENDING);
        if (!orderRegistry.contains(order)) {
            orderRegistry.add(order);
        }
        return order;
    }

    public void cancelOrder(int orderId) {
        checkAuthenticated();
        Order targetOrder = null;
        for (Order o : orderRegistry) {
            if (o != null && o.getOrderId() == orderId) {
                targetOrder = o;
                break;
            }
        }
        if (targetOrder == null) {
            throw new IllegalArgumentException("Order with ID " + orderId + " was not found.");
        }
       if (targetOrder.getStatus() == OrderStatus.COMPLETED || targetOrder.getStatus() == OrderStatus.CANCELLED) {
    throw new IllegalStateException("Cannot cancel order ID " + orderId + " because it is already " + targetOrder.getStatus());
}
        targetOrder.setStatus(OrderStatus.CANCELLED);
    }

    public void createReservation(Reservation res) {
        checkAuthenticated();
        if (res == null) {
            throw new IllegalArgumentException("Reservation cannot be null.");
        }
        int tableNumber = res.getTableNumber();
        Table table = null;
        for (Table t : tableRegistry) {
            if (t != null && t.getTableNumber() == tableNumber) {
                table = t;
                break;
            }
        }
        if (table == null) {
            throw new IllegalArgumentException("Table number " + tableNumber + " does not exist.");
        }
        if (!table.isAvailable()) {
            throw new IllegalStateException("Table number " + tableNumber + " is not available for reservation.");
        }
        table.setAvailable(false);
        reservationRegistry.add(res);
    }

  public Invoice generateInvoice(Order order) {
    checkAuthenticated();
    if (order == null) {
        throw new IllegalArgumentException("Order cannot be null.");
    }

    double calculatedTotal = order.calculateTotal(); 

    Invoice invoice = new Invoice(nextInvoiceId++, calculatedTotal);
    return invoice;
}

    public void registerTable(Table table) {
        if (table != null) {
            boolean found = false;
            for (int i = 0; i < tableRegistry.size(); i++) {
                Table t = tableRegistry.get(i);
                if (t != null && t.getTableNumber() == table.getTableNumber()) {
                    tableRegistry.set(i, table);
                    found = true;
                    break;
                }
            }
            if (!found) {
                tableRegistry.add(table);
            }
        }
    }

    public List<Order> getOrderRegistry() {
        return orderRegistry;
    }

    public List<Reservation> getReservationRegistry() {
        return reservationRegistry;
    }

    public List<Table> getTableRegistry() {
        return tableRegistry;
    }
}