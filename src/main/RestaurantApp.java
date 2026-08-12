package main;

import Dao.*;
import classes.*;

import java.util.List;
import java.util.Scanner;

public class RestaurantApp {

    private static final Scanner scanner = new Scanner(System.in);
    private static final EmployeeDAO employeeDao = new EmployeeDAO();
    private static final FoodItemDAO foodItemDao = new FoodItemDAO();
    private static final TableDAO tableDao = new TableDAO();
    private static final OrderDAO orderDao = new OrderDAO();
    private static final InvoiceDAO invoiceDao = new InvoiceDAO();
    private static final ReservationDAO reservationDao = new ReservationDAO();

    private static Employee currentUser = null;

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("       WELCOME TO RESTAURANT MANAGEMENT SYSTEM    ");
        System.out.println("==================================================");

        // 1. Loop until successful login or explicit exit
        while (currentUser == null) {
            if (!login()) {
                System.out.println("1. Try Login Again");
                System.out.println("2. Exit System");
                int exitChoice = getIntInput("Choice: ");
                if (exitChoice == 2) {
                    System.out.println("System Closed.");
                    return;
                }
            }
        }

        // 2. Main System Loop
        boolean running = true;
        while (running) {
            showMainMenu();
            int choice = getIntInput("Choose an option: ");
            switch (choice) {
                case 1 -> displayMenu();
                case 2 -> createNewOrder();
                case 3 -> showTablesAndReservations();
                case 4 -> {
                    if (isManager()) {
                        managerPortal();
                    } else {
                        System.out.println("\n[ACCESS DENIED] Only Managers can access the Admin Portal!");
                    }
                }
                case 5 -> {
                    System.out.println("\nLogging out... Goodbye, " + currentUser.getName() + "!");
                    currentUser = null;
                    running = false;
                }
                default -> System.out.println("Invalid choice. Try again.");
            }
        }
    }

    private static boolean isManager() {
        return currentUser != null && "MANAGER".equalsIgnoreCase(currentUser.getRole().name());
    }

    // --- Login System ---
    private static boolean login() {
        System.out.println("\n--- LOGIN ---");
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();

        List<Employee> employees = employeeDao.getAllEmployees();
        for (Employee emp : employees) {
            if (emp.getUsername().equalsIgnoreCase(username) && emp.getPassword().equals(password)) {
                currentUser = emp;
                System.out.println("\nLogin Successful! Welcome, " + emp.getName() + " (" + emp.getRole() + ")");
                return true;
            }
        }
        System.out.println("Invalid username or password!");
        return false;
    }

    // --- Main Menu ---
    private static void showMainMenu() {
        System.out.println("\n==================================================");
        System.out.println("                   MAIN MENU                      ");
        System.out.println("==================================================");
        System.out.println("1. View Food Menu");
        System.out.println("2. Create New Order & Print Invoice");
        System.out.println("3. View Tables & Reservations");
        System.out.println("4. Admin / Manager Portal " + (isManager() ? "[FULL ACCESS]" : "[RESTRICTED]"));
        System.out.println("5. Exit / Logout");
        System.out.println("==================================================");
    }

    // --- Display Food Menu ---
    private static void displayMenu() {
        System.out.println("\n--- RESTAURANT MENU ---");
        List<FoodItem> items = foodItemDao.getAllFoodItems();
        if (items.isEmpty()) {
            System.out.println("No food items found in the menu.");
            return;
        }
        for (FoodItem item : items) {
            System.out.printf("ID: %-3d | %-25s | %-12s | Price: $%.2f%n",
                    item.getId(), item.getName(), item.getCategory(), item.getPrice());
        }
    }

    // --- Interactive Order Creation & Table Automation ---
    private static void createNewOrder() {
        System.out.println("\n--- CREATE NEW ORDER ---");
        System.out.println("Select Order Type: 1. DINE_IN  2. TAKEAWAY  3. DELIVERY");
        int typeChoice = getIntInput("Choice: ");
        OrderType type = switch (typeChoice) {
            case 2 -> OrderType.TAKEAWAY;
            case 3 -> OrderType.DELIVERY;
            default -> OrderType.DINE_IN;
        };

        Order order = new Order(type, currentUser);
        Table selectedTable = null;

        // Validation for Dine-In Table
        if (type == OrderType.DINE_IN) {
            int tableNum = getIntInput("Enter Table Number: ");
            selectedTable = tableDao.getTableByNumber(tableNum);
            
            if (selectedTable == null) {
                System.out.println("Error: Table #" + tableNum + " does not exist in system!");
                return;
            }
            if (!selectedTable.isAvailable()) {
                System.out.println("Error: Table #" + tableNum + " is currently BUSY!");
                return;
            }
            order.setTableNumber(tableNum);
        } else {
            System.out.print("Customer Name: ");
            order.setCustomerName(scanner.nextLine());
            System.out.print("Customer Phone: ");
            order.setCustomerPhone(scanner.nextLine());
            if (type == OrderType.DELIVERY) {
                System.out.print("Delivery Address: ");
                order.setDeliveryAddress(scanner.nextLine());
            }
        }

        displayMenu();
        List<FoodItem> menu = foodItemDao.getAllFoodItems();

        while (true) {
            int itemId = getIntInput("\nEnter Food Item ID to add (or 0 to finish): ");
            if (itemId == 0) break;

            FoodItem selectedItem = menu.stream()
                    .filter(i -> i.getId() == itemId)
                    .findFirst()
                    .orElse(null);

            if (selectedItem != null) {
                int qty = getIntInput("Enter Quantity: ");
                if (qty > 0) {
                    order.addItem(selectedItem, qty);
                    System.out.println("Added " + qty + "x " + selectedItem.getName());
                } else {
                    System.out.println("Quantity must be greater than 0.");
                }
            } else {
                System.out.println("Invalid Food Item ID!");
            }
        }

        if (order.getItems().isEmpty()) {
            System.out.println("Order cancelled (No items added).");
            return;
        }

        if (orderDao.createOrder(order)) {
            // Update table to BUSY automatically
            if (selectedTable != null) {
                selectedTable.setAvailable(false);
                tableDao.updateTableStatus(selectedTable);
                System.out.println("Status: Table #" + selectedTable.getTableNumber() + " is now BUSY.");
            }

            System.out.println("\nOrder Created Successfully! Total Amount: $" + order.getTotalAmount());
            System.out.print("Payment Method (CASH / VISA): ");
            String paymentMethod = scanner.nextLine().toUpperCase();

            Invoice invoice = new Invoice();
            invoice.setOrder(order);
            invoice.setPaymentMethod(paymentMethod);
            invoice.processPayment();

            if (invoiceDao.createInvoice(invoice)) {
                System.out.println("\n--- RECEIPT ---");
                System.out.println(invoice.printReceipt());

                // Free table after payment
                if (selectedTable != null) {
                    selectedTable.setAvailable(true);
                    tableDao.updateTableStatus(selectedTable);
                    System.out.println("Payment Completed. Table #" + selectedTable.getTableNumber() + " is now AVAILABLE again.");
                }
            }
        } else {
            System.out.println("Failed to save order to database.");
        }
    }

    // --- Tables & Reservations Status ---
    private static void showTablesAndReservations() {
        System.out.println("\n--- TABLES STATUS ---");
        List<Table> tables = tableDao.getAllTables();
        for (Table t : tables) {
            System.out.println("Table #" + t.getTableNumber() + 
                               " | Capacity: " + t.getCapacity() +
                               " | Status: " + (t.isAvailable() ? "AVAILABLE" : "BUSY"));
        }

        System.out.println("\n--- ACTIVE RESERVATIONS ---");
        List<Reservation> reservations = reservationDao.getAllReservations();
        if (reservations.isEmpty()) {
            System.out.println("No active reservations found.");
        } else {
            for (Reservation r : reservations) {
                System.out.println("Reservation #" + r.getId() + " - " + r.getCustomerName() +
                                   " | Phone: " + r.getPhone() + " | Table: " + r.getTableNumber());
            }
        }
    }

    // --- Manager Full CRUD Portal ---
    private static void managerPortal() {
        boolean inPortal = true;
        while (inPortal) {
            System.out.println("\n--------------------------------------------------");
            System.out.println("               ADMIN / MANAGER PORTAL             ");
            System.out.println("--------------------------------------------------");
            System.out.println("1. View All Employees");
            System.out.println("2. Delete Employee");
            System.out.println("3. Add New Food Item to Menu");
            System.out.println("4. Delete Food Item from Menu");
            System.out.println("5. Return to Main Menu");
            System.out.println("--------------------------------------------------");

            int choice = getIntInput("Choice: ");
            switch (choice) {
                case 1 -> viewAllEmployees();
                case 2 -> deleteEmployee();
                case 3 -> addFoodItem();
                case 4 -> deleteFoodItem();
                case 5 -> inPortal = false;
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private static void viewAllEmployees() {
        System.out.println("\n--- ALL EMPLOYEES ---");
        List<Employee> employees = employeeDao.getAllEmployees();
        for (Employee e : employees) {
            System.out.printf("ID: %-3d | Name: %-15s | Role: %-8s | Username: %-8s%n",
                    e.getId(), e.getName(), e.getRole(), e.getUsername());
        }
    }

    private static void deleteEmployee() {
        viewAllEmployees();
        int empId = getIntInput("\nEnter Employee ID to delete (0 to cancel): ");
        if (empId == 0) return;

        if (empId == currentUser.getId()) {
            System.out.println("Security Error: You cannot delete your own active account!");
            return;
        }

        if (employeeDao.deleteEmployee(empId)) {
            System.out.println("Employee deleted successfully!");
        } else {
            System.out.println("Failed to delete employee. Please check if ID exists.");
        }
    }

    private static void addFoodItem() {
        System.out.println("\n--- ADD NEW MENU ITEM ---");
        System.out.print("Item Name: ");
        String name = scanner.nextLine().trim();

        System.out.print("Category (Main Course, Burger, Pizza, Appetizer, Dessert, Beverages): ");
        String category = scanner.nextLine().trim();

        System.out.print("Price: ");
        while (!scanner.hasNextDouble()) {
            System.out.print("Please enter a valid price number: ");
            scanner.next();
        }
        double price = scanner.nextDouble();
        scanner.nextLine();

        FoodItem item = new FoodItem(0, name, price, category);
        if (foodItemDao.addFoodItem(item)) {
            System.out.println("Food Item '" + name + "' added to menu successfully!");
        } else {
            System.out.println("Failed to add food item.");
        }
    }

    private static void deleteFoodItem() {
        displayMenu();
        int itemId = getIntInput("\nEnter Food Item ID to delete (0 to cancel): ");
        if (itemId == 0) return;

        if (foodItemDao.deleteFoodItem(itemId)) {
            System.out.println("Food Item deleted successfully!");
        } else {
            System.out.println("Failed to delete food item.");
        }
    }

    private static int getIntInput(String prompt) {
        System.out.print(prompt);
        while (!scanner.hasNextInt()) {
            System.out.println("Please enter a valid number.");
            scanner.next();
            System.out.print(prompt);
        }
        int num = scanner.nextInt();
        scanner.nextLine();
        return num;
    }
}