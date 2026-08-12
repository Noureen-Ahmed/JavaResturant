package Dao;

import classes.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO {

    // 1. Create Order with Items (Transaction)
    public boolean createOrder(Order order) {
        String sqlOrder = "INSERT INTO orders (cashier_id, order_type, status, total_amount, " +
                "customer_name, customer_phone, delivery_address, delivery_fee, table_number) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        String sqlItem = "INSERT INTO order_items (order_id, food_item_id, quantity) VALUES (?, ?, ?)";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Start Transaction

            // Insert into orders table
            try (PreparedStatement pstmt = conn.prepareStatement(sqlOrder, Statement.RETURN_GENERATED_KEYS)) {
                if (order.getCashier() != null) {
                    pstmt.setInt(1, order.getCashier().getId());
                } else {
                    pstmt.setNull(1, Types.INTEGER);
                }

                pstmt.setString(2, order.getOrderType().name());
                pstmt.setString(3, order.getStatus().name());
                pstmt.setDouble(4, order.calculateTotal());
                pstmt.setString(5, order.getCustomerName());
                pstmt.setString(6, order.getCustomerPhone());
                pstmt.setString(7, order.getDeliveryAddress());
                pstmt.setDouble(8, order.getDeliveryFee());

                if (order.getTableNumber() > 0) {
                    pstmt.setInt(9, order.getTableNumber());
                } else {
                    pstmt.setNull(9, Types.INTEGER);
                }

                int affectedRows = pstmt.executeUpdate();
                if (affectedRows == 0) {
                    conn.rollback();
                    return false;
                }

                // Get Generated Order ID
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        order.setOrderId(rs.getInt(1));
                    } else {
                        conn.rollback();
                        return false;
                    }
                }
            }

            // Insert Items into order_items table
            if (order.getItems() != null && !order.getItems().isEmpty()) {
                try (PreparedStatement pstmtItem = conn.prepareStatement(sqlItem)) {
                    for (OrderItem item : order.getItems()) {
                        pstmtItem.setInt(1, order.getOrderId());
                        pstmtItem.setInt(2, item.getItem().getId());
                        pstmtItem.setInt(3, item.getQuantity());
                        pstmtItem.addBatch();
                    }
                    pstmtItem.executeBatch();
                }
            }

            conn.commit(); // Save changes permanently
            return true;

        } catch (SQLException e) {
            System.err.println("Error creating order: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback(); // Cancel transaction on error
                } catch (SQLException ex) {
                    System.err.println("Rollback error: " + ex.getMessage());
                }
            }
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    System.err.println("Reset autocommit error: " + e.getMessage());
                }
            }
        }
        return false;
    }

    // 2. Get Order By ID
    public Order getOrderById(int orderId) {
        String sql = "SELECT * FROM orders WHERE order_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, orderId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Order order = mapResultSetToOrder(rs);
                    order.setItems(getOrderItems(orderId));
                    return order;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching order by ID: " + e.getMessage());
        }
        return null;
    }

    // 3. Update Order Status
    public boolean updateOrderStatus(int orderId, OrderStatus status) {
        String sql = "UPDATE orders SET status = ? WHERE order_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status.name());
            pstmt.setInt(2, orderId);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating order status: " + e.getMessage());
        }
        return false;
    }

    // Helper Method: Load Order Items
    private List<OrderItem> getOrderItems(int orderId) {
        List<OrderItem> list = new ArrayList<>();
        String sql = "SELECT oi.quantity, f.id, f.name, f.price, f.category " +
                     "FROM order_items oi " +
                     "JOIN food_items f ON oi.food_item_id = f.id " +
                     "WHERE oi.order_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, orderId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    FoodItem item = new FoodItem(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getDouble("price"),
                            rs.getString("category")
                    );
                    int quantity = rs.getInt("quantity");
                    list.add(new OrderItem(item, quantity));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching order items: " + e.getMessage());
        }
        return list;
    }

    // Helper Method: Map Order ResultSet
    private Order mapResultSetToOrder(ResultSet rs) throws SQLException {
        int orderId = rs.getInt("order_id");
        Timestamp orderDate = rs.getTimestamp("order_date");
        OrderType type = OrderType.valueOf(rs.getString("order_type"));
        OrderStatus status = OrderStatus.valueOf(rs.getString("status"));

        Order order = new Order(orderId, orderDate, type, status);
        order.setCustomerName(rs.getString("customer_name"));
        order.setCustomerPhone(rs.getString("customer_phone"));
        order.setDeliveryAddress(rs.getString("delivery_address"));
        order.setDeliveryFee(rs.getDouble("delivery_fee"));
        order.setTableNumber(rs.getInt("table_number"));
        order.setTotalAmount(rs.getDouble("total_amount"));

        return order;
    }
}