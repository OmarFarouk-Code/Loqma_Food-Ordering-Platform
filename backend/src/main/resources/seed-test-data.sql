-- Idempotent test data for Loqma (Customer Homepage & Restaurant List)
-- Run against MySQL `loqma` database.
-- Safe to re-run: new rows are inserted, existing restaurant rows get their
-- logo / cover_image refreshed (so Cloudinary URL changes actually apply).

-- 1. Sample customer (matches CurrentCustomerProvider.FAKE_CUSTOMER_ID = "sample-customer-1")
INSERT IGNORE INTO users (id, name, email, phone, password, role, status, reapplication_attempts)
VALUES ('sample-customer-1', 'Omar Aly', 'omar.aly@loqma.test', '01099999999', '$2a$10$dummyhashforseedingonly000000000000000000000000000', 'CUSTOMER', 'ACTIVE', 0);

INSERT IGNORE INTO customers (user_id, points_balance)
VALUES ('sample-customer-1', 250);

-- 2. Restaurant owners (users + restaurant_owners)
INSERT IGNORE INTO users (id, name, email, phone, password, role, status, reapplication_attempts) VALUES
('owner-seed-01', 'Karim Smash',    'owner01@loqma.test', '01010000001', '$2a$10$dummyhash', 'RESTAURANT_OWNER', 'ACTIVE', 0),
('owner-seed-02', 'Marco Rossi',    'owner02@loqma.test', '01010000002', '$2a$10$dummyhash', 'RESTAURANT_OWNER', 'ACTIVE', 0),
('owner-seed-03', 'Tarek Crisp',    'owner03@loqma.test', '01010000003', '$2a$10$dummyhash', 'RESTAURANT_OWNER', 'ACTIVE', 0),
('owner-seed-04', 'Hassan Bahary',  'owner04@loqma.test', '01010000004', '$2a$10$dummyhash', 'RESTAURANT_OWNER', 'ACTIVE', 0),
('owner-seed-05', 'Kenji Tanaka',   'owner05@loqma.test', '01010000005', '$2a$10$dummyhash', 'RESTAURANT_OWNER', 'ACTIVE', 0),
('owner-seed-06', 'Giulia Conti',   'owner06@loqma.test', '01010000006', '$2a$10$dummyhash', 'RESTAURANT_OWNER', 'ACTIVE', 0),
('owner-seed-07', 'Mostafa Kebda',  'owner07@loqma.test', '01010000007', '$2a$10$dummyhash', 'RESTAURANT_OWNER', 'ACTIVE', 0),
('owner-seed-08', 'Nour Kunafa',    'owner08@loqma.test', '01010000008', '$2a$10$dummyhash', 'RESTAURANT_OWNER', 'ACTIVE', 0),
('owner-seed-09', 'Salma Greens',   'owner09@loqma.test', '01010000009', '$2a$10$dummyhash', 'RESTAURANT_OWNER', 'ACTIVE', 0),
('owner-seed-10', 'Youssef Juice',  'owner10@loqma.test', '01010000010', '$2a$10$dummyhash', 'RESTAURANT_OWNER', 'ACTIVE', 0),
('owner-seed-11', 'Wael Flame',     'owner11@loqma.test', '01010000011', '$2a$10$dummyhash', 'RESTAURANT_OWNER', 'ACTIVE', 0);

INSERT IGNORE INTO restaurant_owners (user_id, restaurant_name, branch) VALUES
('owner-seed-01', 'Smash & Co.',        'Maadi'),
('owner-seed-02', 'Napoli Woodfire',    'Zamalek'),
('owner-seed-03', 'Crispy Cluck',       'Nasr City'),
('owner-seed-04', 'Nile Catch',         'Dokki'),
('owner-seed-05', 'Sushi Garden',       'Zamalek'),
('owner-seed-06', 'Trattoria Roma',     'New Cairo'),
('owner-seed-07', 'Kebda Corner',       'Heliopolis'),
('owner-seed-08', 'Pistachio & Cream',  'Mohandessin'),
('owner-seed-09', 'Green Bowl',         'Maadi'),
('owner-seed-10', 'Citrus Bar',         'Downtown'),
('owner-seed-11', 'Flame Burger House', 'Sheikh Zayed');

