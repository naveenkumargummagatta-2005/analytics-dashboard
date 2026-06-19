-- ============================================================
-- Analytics Dashboard - Database Schema & Seed Data
-- Run this once in MySQL before starting the Java backend.
-- ============================================================

CREATE DATABASE IF NOT EXISTS analytics_db;
USE analytics_db;

DROP TABLE IF EXISTS sales;

CREATE TABLE sales (
    id INT AUTO_INCREMENT PRIMARY KEY,
    product_name VARCHAR(100) NOT NULL,
    category VARCHAR(50) NOT NULL,
    region VARCHAR(50) NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    sale_date DATE NOT NULL
);

-- Seed data: ~40 rows across categories, regions, and months
INSERT INTO sales (product_name, category, region, quantity, unit_price, sale_date) VALUES
('Wireless Mouse', 'Electronics', 'North', 25, 19.99, '2025-01-05'),
('Mechanical Keyboard', 'Electronics', 'North', 12, 79.99, '2025-01-12'),
('USB-C Hub', 'Electronics', 'South', 18, 34.50, '2025-01-18'),
('Office Chair', 'Furniture', 'East', 7, 149.00, '2025-01-22'),
('Standing Desk', 'Furniture', 'West', 4, 399.00, '2025-01-28'),
('Notebook Pack', 'Stationery', 'North', 60, 4.99, '2025-02-02'),
('Gel Pens Set', 'Stationery', 'South', 45, 7.49, '2025-02-06'),
('Monitor 27in', 'Electronics', 'East', 9, 219.99, '2025-02-10'),
('Webcam HD', 'Electronics', 'West', 14, 49.99, '2025-02-14'),
('Desk Lamp', 'Furniture', 'North', 22, 24.99, '2025-02-19'),
('Bookshelf', 'Furniture', 'South', 6, 89.00, '2025-02-24'),
('Sticky Notes', 'Stationery', 'East', 80, 2.99, '2025-02-27'),
('Bluetooth Speaker', 'Electronics', 'North', 16, 59.99, '2025-03-03'),
('Laptop Stand', 'Electronics', 'South', 21, 29.99, '2025-03-08'),
('Filing Cabinet', 'Furniture', 'West', 5, 129.00, '2025-03-11'),
('Whiteboard', 'Stationery', 'East', 10, 39.99, '2025-03-15'),
('External SSD 1TB', 'Electronics', 'West', 13, 89.99, '2025-03-19'),
('Ergonomic Mouse Pad', 'Electronics', 'North', 30, 14.99, '2025-03-23'),
('Conference Table', 'Furniture', 'South', 2, 599.00, '2025-03-27'),
('Highlighter Pack', 'Stationery', 'North', 55, 5.49, '2025-03-30'),
('Wireless Charger', 'Electronics', 'East', 19, 24.99, '2025-04-02'),
('Gaming Headset', 'Electronics', 'West', 11, 69.99, '2025-04-06'),
('Bookcase Small', 'Furniture', 'North', 8, 64.00, '2025-04-10'),
('Printer Paper Box', 'Stationery', 'South', 35, 12.99, '2025-04-14'),
('Desk Organizer', 'Furniture', 'East', 17, 18.99, '2025-04-18'),
('Tablet Stand', 'Electronics', 'North', 24, 22.50, '2025-04-22'),
('Mesh Office Chair', 'Furniture', 'West', 9, 175.00, '2025-04-26'),
('Marker Set', 'Stationery', 'East', 48, 6.99, '2025-04-29'),
('27in Curved Monitor', 'Electronics', 'South', 7, 289.99, '2025-05-03'),
('LED Desk Light', 'Furniture', 'North', 20, 27.99, '2025-05-07'),
('Notebook Premium', 'Stationery', 'West', 38, 9.99, '2025-05-11'),
('Docking Station', 'Electronics', 'East', 15, 109.99, '2025-05-15'),
('Adjustable Footrest', 'Furniture', 'South', 12, 34.00, '2025-05-19'),
('Binder Clips Box', 'Stationery', 'North', 70, 3.49, '2025-05-23'),
('Mechanical Numpad', 'Electronics', 'West', 18, 39.99, '2025-05-27'),
('Office Sofa', 'Furniture', 'East', 3, 449.00, '2025-05-30'),
('Sticky Flags', 'Stationery', 'South', 65, 4.29, '2025-06-02'),
('Portable Monitor', 'Electronics', 'North', 10, 159.99, '2025-06-06'),
('Reception Desk', 'Furniture', 'West', 1, 899.00, '2025-06-10'),
('Pen Holder Set', 'Stationery', 'East', 42, 8.99, '2025-06-14');
