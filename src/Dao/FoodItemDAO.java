package Dao;

import classes.FoodItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FoodItemDAO {

    // 1. Add Food Item
    public boolean addFoodItem(FoodItem item) {
        String sql = "INSERT INTO food_items (name, price, category, is_available) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, item.getName());
            pstmt.setDouble(2, item.getPrice());
            pstmt.setString(3, item.getCategory());
            pstmt.setBoolean(4, true); // Default available in DB

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

    // 2. Get All Food Items
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

    // 3. Get Food Item By ID
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

    // 4. Update Food Item
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

    // 5. Delete Food Item
    public boolean deleteFoodItem(int id) {
        String sql = "DELETE FROM food_items WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting food item: " + e.getMessage());
        }
        return false;
    }

    // Helper Method
    private FoodItem mapResultSetToFoodItem(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String name = rs.getString("name");
        double price = rs.getDouble("price");
        String category = rs.getString("category");

        return new FoodItem(id, name, price, category);
    }
}

