-- ==============================================================================
-- Project: Personalized Customer Insights Through Purchase Patterns and Preference Prediction
-- Database Seeder Script: data.sql
-- Default Admin Account: admin / admin123 (BCrypt hashed)
-- ==============================================================================

USE customer_insights;

-- 1. Insert Administrator (password: admin123)
INSERT INTO admins (username, password, full_name, email, role, created_at)
VALUES ('admin', '$2a$10$w09ZkC27o58c819GzL6x.e1vG8r0X6kGqT0zBq7v1iS2jY4zZgV.2', 'Lead Analytics Director', 'admin@customerinsights.internal', 'ROLE_ADMIN', NOW())
ON DUPLICATE KEY UPDATE full_name=VALUES(full_name);

-- 2. Insert Products (18 Products across 5 Categories)
INSERT INTO products (id, name, category, price, stock_quantity, description, image_url, created_at) VALUES
(1, 'Quantum Ultra 4K Smart TV 55"', 'Electronics', 599.99, 45, '55-inch HDR10+ Smart OLED screen with AI picture enhancement.', 'https://images.unsplash.com/photo-1593359677879-a4bb92f829d1?w=400', NOW()),
(2, 'AeroBook Pro 15" Laptop', 'Electronics', 1099.99, 30, 'Next-gen M-Core processor with 16GB RAM and 512GB NVMe SSD.', 'https://images.unsplash.com/photo-1496181133206-80ce9b88a853?w=400', NOW()),
(3, 'VisionTab 11" Tablet', 'Electronics', 449.50, 25, 'Ultra-thin 120Hz display with stylus support for designers.', 'https://images.unsplash.com/photo-1544244015-0df4b3ffc6b0?w=400', NOW()),
(4, 'PowerCore 25000mAh Power Bank', 'Electronics', 49.99, 120, '65W USB-C PD fast charge for laptops, tablets, and phones.', 'https://images.unsplash.com/photo-1609592424364-500e57207436?w=400', NOW()),
(5, 'SonicWave ANC Wireless Headphones', 'Audio & Wearables', 199.99, 60, 'Active Noise Cancelling over-ear Bluetooth 5.3 headphones.', 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=400', NOW()),
(6, 'PulseFit Pro Smartwatch', 'Audio & Wearables', 179.95, 50, 'Health tracking, SpO2, heart rate, GPS, and 10-day battery life.', 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=400', NOW()),
(7, 'TrueAir Wireless Earbuds', 'Audio & Wearables', 89.99, 85, 'IPX7 waterproof earbuds with immersive bass and wireless case.', 'https://images.unsplash.com/photo-1590658268037-6bf12165a8df?w=400', NOW()),
(8, 'SoundBar 300W Subwoofer System', 'Audio & Wearables', 249.00, 22, 'Dolby Atmos home theater soundbar with wireless deep bass sub.', 'https://images.unsplash.com/photo-1545454675-3531b543be5d?w=400', NOW()),
(9, 'ErgoMech Mechanical Keyboard', 'Computer Accessories', 129.99, 70, 'Custom tactile switches with RGB per-key backlighting.', 'https://images.unsplash.com/photo-1587829741301-dc798b83add3?w=400', NOW()),
(10, 'Precision MX Ergonomic Mouse', 'Computer Accessories', 79.99, 90, 'Hyper-fast scroll wheel and ergonomic wrist-support design.', 'https://images.unsplash.com/photo-1527864550417-7fd91fc51a46?w=400', NOW()),
(11, 'UltraWide 34" Curved Monitor', 'Computer Accessories', 499.00, 18, 'WQHD 3440x1440 resolution, 144Hz refresh rate, USB-C hub.', 'https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?w=400', NOW()),
(12, 'ProStream 4K Webcam', 'Computer Accessories', 99.95, 40, 'Autofocus dual stereo microphone with privacy shutter.', 'https://images.unsplash.com/photo-1588508065123-287b28e013da?w=400', NOW()),
(13, 'SmartBeam Robot Vacuum & Mop', 'Smart Home', 349.99, 28, 'LiDAR navigation, automatic dirt disposal, Alexa/Google compatible.', 'https://images.unsplash.com/photo-1518770660439-4636190af475?w=400', NOW()),
(14, 'OmniGlow Smart RGB Bulb 4-Pack', 'Smart Home', 39.99, 110, '16M colors, scheduling, voice controlled Wi-Fi LEDs.', 'https://images.unsplash.com/photo-1550985616-10810253b84d?w=400', NOW()),
(15, 'NestGuard Smart Video Doorbell', 'Smart Home', 149.99, 35, '2K HDR video with two-way talk and night vision security.', 'https://images.unsplash.com/photo-1558002038-1055907df827?w=400', NOW()),
(16, 'Barista Touch Espresso Machine', 'Home Appliances', 420.00, 15, '15-bar Italian pressure pump with integrated conical burr grinder.', 'https://images.unsplash.com/photo-1517668808822-9ebb02f2a0e6?w=400', NOW()),
(17, 'AirCrisp XL Digital Air Fryer', 'Home Appliances', 119.99, 65, '6-quart rapid air circulation with 8 one-touch cooking presets.', 'https://images.unsplash.com/photo-1585659722983-3a675dabf23d?w=400', NOW()),
(18, 'PureBreeze HEPA Air Purifier', 'Home Appliances', 159.00, 42, '3-stage filtration capturing 99.97% of dust, smoke, and allergens.', 'https://images.unsplash.com/photo-1585771724684-38269d6639fd?w=400', NOW()),
(19, 'Apex Sprint Pro Running Shoes', 'Shoes', 129.99, 45, 'Breathable engineered mesh running shoes with responsive foam cushioning and high durability.', 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=400', NOW()),
(20, 'TrailShield Waterproof Hiking Shoes', 'Shoes', 149.99, 30, 'All-terrain waterproof outdoor walking and trail shoes with rugged vibram grip.', 'https://images.unsplash.com/photo-1551107696-a4b085a6d9a6?w=400', NOW()),
(21, 'CloudStride Everyday Walking Shoes', 'Shoes', 79.99, 60, 'Ultra-comfortable lightweight walking sneakers with memory foam insole.', 'https://images.unsplash.com/photo-1525966222134-fcfa99b8ae77?w=400', NOW()),
(22, 'ProCourt Indoor Sports Trainers', 'Shoes', 109.99, 40, 'High-traction non-marking court shoes for gym agility, basketball, and sports training.', 'https://images.unsplash.com/photo-1608231387042-66d1773070a5?w=400', NOW()),
(23, 'Heritage Classic Leather Derby Shoes', 'Shoes', 159.00, 25, 'Handcrafted full-grain durable leather classic dress shoes with Goodyear welt.', 'https://images.unsplash.com/photo-1533867617858-e7b97e060509?w=400', NOW()),
(24, 'AirGlide Performance Marathon Shoes', 'Shoes', 189.99, 35, 'Carbon-fiber plate marathon racing shoes with maximum energy return and supreme comfort.', 'https://images.unsplash.com/photo-1595950653106-6c9ebd614d3a?w=400', NOW())
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- 3. Insert Customers (25 Fictional Customers)
INSERT INTO customers (id, name, email, phone, city, registration_date, status, created_at) VALUES
(1, 'Alexander Wright', 'alex.wright@example.com', '+1-555-0101', 'Seattle, WA', DATE_SUB(CURDATE(), INTERVAL 14 MONTH), 'ACTIVE', NOW()),
(2, 'Sophia Martinez', 'sophia.m@example.com', '+1-555-0102', 'Austin, TX', DATE_SUB(CURDATE(), INTERVAL 12 MONTH), 'ACTIVE', NOW()),
(3, 'Liam Chen', 'liam.chen@example.com', '+1-555-0103', 'San Francisco, CA', DATE_SUB(CURDATE(), INTERVAL 10 MONTH), 'ACTIVE', NOW()),
(4, 'Emma Johnson', 'emma.j@example.com', '+1-555-0104', 'New York, NY', DATE_SUB(CURDATE(), INTERVAL 9 MONTH), 'ACTIVE', NOW()),
(5, 'Noah Patel', 'noah.patel@example.com', '+1-555-0105', 'Chicago, IL', DATE_SUB(CURDATE(), INTERVAL 8 MONTH), 'ACTIVE', NOW()),
(6, 'Olivia Taylor', 'olivia.t@example.com', '+1-555-0106', 'Denver, CO', DATE_SUB(CURDATE(), INTERVAL 7 MONTH), 'ACTIVE', NOW()),
(7, 'Lucas Davis', 'lucas.d@example.com', '+1-555-0107', 'Boston, MA', DATE_SUB(CURDATE(), INTERVAL 6 MONTH), 'ACTIVE', NOW()),
(8, 'Ava Wilson', 'ava.w@example.com', '+1-555-0108', 'Atlanta, GA', DATE_SUB(CURDATE(), INTERVAL 6 MONTH), 'ACTIVE', NOW()),
(9, 'Ethan Brown', 'ethan.b@example.com', '+1-555-0109', 'Portland, OR', DATE_SUB(CURDATE(), INTERVAL 5 MONTH), 'ACTIVE', NOW()),
(10, 'Isabella Clark', 'isabella.c@example.com', '+1-555-0110', 'San Diego, CA', DATE_SUB(CURDATE(), INTERVAL 5 MONTH), 'ACTIVE', NOW()),
(11, 'Mason Rodriguez', 'mason.r@example.com', '+1-555-0111', 'Miami, FL', DATE_SUB(CURDATE(), INTERVAL 4 MONTH), 'ACTIVE', NOW()),
(12, 'Mia White', 'mia.white@example.com', '+1-555-0112', 'Phoenix, AZ', DATE_SUB(CURDATE(), INTERVAL 4 MONTH), 'ACTIVE', NOW()),
(13, 'Benjamin Hall', 'ben.hall@example.com', '+1-555-0113', 'Dallas, TX', DATE_SUB(CURDATE(), INTERVAL 3 MONTH), 'ACTIVE', NOW()),
(14, 'Charlotte Young', 'charlotte.y@example.com', '+1-555-0114', 'Minneapolis, MN', DATE_SUB(CURDATE(), INTERVAL 3 MONTH), 'ACTIVE', NOW()),
(15, 'James King', 'james.king@example.com', '+1-555-0115', 'Raleigh, NC', DATE_SUB(CURDATE(), INTERVAL 3 MONTH), 'ACTIVE', NOW()),
(16, 'Amelia Scott', 'amelia.s@example.com', '+1-555-0116', 'Nashville, TN', DATE_SUB(CURDATE(), INTERVAL 2 MONTH), 'ACTIVE', NOW()),
(17, 'Henry Green', 'henry.g@example.com', '+1-555-0117', 'Salt Lake City, UT', DATE_SUB(CURDATE(), INTERVAL 2 MONTH), 'ACTIVE', NOW()),
(18, 'Harper Adams', 'harper.a@example.com', '+1-555-0118', 'Philadelphia, PA', DATE_SUB(CURDATE(), INTERVAL 2 MONTH), 'ACTIVE', NOW()),
(19, 'Daniel Baker', 'daniel.b@example.com', '+1-555-0119', 'Las Vegas, NV', DATE_SUB(CURDATE(), INTERVAL 1 MONTH), 'ACTIVE', NOW()),
(20, 'Evelyn Nelson', 'evelyn.n@example.com', '+1-555-0120', 'Columbus, OH', DATE_SUB(CURDATE(), INTERVAL 20 DAY), 'ACTIVE', NOW()),
(21, 'Jack Miller', 'jack.m@example.com', '+1-555-0121', 'Detroit, MI', DATE_SUB(CURDATE(), INTERVAL 10 DAY), 'ACTIVE', NOW()),
(22, 'Grace Hill', 'grace.h@example.com', '+1-555-0122', 'Tampa, FL', DATE_SUB(CURDATE(), INTERVAL 5 DAY), 'ACTIVE', NOW()),
(23, 'Samuel Moore', 'samuel.m@example.com', '+1-555-0123', 'Kansas City, MO', DATE_SUB(CURDATE(), INTERVAL 8 MONTH), 'INACTIVE', NOW()),
(24, 'Ella Garcia', 'ella.g@example.com', '+1-555-0124', 'Orlando, FL', DATE_SUB(CURDATE(), INTERVAL 9 MONTH), 'ACTIVE', NOW()),
(25, 'David Lopez', 'david.l@example.com', '+1-555-0125', 'San Antonio, TX', DATE_SUB(CURDATE(), INTERVAL 11 MONTH), 'ACTIVE', NOW())
ON DUPLICATE KEY UPDATE name=VALUES(name);
