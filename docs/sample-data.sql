USE loqma;
DELETE FROM reviews WHERE id LIKE 'sample-%';

DELETE FROM customer_favorites WHERE customer_id LIKE 'sample-%';
DELETE FROM menu_items WHERE restaurant_id LIKE 'sample-%';
DELETE FROM restaurants WHERE id LIKE 'sample-%';
DELETE FROM restaurant_owners WHERE user_id LIKE 'sample-%';
DELETE FROM customers WHERE user_id LIKE 'sample-%';
DELETE FROM users WHERE id LIKE 'sample-%';

INSERT INTO users (id, name, email, phone, password, role, status, reapplication_attempts) VALUES
('sample-owner-1', 'Owner One',   'owner1@sample.test', '01990000001', 'sample-not-a-real-hash', 'RESTAURANT_OWNER', 'ACTIVE', 0),
('sample-owner-2', 'Owner Two',   'owner2@sample.test', '01990000002', 'sample-not-a-real-hash', 'RESTAURANT_OWNER', 'ACTIVE', 0),
('sample-owner-3', 'Owner Three', 'owner3@sample.test', '01990000003', 'sample-not-a-real-hash', 'RESTAURANT_OWNER', 'ACTIVE', 0),
('sample-owner-4', 'Owner Four',  'owner4@sample.test', '01990000004', 'sample-not-a-real-hash', 'RESTAURANT_OWNER', 'ACTIVE', 0),
('sample-owner-5', 'Owner Five',  'owner5@sample.test', '01990000005', 'sample-not-a-real-hash', 'RESTAURANT_OWNER', 'ACTIVE', 0),
('sample-owner-6', 'Owner Six',   'owner6@sample.test', '01990000006', 'sample-not-a-real-hash', 'RESTAURANT_OWNER', 'ACTIVE', 0),
('sample-customer-1', 'Test Customer', 'customer1@sample.test', '01990000011', 'sample-not-a-real-hash', 'CUSTOMER', 'ACTIVE', 0);

INSERT INTO restaurant_owners (user_id, restaurant_name, branch) VALUES
('sample-owner-1', 'Burger King', 'Maadi'),
('sample-owner-2', 'Pizza Roma', 'Nasr City'),
('sample-owner-3', 'Za3eem El Koshary', 'Downtown'),
('sample-owner-4', 'Asmak KhaledFish', 'Zamalek'),
('sample-owner-5', '7alewyat ballam', 'Heliopolis'),
('sample-owner-6', 'Healthy Tabaly', 'New Cairo');

INSERT INTO customers (user_id, points_balance) VALUES ('sample-customer-1', 0);

INSERT INTO restaurants (id, owner_id, name, branch, cuisine, logo, cover_image, address, phone,
                         delivery_fee, minimum_order_amount, prep_time, max_delivery_distance,
                         average_rating, is_open, gps_location) VALUES
('sample-rest-1', 'sample-owner-1', 'Burger King', 'Maadi', 'Burgers', NULL, NULL, 'Road 9, Maadi', '0221000001', 15, 80, 20, 15, 4.5, 1, '29.9602,31.2569'),
('sample-rest-2', 'sample-owner-2', 'Pizza Roma', 'Nasr City', 'Pizza', NULL, NULL, 'Abbas El Akkad St, Nasr City', '0221000002', 20, 100, 25, 15, 4.2, 1, '30.0626,31.3219'),
('sample-rest-3', 'sample-owner-3', 'Za3eem El Koshary', 'Downtown', 'Egyptian', NULL, NULL, 'Talaat Harb St, Downtown', '0221000003', 10, 40, 15, 15, 4.8, 1, '30.0444,31.2357'),
('sample-rest-4', 'sample-owner-4', 'Asmak KhaledFish', 'Zamalek', 'Seafood', NULL, NULL, '26th of July St, Zamalek', '0221000004', 25, 150, 35, 15, 3.9, 0, '30.0609,31.2193'),
('sample-rest-5', 'sample-owner-5', '7alewyat ballam', 'Heliopolis', 'Desserts', NULL, NULL, 'Korba, Heliopolis', '0221000005', 12, 60, 15, 15, 4.6, 1, '30.0911,31.3228'),
('sample-rest-6', 'sample-owner-6', 'Healthy Tabaly', 'New Cairo', 'Healthy', NULL, NULL, 'South 90th St, New Cairo', '0221000006', 18, 90, 20, 15, 4.0, 1, '30.0074,31.4913');

INSERT INTO menu_items (id, restaurant_id, category, name, description, base_price, photo, is_available) VALUES
('sample-item-1',  'sample-rest-1', 'Burgers',  'Classic Cheeseburger', 'Beef patty with cheddar', 85, NULL, 1),
('sample-item-2',  'sample-rest-1', 'Sides',    'Loaded Fries', 'Fries with cheese sauce', 45, NULL, 1),
('sample-item-3',  'sample-rest-2', 'Pizza',    'Margherita Pizza', 'Tomato, mozzarella, basil', 110, NULL, 1),
('sample-item-4',  'sample-rest-2', 'Pasta',    'Chicken Alfredo', 'Creamy pasta with chicken', 95, NULL, 1),
('sample-item-5',  'sample-rest-3', 'Main',     'Koshary Large', 'Rice, lentils, pasta, sauce', 40, NULL, 1),
('sample-item-6',  'sample-rest-3', 'Sides',    'Fried Onions', 'Extra crispy onions', 10, NULL, 1),
('sample-item-7',  'sample-rest-4', 'Seafood',  'Grilled Sea Bass', 'Served with rice', 220, NULL, 1),
('sample-item-8',  'sample-rest-4', 'Seafood',  'Shrimp Platter', 'Fried or grilled shrimp', 260, NULL, 1),
('sample-item-9',  'sample-rest-5', 'Desserts', 'Chocolate Lava Cake', 'Warm center', 70, NULL, 1),
('sample-item-10', 'sample-rest-5', 'Desserts', 'Kunafa with Cream', 'Classic dessert', 65, NULL, 1),
('sample-item-11', 'sample-rest-6', 'Bowls',    'Chicken Quinoa Bowl', 'Grilled chicken, quinoa, greens', 120, NULL, 1),
('sample-item-12', 'sample-rest-6', 'Salads',   'Greek Salad', 'Feta, olives, cucumber', 80, NULL, 1);

INSERT INTO customer_favorites (customer_id, restaurant_id) VALUES
('sample-customer-1', 'sample-rest-1'),
('sample-customer-1', 'sample-rest-3');

UPDATE menu_items SET discount_percent = 20
WHERE id IN ('sample-item-1', 'sample-item-5', 'sample-item-9');