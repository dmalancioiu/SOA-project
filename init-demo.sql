-- ==========================================
-- Food Delivery Platform - Demo Data Script
-- ==========================================
-- This script creates a clean demo environment with:
-- - 3 Users (Customer, Driver, Owner)
-- - 1 Restaurant (Burger King)
-- - 3 Menu Items (Burger, Fries, Coke)
-- ==========================================

-- Clean up existing data (in reverse order of dependencies)
SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE delivery_service.deliveries;
TRUNCATE TABLE order_service.order_items;
TRUNCATE TABLE order_service.orders;
TRUNCATE TABLE restaurant_service.menu_items;
TRUNCATE TABLE restaurant_service.restaurants;
TRUNCATE TABLE user_service.users;

SET FOREIGN_KEY_CHECKS = 1;

-- ==========================================
-- USER SERVICE - Users
-- ==========================================
-- Password for all users: "password"
-- BCrypt hash: $2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3Z6

INSERT INTO user_service.users (id, email, password, first_name, last_name, phone, role, active, created_at, updated_at)
VALUES
    (1, 'user@test.com', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3Z6', 'John', 'Customer', '+1234567890', 'CUSTOMER', 1, NOW(), NOW()),
    (2, 'driver@test.com', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3Z6', 'Mike', 'Driver', '+1234567891', 'DRIVER', 1, NOW(), NOW()),
    (3, 'owner@test.com', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3Z6', 'Sarah', 'Owner', '+1234567892', 'RESTAURANT_OWNER', 1, NOW(), NOW());

-- Reset auto-increment
ALTER TABLE user_service.users AUTO_INCREMENT = 4;

-- ==========================================
-- RESTAURANT SERVICE - Restaurant
-- ==========================================

INSERT INTO restaurant_service.restaurants (id, owner_id, name, description, cuisine_type, address, phone, rating, delivery_time, active, created_at, updated_at)
VALUES
    (1, 3, 'Burger King', 'Home of the Whopper - Flame-grilled burgers and delicious sides', 'AMERICAN', '123 Main Street, New York, NY 10001', '+1-555-BURGER', 4.5, 30, 1, NOW(), NOW());

-- Reset auto-increment
ALTER TABLE restaurant_service.restaurants AUTO_INCREMENT = 2;

-- ==========================================
-- RESTAURANT SERVICE - Menu Items
-- ==========================================

INSERT INTO restaurant_service.menu_items (id, restaurant_id, name, description, price, category, image_url, available, created_at, updated_at)
VALUES
    (1, 1, 'Whopper Burger', 'Our signature flame-grilled beef burger with fresh lettuce, tomato, pickles, onions, ketchup and mayo on a toasted sesame seed bun', 6.99, 'MAIN_COURSE', 'https://via.placeholder.com/400x300?text=Whopper+Burger', 1, NOW(), NOW()),
    (2, 1, 'French Fries', 'Crispy golden french fries, perfectly seasoned', 2.99, 'SIDES', 'https://via.placeholder.com/400x300?text=French+Fries', 1, NOW(), NOW()),
    (3, 1, 'Coca Cola', 'Ice-cold Coca Cola - Available in Medium and Large', 1.99, 'BEVERAGES', 'https://via.placeholder.com/400x300?text=Coca+Cola', 1, NOW(), NOW());

-- Reset auto-increment
ALTER TABLE restaurant_service.menu_items AUTO_INCREMENT = 4;

-- ==========================================
-- Verification Queries
-- ==========================================

-- Verify users
SELECT 'Users Created:' as Status;
SELECT id, email, first_name, last_name, role FROM user_service.users;

-- Verify restaurant
SELECT 'Restaurant Created:' as Status;
SELECT id, name, cuisine_type, active FROM restaurant_service.restaurants;

-- Verify menu items
SELECT 'Menu Items Created:' as Status;
SELECT id, restaurant_id, name, price, category, available FROM restaurant_service.menu_items;

-- ==========================================
-- Demo Credentials Summary
-- ==========================================

SELECT '==========================================' as '';
SELECT 'DEMO CREDENTIALS' as '';
SELECT '==========================================' as '';
SELECT '' as '';
SELECT 'Customer Account:' as '';
SELECT '  Email: user@test.com' as '';
SELECT '  Password: password' as '';
SELECT '' as '';
SELECT 'Driver Account:' as '';
SELECT '  Email: driver@test.com' as '';
SELECT '  Password: password' as '';
SELECT '' as '';
SELECT 'Restaurant Owner Account:' as '';
SELECT '  Email: owner@test.com' as '';
SELECT '  Password: password' as '';
SELECT '' as '';
SELECT '==========================================' as '';

-- ==========================================
-- Success Message
-- ==========================================

SELECT 'Demo data initialized successfully!' as 'Status';
SELECT 'You can now login with any of the demo accounts listed above.' as 'Info';
