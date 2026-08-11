package classes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a Manager in the Restaurant Management System.
 */
public class Manager extends Employee {

    private final List<Employee> employeeRegistry;
    private final List<FoodItem> menuCatalog;
    private final List<Order> salesHistory;

    // 1. Empty Constructor
    public Manager() {
        super();
        this.setRole(Role.MANAGER);
        this.employeeRegistry = new ArrayList<>();
        this.menuCatalog = new ArrayList<>();
        this.salesHistory = new ArrayList<>();
    }

    // 2. Constructor لإنشاء مدير جديد (من غير ID عشان الـ GUI)
    public Manager(String name, String phone, String username, String password) {
        super(name, phone, username, password, Role.MANAGER);
        this.employeeRegistry = new ArrayList<>();
        this.menuCatalog = new ArrayList<>();
        this.salesHistory = new ArrayList<>();
    }

    // 3. Constructor بالـ ID (للداتا بيز)
    public Manager(int id, String name, String phone, String username, String password) {
        super(id, name, phone, username, password, Role.MANAGER);
        this.employeeRegistry = new ArrayList<>();
        this.menuCatalog = new ArrayList<>();
        this.salesHistory = new ArrayList<>();
    }

    // 4. Constructor كامل بالقوائم جاهزة
    public Manager(int id, String name, String phone, String username, String password,
                   List<Employee> employeeRegistry, List<FoodItem> menuCatalog, List<Order> salesHistory) {
        super(id, name, phone, username, password, Role.MANAGER);
        this.employeeRegistry = employeeRegistry != null ? employeeRegistry : new ArrayList<>();
        this.menuCatalog = menuCatalog != null ? menuCatalog : new ArrayList<>();
        this.salesHistory = salesHistory != null ? salesHistory : new ArrayList<>();
    }

    private void checkAuthenticated() {
        if (!isLoggedIn()) {
            throw new IllegalStateException("Authentication required: Manager must be logged in to perform privileged operations.");
        }
    }

    public void addEmployee(Employee emp) {
        checkAuthenticated();
        if (emp == null) {
            throw new IllegalArgumentException("Employee to add cannot be null.");
        }
        for (Employee existing : employeeRegistry) {
           if (existing != null) {
            // فحص تكرار الـ ID
            if (existing.getId() == emp.getId() && emp.getId() > 0) {
                throw new IllegalArgumentException("Duplicate employee ID: " + emp.getId() + " already exists.");
            }
            
            // فحص تكرار الـ Username مع الحماية من الـ null
            if (existing.getUsername() != null 
                    && emp.getUsername() != null 
                    && existing.getUsername().equalsIgnoreCase(emp.getUsername())) {
                throw new IllegalArgumentException("Duplicate username: '" + emp.getUsername() + "' already exists.");
            }
        }
        }
        employeeRegistry.add(emp);
    }

    public void deleteEmployee(int id) {
        checkAuthenticated();
        Employee toRemove = null;
        for (Employee emp : employeeRegistry) {
            if (emp.getId() == id) {
                toRemove = emp;
                break;
            }
        }
        if (toRemove == null) {
            throw new IllegalArgumentException("Cannot delete: Employee with ID " + id + " does not exist in registry.");
        }
        employeeRegistry.remove(toRemove);
    }

    public void updateMenuItem(FoodItem item) {
        checkAuthenticated();
        if (item == null) {
            throw new IllegalArgumentException("FoodItem to update cannot be null.");
        }
        FoodItem existingItem = null;
        for (FoodItem existing : menuCatalog) {
            if (existing.getId() == item.getId()) {
                existingItem = existing;
                break;
            }
        }
        if (existingItem == null) {
            throw new IllegalArgumentException("Cannot update: FoodItem with ID " + item.getId() + " was not found in the menu catalog.");
        }
        existingItem.setName(item.getName());
        existingItem.setPrice(item.getPrice());
        existingItem.setCategory(item.getCategory());
    }

    public double getSalesReport() {
        checkAuthenticated();
        return getSalesReport(this.salesHistory);
    }

    public double getSalesReport(List<Order> orders) {
        checkAuthenticated();
        if (orders == null) {
            return 0.0;
        }
        double totalSales = 0.0;
        for (Order order : orders) {
            if (order != null && order.getStatus() == OrderStatus.COMPLETED) {
                totalSales += order.getTotalAmount();
            }
        }
        return totalSales;
    }

    // Getters and Setters
    public List<Employee> getEmployeeRegistry() {
        return employeeRegistry;
    }

    public List<FoodItem> getMenuCatalog() {
        return menuCatalog;
    }

    public List<Order> getSalesHistory() {
        return salesHistory;
    }

    public void addMenuItem(FoodItem item) {
        if (item != null && !menuCatalog.contains(item)) {
            menuCatalog.add(item);
        }
    }

    public void recordOrder(Order order) {
        if (order != null) {
            salesHistory.add(order);
        }
    }
}