-- 3. Restaurants
-- INSERT ... ON DUPLICATE KEY UPDATE (not INSERT IGNORE): if the row already
-- exists, only logo and cover_image are overwritten. Nothing else is touched,
-- and foreign keys (menu_items, favorites) are not affected.
INSERT INTO restaurants (
  id, owner_id, name, branch, cuisine, logo, cover_image, address, phone,
  delivery_fee, minimum_order_amount, prep_time, max_delivery_distance,
  average_rating, is_open, gps_location
) VALUES
('rest-seed-01', 'owner-seed-01', 'Smash & Co.', 'Maadi', 'Burgers',
 'https://res.cloudinary.com/nhail52p/image/upload/v1795697408/smash-co-logo.avif',
 'https://res.cloudinary.com/nhail52p/image/upload/v1795697408/smash-co-logo.avif',
 'Road 9, Maadi', '01010000001', 20.0, 80.0, 15.0, 15.0, 4.8, true, '29.9602,31.2569'),

('rest-seed-02', 'owner-seed-02', 'Napoli Woodfire', 'Zamalek', 'Pizza',
 'https://res.cloudinary.com/nhail52p/image/upload/v1795697751/napoli-woodfire-logo.avif',
 'https://res.cloudinary.com/nhail52p/image/upload/v1795697751/napoli-woodfire-logo.avif',
 '26th of July St, Zamalek', '01010000002', 30.0, 120.0, 20.0, 15.0, 4.7, true, '30.0609,31.2197'),

('rest-seed-03', 'owner-seed-03', 'Crispy Cluck', 'Nasr City', 'Chicken',
 'https://res.cloudinary.com/nhail52p/image/upload/v1795697750/cripsy-cluck-logo.avif',
 'https://res.cloudinary.com/nhail52p/image/upload/v1795697750/cripsy-cluck-logo.avif',
 'Abbas El Akkad, Nasr City', '01010000003', 15.0, 60.0, 18.0, 15.0, 4.3, true, '30.0561,31.3301'),

('rest-seed-04', 'owner-seed-04', 'Nile Catch', 'Dokki', 'Seafood',
 'https://res.cloudinary.com/nhail52p/image/upload/v1795697746/nile-catch-logo.avif',
 'https://res.cloudinary.com/nhail52p/image/upload/v1795697746/nile-catch-logo.avif',
 'Tahrir St, Dokki', '01010000004', 35.0, 150.0, 28.0, 15.0, 4.5, true, '30.0385,31.2121'),

('rest-seed-05', 'owner-seed-05', 'Sushi Garden', 'Zamalek', 'Asian',
 'https://res.cloudinary.com/nhail52p/image/upload/v1795697748/sushi-garden-logo.avif',
 'https://res.cloudinary.com/nhail52p/image/upload/v1795697748/sushi-garden-logo.avif',
 'Brazil St, Zamalek', '01010000005', 40.0, 180.0, 22.0, 15.0, 4.6, true, '30.0647,31.2185'),

('rest-seed-06', 'owner-seed-06', 'Trattoria Roma', 'New Cairo', 'Italian',
 'https://res.cloudinary.com/nhail52p/image/upload/v1795697740/trattoria-roma-logo.avif',
 'https://res.cloudinary.com/nhail52p/image/upload/v1795697740/trattoria-roma-logo.avif',
 '5th Settlement, New Cairo', '01010000006', 30.0, 140.0, 25.0, 20.0, 4.4, true, '30.0131,31.4913'),

('rest-seed-07', 'owner-seed-07', 'Kebda Corner', 'Heliopolis', 'Oriental',
 'https://res.cloudinary.com/nhail52p/image/upload/v1795697745/kebda-corner-logo.avif',
 'https://res.cloudinary.com/nhail52p/image/upload/v1795697745/kebda-corner-logo.avif',
 'Korba, Heliopolis', '01010000007', 25.0, 50.0, 15.0, 15.0, 4.2, false, '30.0876,31.3242'),

('rest-seed-08', 'owner-seed-08', 'Pistachio & Cream', 'Mohandessin', 'Desserts',
 'https://res.cloudinary.com/nhail52p/image/upload/v1795697740/pistachio-cream-logo.avif',
 'https://res.cloudinary.com/nhail52p/image/upload/v1795697740/pistachio-cream-logo.avif',
 'Lebanon Sq, Mohandessin', '01010000008', 20.0, 70.0, 12.0, 15.0, 4.9, true, '30.0566,31.2015'),

