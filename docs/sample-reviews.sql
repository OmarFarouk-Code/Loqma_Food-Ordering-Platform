USE loqma;

SET FOREIGN_KEY_CHECKS = 0;

DELETE FROM reviews WHERE id LIKE 'sample-%';

INSERT INTO reviews (id, order_id, customer_id, restaurant_id, customer_name, restaurant_rating,
                     restaurant_tags, driver_tags, restaurant_comment, created_at) VALUES
('sample-review-1', 'sample-order-1', 'sample-customer-1', 'sample-rest-3', 'Ahmed Mostafa', 5, '[]', '[]', 'Best koshary downtown.',        '2026-11-20 13:10:00'),
('sample-review-2', 'sample-order-2', 'sample-customer-1', 'sample-rest-3', 'Mona',          5, '[]', '[]', 'Hot and fast.',                '2026-11-21 14:30:00'),
('sample-review-3', 'sample-order-3', 'sample-customer-1', 'sample-rest-3', 'Youssef',       4, '[]', '[]', 'Great, a bit salty for me.',   '2026-11-22 12:00:00'),
('sample-review-4', 'sample-order-4', 'sample-customer-1', 'sample-rest-3', 'Salma',         5, '[]', '[]', 'Huge portion for the price.',  '2026-11-23 19:45:00'),
('sample-review-5', 'sample-order-5', 'sample-customer-1', 'sample-rest-3', 'Omar',          3, '[]', '[]', 'Good food, arrived lukewarm.', '2026-11-24 15:20:00'),
('sample-review-6', 'sample-order-6', 'sample-customer-1', 'sample-rest-1', 'Laila',         4, '[]', '[]', 'Juicy burger.',                '2026-11-22 20:00:00'),
('sample-review-7', 'sample-order-7', 'sample-customer-1', 'sample-rest-1', 'Hassan',        5, '[]', '[]', 'Perfect every time.',          '2026-11-23 21:10:00');

SET FOREIGN_KEY_CHECKS = 1;