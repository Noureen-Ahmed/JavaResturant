package Dao;

import classes.Employee;
import classes.Manager;
import classes.Cashier;
import classes.DeliveryMan;
import classes.Role;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmployeeDAO {

    // 1. Add Employee
    public boolean addEmployee(Employee employee) {
        String sql = "INSERT INTO employees (name, phone, username, password, role, is_available) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, employee.getName());
            pstmt.setString(2, employee.getPhone());
            pstmt.setString(3, employee.getUsername());
            pstmt.setString(4, employee.getPassword());
            pstmt.setString(5, employee.getRole().name());
            
            // Check if employee is DeliveryMan to set availability
            if (employee instanceof DeliveryMan) {
                pstmt.setBoolean(6, ((DeliveryMan) employee).isAvailable());
            } else {
                pstmt.setBoolean(6, true); // Default value for non-delivery staff
            }

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        employee.setId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error adding employee: " + e.getMessage());
        }
        return false;
    }

    // 2. Get All Employees
    public List<Employee> getAllEmployees() {
        List<Employee> list = new ArrayList<>();
        String sql = "SELECT * FROM employees";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapResultSetToEmployee(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching employees: " + e.getMessage());
        }
        return list;
    }

    // 3. Get Employee By ID
    public Employee getEmployeeById(int id) {
        String sql = "SELECT * FROM employees WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToEmployee(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching employee by ID: " + e.getMessage());
        }
        return null;
    }

    // 4. Update Employee
    public boolean updateEmployee(Employee employee) {
        String sql = "UPDATE employees SET name = ?, phone = ?, username = ?, password = ?, role = ?, is_available = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, employee.getName());
            pstmt.setString(2, employee.getPhone());
            pstmt.setString(3, employee.getUsername());
            pstmt.setString(4, employee.getPassword());
            pstmt.setString(5, employee.getRole().name());
            
            if (employee instanceof DeliveryMan) {
                pstmt.setBoolean(6, ((DeliveryMan) employee).isAvailable());
            } else {
                pstmt.setBoolean(6, true);
            }
            
            pstmt.setInt(7, employee.getId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating employee: " + e.getMessage());
        }
        return false;
    }

    // 5. Delete Employee
    public boolean deleteEmployee(int id) {
        String sql = "DELETE FROM employees WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting employee: " + e.getMessage());
        }
        return false;
    }

    // Helper Method: Factory Mapping
    private Employee mapResultSetToEmployee(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String name = rs.getString("name");
        String phone = rs.getString("phone");
        String username = rs.getString("username");
        String password = rs.getString("password");
        Role role = Role.valueOf(rs.getString("role"));
        boolean available = rs.getBoolean("is_available");

        Employee emp;
        switch (role) {
            case MANAGER:
                emp = new Manager(id, name, phone, username, password);
                break;
            case CASHIER:
                emp = new Cashier(id, name, phone, username, password);
                break;
            case DELIVERY_MAN:
                DeliveryMan deliveryMan = new DeliveryMan(id, name, phone, username, password);
                deliveryMan.setAvailable(available); // Set availability ONLY for DeliveryMan
                emp = deliveryMan;
                break;
            default:
                throw new IllegalArgumentException("Unknown Role: " + role);
        }
        return emp;
    }

}