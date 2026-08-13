CREATE DATABASE IF NOT EXISTS restaurant_db;
USE restaurant_db;

-- 1. Employees Table
CREATE TABLE IF NOT EXISTS employees (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(50) NOT NULL,
    role VARCHAR(20) NOT NULL,
    salary DOUBLE DEFAULT 0,
    is_available BOOLEAN DEFAULT TRUE
);

-- 2. Food Items Table
CREATE TABLE IF NOT EXISTS food_items (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    price DOUBLE NOT NULL,
    category VARCHAR(50),
    is_available BOOLEAN DEFAULT TRUE
);

-- 3. Restaurant Tables
CREATE TABLE IF NOT EXISTS restaurant_tables (
    table_number INT PRIMARY KEY,
    capacity INT NOT NULL,
    is_available BOOLEAN DEFAULT TRUE
);

-- 4. Orders Table
CREATE TABLE IF NOT EXISTS orders (
    order_id INT PRIMARY KEY AUTO_INCREMENT,
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    cashier_id INT,
    order_type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    total_amount DOUBLE DEFAULT 0,
    customer_name VARCHAR(100),
    customer_phone VARCHAR(20),
    delivery_address TEXT,
    delivery_fee DOUBLE DEFAULT 0,
    table_number INT,
    FOREIGN KEY (cashier_id) REFERENCES employees(id) ON DELETE SET NULL,
    FOREIGN KEY (table_number) REFERENCES restaurant_tables(table_number) ON DELETE SET NULL
);

-- 5. Order Items Table
CREATE TABLE IF NOT EXISTS order_items (
    id INT PRIMARY KEY AUTO_INCREMENT,
    order_id INT NOT NULL,
    food_item_id INT NOT NULL,
    quantity INT NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
    FOREIGN KEY (food_item_id) REFERENCES food_items(id) ON DELETE CASCADE
);

-- 6. Invoices Table
CREATE TABLE IF NOT EXISTS invoices (
    invoice_id INT PRIMARY KEY AUTO_INCREMENT,
    order_id INT,
    amount DOUBLE NOT NULL,
    payment_method VARCHAR(50),
    payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_paid BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE
);

-- 7. Reservations Table
CREATE TABLE IF NOT EXISTS reservations (
    id INT PRIMARY KEY AUTO_INCREMENT,
    customer_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    reservation_date TIMESTAMP NOT NULL,
    table_number INT,
    FOREIGN KEY (table_number) REFERENCES restaurant_tables(table_number) ON DELETE CASCADE
);

-- ==================================================
-- SEED INITIAL DATA
-- ==================================================

-- Initial Employees
INSERT INTO employees (id, name, phone, username, password, role, is_available) VALUES
(18, 'Norien', '01013229536', 'noni', '1234', 'MANAGER', TRUE),
(18, 'mohamed', '01000000001', 'Antr', '1234', 'MANAGER', TRUE),
(19, 'Filo Wael', '01000000002', 'filo', '1234', 'MANAGER', TRUE),
(20, 'Sara Ali', '01000000003', 'sara', '1234', 'MANAGER', TRUE),
(21, 'noureen', '01000000004', 'nourine', '1234', 'CASHIER', TRUE),
(22, 'norien', '01000000005', 'noureen', '1234', 'DELIVERY', TRUE),
(23, 'Mina Atef', '01000000006', 'mina', '1234', 'DELIVERY', TRUE)
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- Initial Food Items
INSERT INTO food_items (id, name, price, category, is_available) VALUES
(1, 'Beef Burger', 120.00, 'Main Course', TRUE),
(2, 'Chicken Pizza', 150.00, 'Main Course', TRUE),
(3, 'French Fries', 45.00, 'Appetizers', TRUE),
(4, 'Caesar Salad', 60.00, 'Salads', TRUE),
(5, 'Fresh Orange Juice', 35.00, 'Beverages', TRUE)
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- Initial Restaurant Tables
INSERT INTO restaurant_tables (table_number, capacity, is_available) VALUES
(1, 2, TRUE),
(2, 4, TRUE),
(3, 4, TRUE),
(4, 6, TRUE),
(5, 8, TRUE)
ON DUPLICATE KEY UPDATE capacity=VALUES(capacity);
