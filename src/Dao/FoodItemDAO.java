package Dao;

import classes.FoodItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FoodItemDAO {

    public boolean addFoodItem(FoodItem item) {
        String sql = "INSERT INTO food_items (name, price, category, is_available) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, item.getName());
            pstmt.setDouble(2, item.getPrice());
            pstmt.setString(3, item.getCategory());
            pstmt.setBoolean(4, true); 

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        item.setId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error adding food item: " + e.getMessage());
        }
        return false;
    }

    public List<FoodItem> getAllFoodItems() {
        List<FoodItem> list = new ArrayList<>();
        String sql = "SELECT * FROM food_items";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapResultSetToFoodItem(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching food items: " + e.getMessage());
        }
        return list;
    }

    public FoodItem getFoodItemById(int id) {
        String sql = "SELECT * FROM food_items WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToFoodItem(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching food item by ID: " + e.getMessage());
        }
        return null;
    }

    public boolean updateFoodItem(FoodItem item) {
        String sql = "UPDATE food_items SET name = ?, price = ?, category = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, item.getName());
            pstmt.setDouble(2, item.getPrice());
            pstmt.setString(3, item.getCategory());
            pstmt.setInt(4, item.getId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating food item: " + e.getMessage());
        }
        return false;
    }

    public boolean deleteFoodItem(int id) {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement pstmt = conn.prepareStatement("DELETE FROM food_items WHERE id = ?")) {
                pstmt.setInt(1, id);
                if (pstmt.executeUpdate() == 0) {
                    conn.rollback();
                    return false;
                }
            }

            renumberFoodItems(conn);
            conn.commit();
            return true;
        } catch (SQLException e) {
            System.err.println("Error deleting food item: " + e.getMessage());
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

    private void renumberFoodItems(Connection conn) throws SQLException {
        List<Integer> oldIds = new ArrayList<>();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id FROM food_items ORDER BY id")) {
            while (rs.next()) oldIds.add(rs.getInt(1));
        }
        if (oldIds.isEmpty()) return;

        try (Statement stmt = conn.createStatement()) {
            stmt.execute("SET FOREIGN_KEY_CHECKS = 0");
        }

        int newId = 1;
        for (int oldId : oldIds) {
            try (PreparedStatement ps = conn.prepareStatement("UPDATE order_items SET food_item_id = ? WHERE food_item_id = ?")) {
                ps.setInt(1, -newId);
                ps.setInt(2, oldId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("UPDATE food_items SET id = ? WHERE id = ?")) {
                ps.setInt(1, newId);
                ps.setInt(2, oldId);
                ps.executeUpdate();
            }
            newId++;
        }

        try (Statement stmt = conn.createStatement()) {
            stmt.execute("UPDATE order_items SET food_item_id = ABS(food_item_id)");
            stmt.execute("ALTER TABLE food_items AUTO_INCREMENT = 1");
            stmt.execute("SET FOREIGN_KEY_CHECKS = 1");
        }
    }

    public boolean isNameExists(String name, int excludeId) {
        String sql = "SELECT 1 FROM food_items WHERE LOWER(name) = LOWER(?) AND id != ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setInt(2, excludeId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Error checking food item name existence: " + e.getMessage());
        }
        return false;
    }

    private FoodItem mapResultSetToFoodItem(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String name = rs.getString("name");
        double price = rs.getDouble("price");
        String category = rs.getString("category");

        return new FoodItem(id, name, price, category);
    }
}

