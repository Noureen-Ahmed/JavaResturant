package Dao;

import classes.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO {

    public boolean createOrder(Order order) {
        String sqlOrder = "INSERT INTO orders (cashier_id, order_type, status, total_amount, " +
                "customer_name, customer_phone, delivery_address, delivery_fee, table_number) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        String sqlItem = "INSERT INTO order_items (order_id, food_item_id, quantity) VALUES (?, ?, ?)";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); 

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

                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        order.setOrderId(rs.getInt(1));
                    } else {
                        conn.rollback();
                        return false;
                    }
                }
            }

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

            conn.commit(); 
            return true;

        } catch (SQLException e) {
            System.err.println("Error creating order: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback(); 
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

    public List<Order> getAllOrders() {
        List<Order> list = new ArrayList<>();
        String sql = "SELECT * FROM orders ORDER BY order_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToOrder(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all orders: " + e.getMessage());
        }
        return list;
    }

    public boolean updateOrderStatus(int orderId, OrderStatus status) {
        String sql = "UPDATE orders SET status = ? WHERE order_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status.name());
            pstmt.setInt(2, orderId);

            boolean success = pstmt.executeUpdate() > 0;
            if (success) {
                renumberOrders(conn);
            }
            return success;
        } catch (SQLException e) {
            System.err.println("Error updating order status: " + e.getMessage());
        }
        return false;
    }

    public boolean isPhoneExists(String phone) {
        if (phone == null || phone.trim().isEmpty()) return false;
        String sql = "SELECT 1 FROM orders WHERE customer_phone = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, phone.trim());
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Error checking order phone existence: " + e.getMessage());
        }
        return false;
    }

    public List<Order> getDeliveriesByDriverName(String driverName) {
        List<Order> list = new ArrayList<>();
        String searchStr = "%Driver: " + driverName + "%";
        String sql = "SELECT * FROM orders WHERE delivery_address LIKE ? AND status != 'COMPLETED'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
            pstmt.setString(1, searchStr);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToOrder(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching deliveries: " + e.getMessage());
        }
        return list;
    }

    public boolean deleteOrder(int orderId) {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement pstmtItems = conn.prepareStatement("DELETE FROM order_items WHERE order_id = ?")) {
                pstmtItems.setInt(1, orderId);
                pstmtItems.executeUpdate();
            }
            try (PreparedStatement pstmtOrder = conn.prepareStatement("DELETE FROM orders WHERE order_id = ?")) {
                pstmtOrder.setInt(1, orderId);
                if (pstmtOrder.executeUpdate() == 0) {
                    conn.rollback();
                    return false;
                }
            }

            renumberOrders(conn);
            conn.commit();
            return true;
        } catch (SQLException e) {
            System.err.println("Error deleting order: " + e.getMessage());
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
            }
        }
        return false;
    }

    private void renumberOrders(Connection conn) throws SQLException {
        List<Integer> oldIds = new ArrayList<>();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT order_id FROM orders ORDER BY order_id")) {
            while (rs.next()) oldIds.add(rs.getInt(1));
        }
        if (oldIds.isEmpty()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("ALTER TABLE orders AUTO_INCREMENT = 1");
            }
            return;
        }

        try (Statement stmt = conn.createStatement()) {
            stmt.execute("SET FOREIGN_KEY_CHECKS = 0");
        }

        int newId = 1;
        for (int oldId : oldIds) {
            try (PreparedStatement ps = conn.prepareStatement("UPDATE order_items SET order_id = ? WHERE order_id = ?")) {
                ps.setInt(1, -newId);
                ps.setInt(2, oldId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("UPDATE invoices SET order_id = ? WHERE order_id = ?")) {
                ps.setInt(1, -newId);
                ps.setInt(2, oldId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("UPDATE orders SET order_id = ? WHERE order_id = ?")) {
                ps.setInt(1, newId);
                ps.setInt(2, oldId);
                ps.executeUpdate();
            }
            newId++;
        }

        try (Statement stmt = conn.createStatement()) {
            stmt.execute("UPDATE order_items SET order_id = ABS(order_id)");
            stmt.execute("UPDATE invoices SET order_id = ABS(order_id)");
            stmt.execute("ALTER TABLE orders AUTO_INCREMENT = 1");
            stmt.execute("SET FOREIGN_KEY_CHECKS = 1");
        }
    }

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