('rest-seed-09', 'owner-seed-09', 'Green Bowl', 'Maadi', 'Healthy',
 'https://res.cloudinary.com/nhail52p/image/upload/v1795697740/green-bowl-logo.avif',
 'https://res.cloudinary.com/nhail52p/image/upload/v1795697740/green-bowl-logo.avif',
 'Degla, Maadi', '01010000009', 0.0, 90.0, 14.0, 15.0, 4.1, true, '29.9588,31.2733'),

('rest-seed-10', 'owner-seed-10', 'Citrus Bar', 'Downtown', 'Drinks',
 'https://res.cloudinary.com/nhail52p/image/upload/v1795697738/citrus-bar-logo.avif',
 'https://res.cloudinary.com/nhail52p/image/upload/v1795697738/citrus-bar-logo.avif',
 'Talaat Harb, Downtown', '01010000010', 10.0, 40.0, 10.0, 15.0, 4.5, true, '30.0478,31.2392'),

('rest-seed-11', 'owner-seed-11', 'Flame Burger House', 'Sheikh Zayed', 'Burgers',
 'https://res.cloudinary.com/nhail52p/image/upload/v1795697738/flame-burger-house.avif',
 'https://res.cloudinary.com/nhail52p/image/upload/v1795697738/flame-burger-house.avif',
 'Arkan Plaza, Sheikh Zayed', '01010000011', 35.0, 100.0, 20.0, 20.0, 3.9, true, '30.0226,30.9963')
ON DUPLICATE KEY UPDATE
  logo        = VALUES(logo),
  cover_image = VALUES(cover_image);

-- 4. Menu items (for dish search + Deals filter)
-- Still INSERT IGNORE: these have no photo column set yet. When you add
-- menu photos, switch to the same ON DUPLICATE KEY UPDATE pattern with
-- photo = VALUES(photo).
INSERT IGNORE INTO menu_items (id, restaurant_id, category, name, description, base_price, is_available, discount_percent) VALUES
('item-seed-01', 'rest-seed-01', 'Burgers',  'Double Smash Burger',   'Two crispy-edge beef patties with aged cheddar', 185.0, true, 15.0),
('item-seed-02', 'rest-seed-01', 'Sides',    'Truffle Fries',         'Crispy fries with parmesan and truffle oil',    75.0, true, 0.0),
('item-seed-03', 'rest-seed-02', 'Pizza',    'Margherita Napoletana', 'San Marzano tomatoes, fior di latte, basil',     210.0, true, 20.0),
('item-seed-04', 'rest-seed-03', 'Chicken',  'Spicy Chicken Tenders', 'Five golden tenders with honey mustard',         160.0, true, 10.0),
('item-seed-05', 'rest-seed-04', 'Seafood',  'Grilled Tiger Prawns',  'Garlic butter prawns with lemon herb rice',      320.0, true, 0.0),
('item-seed-06', 'rest-seed-05', 'Sushi',    'Dragon Roll',           'Shrimp tempura, avocado, unagi glaze',           260.0, true, 15.0),
('item-seed-07', 'rest-seed-06', 'Pasta',    'Truffle Tagliatelle',   'Fresh pasta in creamy mushroom sauce',           240.0, true, 0.0),
('item-seed-08', 'rest-seed-08', 'Desserts', 'Pistachio Kunafa',      'Warm crispy kunafa with clotted cream',          140.0, true, 25.0),
('item-seed-09', 'rest-seed-09', 'Salads',   'Avocado Quinoa Bowl',   'Roasted sweet potato, edamame, tahini',          155.0, true, 0.0),
('item-seed-10', 'rest-seed-10', 'Drinks',   'Iced Hibiscus Mint',    'Chilled ruby hibiscus with fresh lime & mint',    55.0, true, 0.0);

-- 5. Sample customer favourites
INSERT IGNORE INTO customer_favorites (customer_id, restaurant_id) VALUES
('sample-customer-1', 'rest-seed-01'),
('sample-customer-1', 'rest-seed-02'),
('sample-customer-1', 'rest-seed-05'),
('sample-customer-1', 'rest-seed-08');