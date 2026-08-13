package Dao;

import classes.Table;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TableDAO {

    public boolean addTable(Table table) {
        String sql = "INSERT INTO restaurant_tables (table_number, capacity, is_available) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, table.getTableNumber());
            pstmt.setInt(2, table.getCapacity()); 
            pstmt.setBoolean(3, table.isAvailable());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error adding table: " + e.getMessage());
        }
        return false;
    }

    public List<Table> getAllTables() {
        List<Table> list = new ArrayList<>();
        String sql = "SELECT * FROM restaurant_tables";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapResultSetToTable(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching tables: " + e.getMessage());
        }
        return list;
    }

    public Table getTableByNumber(int tableNumber) {
        String sql = "SELECT * FROM restaurant_tables WHERE table_number = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, tableNumber);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToTable(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching table: " + e.getMessage());
        }
        return null;
    }

    public boolean updateTableStatus(Table table) {
        String sql = "UPDATE restaurant_tables SET is_available = ? WHERE table_number = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setBoolean(1, table.isAvailable());
            pstmt.setInt(2, table.getTableNumber());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating table: " + e.getMessage());
        }
        return false;
    }

    public boolean deleteTable(int tableNumber) {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement pstmt = conn.prepareStatement("DELETE FROM restaurant_tables WHERE table_number = ?")) {
                pstmt.setInt(1, tableNumber);
                if (pstmt.executeUpdate() == 0) {
                    conn.rollback();
                    return false;
                }
            }

            renumberTables(conn);
            conn.commit();
            return true;
        } catch (SQLException e) {
            System.err.println("Error deleting table: " + e.getMessage());
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

    private void renumberTables(Connection conn) throws SQLException {
        List<Integer> oldNumbers = new ArrayList<>();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT table_number FROM restaurant_tables ORDER BY table_number")) {
            while (rs.next()) oldNumbers.add(rs.getInt(1));
        }
        if (oldNumbers.isEmpty()) return;

        try (Statement stmt = conn.createStatement()) {
            stmt.execute("SET FOREIGN_KEY_CHECKS = 0");
        }

        int newNumber = 1;
        for (int oldNumber : oldNumbers) {
            try (PreparedStatement ps = conn.prepareStatement("UPDATE orders SET table_number = ? WHERE table_number = ?")) {
                ps.setInt(1, -newNumber);
                ps.setInt(2, oldNumber);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("UPDATE reservations SET table_number = ? WHERE table_number = ?")) {
                ps.setInt(1, -newNumber);
                ps.setInt(2, oldNumber);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("UPDATE restaurant_tables SET table_number = ? WHERE table_number = ?")) {
                ps.setInt(1, newNumber);
                ps.setInt(2, oldNumber);
                ps.executeUpdate();
            }
            newNumber++;
        }

        try (Statement stmt = conn.createStatement()) {
            stmt.execute("UPDATE orders SET table_number = ABS(table_number)");
            stmt.execute("UPDATE reservations SET table_number = ABS(table_number)");
            stmt.execute("SET FOREIGN_KEY_CHECKS = 1");
        }
    }

    private Table mapResultSetToTable(ResultSet rs) throws SQLException {
        int tableNumber = rs.getInt("table_number");
        int capacity = rs.getInt("capacity");
        boolean available = rs.getBoolean("is_available");

        return new Table(tableNumber, capacity, available); 
    }
}