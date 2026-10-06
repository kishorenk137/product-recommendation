package com.analytics.customerinsights.config;

import com.analytics.customerinsights.entity.*;
import com.analytics.customerinsights.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final AdminRepository adminRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.data.seed-sample-data:true}")
    private boolean seedSampleData;

    public DataInitializer(AdminRepository adminRepository,
                           CustomerRepository customerRepository,
                           ProductRepository productRepository,
                           OrderRepository orderRepository,
                           PasswordEncoder passwordEncoder) {
        this.adminRepository = adminRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        // 1. Initialize Admin user if not present
        if (!adminRepository.existsByUsername("admin")) {
            Admin admin = new Admin(
                    "admin",
                    passwordEncoder.encode("admin123"),
                    "Lead Analytics Director",
                    "admin@customerinsights.internal",
                    "ROLE_ADMIN"
            );
            adminRepository.save(admin);
            log.info("Demo administrator created -> username: admin, password: admin123");
        }

        // 2. Initialize Sample Data if database is empty and flag is enabled
        if (seedSampleData && customerRepository.count() == 0) {
            log.info("Database is empty. Seeding comprehensive realistic sample dataset...");
            seedData();
            log.info("Sample dataset seeded successfully! (25 Customers, 24 Products, 60+ Orders)");
        } else if (seedSampleData && productRepository.findAll().stream().noneMatch(p -> "Shoes".equalsIgnoreCase(p.getCategory()))) {
            log.info("Adding Shoes category products to existing catalog...");
            seedShoesProducts();
        }
    }

    private void seedData() {
        // --- SEED PRODUCTS (18 products across 5 categories) ---
        List<Product> products = new ArrayList<>();
        // Electronics
        products.add(new Product("Quantum Ultra 4K Smart TV 55\"", "Electronics", new BigDecimal("599.99"), 45, "55-inch HDR10+ Smart OLED screen with AI picture enhancement.", "https://images.unsplash.com/photo-1593359677879-a4bb92f829d1?w=400"));
        products.add(new Product("AeroBook Pro 15\" Laptop", "Electronics", new BigDecimal("1099.99"), 30, "Next-gen M-Core processor with 16GB RAM and 512GB NVMe SSD.", "https://images.unsplash.com/photo-1496181133206-80ce9b88a853?w=400"));
        products.add(new Product("VisionTab 11\" Tablet", "Electronics", new BigDecimal("449.50"), 25, "Ultra-thin 120Hz display with stylus support for designers.", "https://images.unsplash.com/photo-1544244015-0df4b3ffc6b0?w=400"));
        products.add(new Product("PowerCore 25000mAh Power Bank", "Electronics", new BigDecimal("49.99"), 120, "65W USB-C PD fast charge for laptops, tablets, and phones.", "https://images.unsplash.com/photo-1609592424364-500e57207436?w=400"));

        // Audio & Wearables
        products.add(new Product("SonicWave ANC Wireless Headphones", "Audio & Wearables", new BigDecimal("199.99"), 60, "Active Noise Cancelling over-ear Bluetooth 5.3 headphones.", "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=400"));
        products.add(new Product("PulseFit Pro Smartwatch", "Audio & Wearables", new BigDecimal("179.95"), 50, "Health tracking, SpO2, heart rate, GPS, and 10-day battery life.", "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=400"));
        products.add(new Product("TrueAir Wireless Earbuds", "Audio & Wearables", new BigDecimal("89.99"), 85, "IPX7 waterproof earbuds with immersive bass and wireless case.", "https://images.unsplash.com/photo-1590658268037-6bf12165a8df?w=400"));
        products.add(new Product("SoundBar 300W Subwoofer System", "Audio & Wearables", new BigDecimal("249.00"), 22, "Dolby Atmos home theater soundbar with wireless deep bass sub.", "https://images.unsplash.com/photo-1545454675-3531b543be5d?w=400"));

        // Computer Accessories
        products.add(new Product("ErgoMech Mechanical Keyboard", "Computer Accessories", new BigDecimal("129.99"), 70, "Custom tactile switches with RGB per-key backlighting.", "https://images.unsplash.com/photo-1587829741301-dc798b83add3?w=400"));
        products.add(new Product("Precision MX Ergonomic Mouse", "Computer Accessories", new BigDecimal("79.99"), 90, "Hyper-fast scroll wheel and ergonomic wrist-support design.", "https://images.unsplash.com/photo-1527864550417-7fd91fc51a46?w=400"));
        products.add(new Product("UltraWide 34\" Curved Monitor", "Computer Accessories", new BigDecimal("499.00"), 18, "WQHD 3440x1440 resolution, 144Hz refresh rate, USB-C hub.", "https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?w=400"));
        products.add(new Product("ProStream 4K Webcam", "Computer Accessories", new BigDecimal("99.95"), 40, "Autofocus dual stereo microphone with privacy shutter.", "https://images.unsplash.com/photo-1588508065123-287b28e013da?w=400"));

        // Smart Home
        products.add(new Product("SmartBeam Robot Vacuum & Mop", "Smart Home", new BigDecimal("349.99"), 28, "LiDAR navigation, automatic dirt disposal, Alexa/Google compatible.", "https://images.unsplash.com/photo-1518770660439-4636190af475?w=400"));
        products.add(new Product("OmniGlow Smart RGB Bulb 4-Pack", "Smart Home", new BigDecimal("39.99"), 110, "16M colors, scheduling, voice controlled Wi-Fi LEDs.", "https://images.unsplash.com/photo-1550985616-10810253b84d?w=400"));
        products.add(new Product("NestGuard Smart Video Doorbell", "Smart Home", new BigDecimal("149.99"), 35, "2K HDR video with two-way talk and night vision security.", "https://images.unsplash.com/photo-1558002038-1055907df827?w=400"));

        // Home Appliances
        products.add(new Product("Barista Touch Espresso Machine", "Home Appliances", new BigDecimal("420.00"), 15, "15-bar Italian pressure pump with integrated conical burr grinder.", "https://images.unsplash.com/photo-1517668808822-9ebb02f2a0e6?w=400"));
        products.add(new Product("AirCrisp XL Digital Air Fryer", "Home Appliances", new BigDecimal("119.99"), 65, "6-quart rapid air circulation with 8 one-touch cooking presets.", "https://images.unsplash.com/photo-1585659722983-3a675dabf23d?w=400"));
        products.add(new Product("PureBreeze HEPA Air Purifier", "Home Appliances", new BigDecimal("159.00"), 42, "3-stage filtration capturing 99.97% of dust, smoke, and allergens.", "https://images.unsplash.com/photo-1585771724684-38269d6639fd?w=400"));

        // Shoes & Footwear
        products.add(new Product("Apex Sprint Pro Running Shoes", "Shoes", new BigDecimal("129.99"), 45, "Breathable engineered mesh running shoes with responsive foam cushioning and high durability.", "https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=400"));
        products.add(new Product("TrailShield Waterproof Hiking Shoes", "Shoes", new BigDecimal("149.99"), 30, "All-terrain waterproof outdoor walking and trail shoes with rugged vibram grip.", "https://images.unsplash.com/photo-1551107696-a4b085a6d9a6?w=400"));
        products.add(new Product("CloudStride Everyday Walking Shoes", "Shoes", new BigDecimal("79.99"), 60, "Ultra-comfortable lightweight walking sneakers with memory foam insole.", "https://images.unsplash.com/photo-1525966222134-fcfa99b8ae77?w=400"));
        products.add(new Product("ProCourt Indoor Sports Trainers", "Shoes", new BigDecimal("109.99"), 40, "High-traction non-marking court shoes for gym agility, basketball, and sports training.", "https://images.unsplash.com/photo-1608231387042-66d1773070a5?w=400"));
        products.add(new Product("Heritage Classic Leather Derby Shoes", "Shoes", new BigDecimal("159.00"), 25, "Handcrafted full-grain durable leather classic dress shoes with Goodyear welt.", "https://images.unsplash.com/photo-1533867617858-e7b97e060509?w=400"));
        products.add(new Product("AirGlide Performance Marathon Shoes", "Shoes", new BigDecimal("189.99"), 35, "Carbon-fiber plate marathon racing shoes with maximum energy return and supreme comfort.", "https://images.unsplash.com/photo-1595950653106-6c9ebd614d3a?w=400"));

        productRepository.saveAll(products);

        // --- SEED CUSTOMERS (25 realistic fictional customers) ---
        List<Customer> customers = new ArrayList<>();
        customers.add(new Customer("Alexander Wright", "alex.wright@example.com", "+1-555-0101", "Seattle, WA", LocalDate.now().minusMonths(14), "ACTIVE"));
        customers.add(new Customer("Sophia Martinez", "sophia.m@example.com", "+1-555-0102", "Austin, TX", LocalDate.now().minusMonths(12), "ACTIVE"));
        customers.add(new Customer("Liam Chen", "liam.chen@example.com", "+1-555-0103", "San Francisco, CA", LocalDate.now().minusMonths(10), "ACTIVE"));
        customers.add(new Customer("Emma Johnson", "emma.j@example.com", "+1-555-0104", "New York, NY", LocalDate.now().minusMonths(9), "ACTIVE"));
        customers.add(new Customer("Noah Patel", "noah.patel@example.com", "+1-555-0105", "Chicago, IL", LocalDate.now().minusMonths(8), "ACTIVE"));
        customers.add(new Customer("Olivia Taylor", "olivia.t@example.com", "+1-555-0106", "Denver, CO", LocalDate.now().minusMonths(7), "ACTIVE"));
        customers.add(new Customer("Lucas Davis", "lucas.d@example.com", "+1-555-0107", "Boston, MA", LocalDate.now().minusMonths(6), "ACTIVE"));
        customers.add(new Customer("Ava Wilson", "ava.w@example.com", "+1-555-0108", "Atlanta, GA", LocalDate.now().minusMonths(6), "ACTIVE"));
        customers.add(new Customer("Ethan Brown", "ethan.b@example.com", "+1-555-0109", "Portland, OR", LocalDate.now().minusMonths(5), "ACTIVE"));
        customers.add(new Customer("Isabella Clark", "isabella.c@example.com", "+1-555-0110", "San Diego, CA", LocalDate.now().minusMonths(5), "ACTIVE"));
        customers.add(new Customer("Mason Rodriguez", "mason.r@example.com", "+1-555-0111", "Miami, FL", LocalDate.now().minusMonths(4), "ACTIVE"));
        customers.add(new Customer("Mia White", "mia.white@example.com", "+1-555-0112", "Phoenix, AZ", LocalDate.now().minusMonths(4), "ACTIVE"));
        customers.add(new Customer("Benjamin Hall", "ben.hall@example.com", "+1-555-0113", "Dallas, TX", LocalDate.now().minusMonths(3), "ACTIVE"));
        customers.add(new Customer("Charlotte Young", "charlotte.y@example.com", "+1-555-0114", "Minneapolis, MN", LocalDate.now().minusMonths(3), "ACTIVE"));
        customers.add(new Customer("James King", "james.king@example.com", "+1-555-0115", "Raleigh, NC", LocalDate.now().minusMonths(3), "ACTIVE"));
        customers.add(new Customer("Amelia Scott", "amelia.s@example.com", "+1-555-0116", "Nashville, TN", LocalDate.now().minusMonths(2), "ACTIVE"));
        customers.add(new Customer("Henry Green", "henry.g@example.com", "+1-555-0117", "Salt Lake City, UT", LocalDate.now().minusMonths(2), "ACTIVE"));
        customers.add(new Customer("Harper Adams", "harper.a@example.com", "+1-555-0118", "Philadelphia, PA", LocalDate.now().minusMonths(2), "ACTIVE"));
        customers.add(new Customer("Daniel Baker", "daniel.b@example.com", "+1-555-0119", "Las Vegas, NV", LocalDate.now().minusMonths(1), "ACTIVE"));
        customers.add(new Customer("Evelyn Nelson", "evelyn.n@example.com", "+1-555-0120", "Columbus, OH", LocalDate.now().minusDays(20), "ACTIVE"));
        // Additional diverse customers for edge cases (new, occasional, inactive)
        customers.add(new Customer("Jack Miller", "jack.m@example.com", "+1-555-0121", "Detroit, MI", LocalDate.now().minusDays(10), "ACTIVE")); // New
        customers.add(new Customer("Grace Hill", "grace.h@example.com", "+1-555-0122", "Tampa, FL", LocalDate.now().minusDays(5), "ACTIVE")); // Brand new, no purchases yet
        customers.add(new Customer("Samuel Moore", "samuel.m@example.com", "+1-555-0123", "Kansas City, MO", LocalDate.now().minusMonths(8), "INACTIVE")); // Inactive
        customers.add(new Customer("Ella Garcia", "ella.g@example.com", "+1-555-0124", "Orlando, FL", LocalDate.now().minusMonths(9), "ACTIVE"));
        customers.add(new Customer("David Lopez", "david.l@example.com", "+1-555-0125", "San Antonio, TX", LocalDate.now().minusMonths(11), "ACTIVE"));

        customerRepository.saveAll(customers);

        // --- SEED ORDERS (60+ orders with realistic patterns) ---
        // Let's create orders with clear patterns:
        // Customer 0 (Alex Wright): High-Spender in Electronics & Accessories (Laptop, TV, Curved Monitor)
        // Customer 1 (Sophia Martinez): Frequent Buyer in Smart Home & Home Appliances (Robot Vacuum, Espresso, Bulbs)
        // Customer 2 (Liam Chen): Audio Enthusiast (ANC Headphones, Earbuds, Soundbar)
        // Customer 3 (Emma Johnson): High-Spender across multiple categories
        // Customer 4 (Noah Patel): Computer Accessories (Keyboard, Mouse, Monitor)
        // Others with realistic distributed orders across the last 10 months.

        List<Order> orders = new ArrayList<>();
        String[] paymentMethods = {"Credit Card", "PayPal", "Debit Card", "Credit Card", "Bank Transfer"};

        // Alex Wright orders (High Spender)
        orders.add(createOrder(customers.get(0), LocalDateTime.now().minusMonths(8), "COMPLETED", paymentMethods[0],
                new ItemSpec(products.get(1), 1), // Laptop ($1099.99)
                new ItemSpec(products.get(9), 1))); // Mouse ($79.99)
        orders.add(createOrder(customers.get(0), LocalDateTime.now().minusMonths(5), "COMPLETED", paymentMethods[1],
                new ItemSpec(products.get(0), 1))); // Smart TV ($599.99)
        orders.add(createOrder(customers.get(0), LocalDateTime.now().minusMonths(2), "COMPLETED", paymentMethods[0],
                new ItemSpec(products.get(10), 1), // Curved Monitor ($499.00)
                new ItemSpec(products.get(8), 1))); // Mechanical Keyboard ($129.99)
        orders.add(createOrder(customers.get(0), LocalDateTime.now().minusDays(15), "COMPLETED", paymentMethods[0],
                new ItemSpec(products.get(3), 2))); // Power Bank x2 ($99.98)

        // Sophia Martinez orders (Frequent Buyer in Smart Home & Appliances)
        orders.add(createOrder(customers.get(1), LocalDateTime.now().minusMonths(9), "COMPLETED", paymentMethods[1],
                new ItemSpec(products.get(12), 1))); // Robot Vacuum ($349.99)
        orders.add(createOrder(customers.get(1), LocalDateTime.now().minusMonths(7), "COMPLETED", paymentMethods[0],
                new ItemSpec(products.get(13), 2))); // Smart Bulbs ($79.98)
        orders.add(createOrder(customers.get(1), LocalDateTime.now().minusMonths(5), "COMPLETED", paymentMethods[2],
                new ItemSpec(products.get(14), 1))); // Video Doorbell ($149.99)
        orders.add(createOrder(customers.get(1), LocalDateTime.now().minusMonths(3), "COMPLETED", paymentMethods[0],
                new ItemSpec(products.get(16), 1))); // Air Fryer ($119.99)
        orders.add(createOrder(customers.get(1), LocalDateTime.now().minusMonths(1), "COMPLETED", paymentMethods[0],
                new ItemSpec(products.get(17), 1))); // HEPA Air Purifier ($159.00)
        orders.add(createOrder(customers.get(1), LocalDateTime.now().minusDays(12), "COMPLETED", paymentMethods[1],
                new ItemSpec(products.get(15), 1))); // Espresso Machine ($420.00)

        // Liam Chen (Audio Enthusiast)
        orders.add(createOrder(customers.get(2), LocalDateTime.now().minusMonths(8), "COMPLETED", paymentMethods[0],
                new ItemSpec(products.get(4), 1))); // ANC Headphones ($199.99)
        orders.add(createOrder(customers.get(2), LocalDateTime.now().minusMonths(6), "COMPLETED", paymentMethods[0],
                new ItemSpec(products.get(6), 1), // Earbuds ($89.99)
                new ItemSpec(products.get(3), 1))); // Power Bank ($49.99)
        orders.add(createOrder(customers.get(2), LocalDateTime.now().minusMonths(3), "COMPLETED", paymentMethods[1],
                new ItemSpec(products.get(7), 1))); // Soundbar 300W ($249.00)
        orders.add(createOrder(customers.get(2), LocalDateTime.now().minusDays(20), "COMPLETED", paymentMethods[0],
                new ItemSpec(products.get(5), 1))); // Smartwatch ($179.95)

        // Emma Johnson (High Spender Multi-Category)
        orders.add(createOrder(customers.get(3), LocalDateTime.now().minusMonths(7), "COMPLETED", paymentMethods[0],
                new ItemSpec(products.get(1), 1), // Laptop ($1099.99)
                new ItemSpec(products.get(8), 1), // Mechanical Keyboard ($129.99)
                new ItemSpec(products.get(9), 1))); // Ergonomic Mouse ($79.99)
        orders.add(createOrder(customers.get(3), LocalDateTime.now().minusMonths(4), "COMPLETED", paymentMethods[1],
                new ItemSpec(products.get(15), 1))); // Espresso Machine ($420.00)
        orders.add(createOrder(customers.get(3), LocalDateTime.now().minusDays(28), "COMPLETED", paymentMethods[0],
                new ItemSpec(products.get(4), 1))); // ANC Headphones ($199.99)

        // Noah Patel (Accessories & Electronics)
        orders.add(createOrder(customers.get(4), LocalDateTime.now().minusMonths(6), "COMPLETED", paymentMethods[2],
                new ItemSpec(products.get(8), 1), // Keyboard
                new ItemSpec(products.get(9), 1))); // Mouse
        orders.add(createOrder(customers.get(4), LocalDateTime.now().minusMonths(4), "COMPLETED", paymentMethods[0],
                new ItemSpec(products.get(10), 1), // Monitor
                new ItemSpec(products.get(11), 1))); // Webcam
        orders.add(createOrder(customers.get(4), LocalDateTime.now().minusDays(22), "COMPLETED", paymentMethods[0],
                new ItemSpec(products.get(2), 1))); // Tablet

        // Olivia Taylor (Home Appliances & Smart Home)
        orders.add(createOrder(customers.get(5), LocalDateTime.now().minusMonths(5), "COMPLETED", paymentMethods[1],
                new ItemSpec(products.get(16), 1), // Air Fryer
                new ItemSpec(products.get(17), 1))); // Air Purifier
        orders.add(createOrder(customers.get(5), LocalDateTime.now().minusMonths(2), "COMPLETED", paymentMethods[0],
                new ItemSpec(products.get(12), 1))); // Robot Vacuum
        orders.add(createOrder(customers.get(5), LocalDateTime.now().minusDays(18), "COMPLETED", paymentMethods[0],
                new ItemSpec(products.get(13), 1))); // Smart Bulbs

        // Lucas Davis (Electronics & Audio)
        orders.add(createOrder(customers.get(6), LocalDateTime.now().minusMonths(5), "COMPLETED", paymentMethods[0],
                new ItemSpec(products.get(0), 1), // Smart TV
                new ItemSpec(products.get(7), 1))); // Soundbar
        orders.add(createOrder(customers.get(6), LocalDateTime.now().minusDays(40), "COMPLETED", paymentMethods[1],
                new ItemSpec(products.get(5), 1))); // Smartwatch

        // Ava Wilson (Frequent Audio buyer)
        orders.add(createOrder(customers.get(7), LocalDateTime.now().minusMonths(5), "COMPLETED", paymentMethods[0],
                new ItemSpec(products.get(4), 1))); // Headphones
        orders.add(createOrder(customers.get(7), LocalDateTime.now().minusMonths(3), "COMPLETED", paymentMethods[2],
                new ItemSpec(products.get(6), 1))); // Earbuds
        orders.add(createOrder(customers.get(7), LocalDateTime.now().minusMonths(1), "COMPLETED", paymentMethods[0],
                new ItemSpec(products.get(5), 1))); // Smartwatch

        // Ethan Brown (Computer Accessories)
        orders.add(createOrder(customers.get(8), LocalDateTime.now().minusMonths(4), "COMPLETED", paymentMethods[0],
                new ItemSpec(products.get(8), 1), // Keyboard
                new ItemSpec(products.get(9), 1))); // Mouse
        orders.add(createOrder(customers.get(8), LocalDateTime.now().minusMonths(2), "COMPLETED", paymentMethods[1],
                new ItemSpec(products.get(11), 1))); // Webcam

        // Isabella Clark (Smart Home)
        orders.add(createOrder(customers.get(9), LocalDateTime.now().minusMonths(4), "COMPLETED", paymentMethods[0],
                new ItemSpec(products.get(14), 1), // Video Doorbell
                new ItemSpec(products.get(13), 2))); // Smart Bulbs x2
        orders.add(createOrder(customers.get(9), LocalDateTime.now().minusDays(25), "COMPLETED", paymentMethods[0],
                new ItemSpec(products.get(12), 1))); // Robot Vacuum

        // Mason Rodriguez (Electronics & Home)
        orders.add(createOrder(customers.get(10), LocalDateTime.now().minusMonths(3), "COMPLETED", paymentMethods[1],
                new ItemSpec(products.get(16), 1))); // Air Fryer
        orders.add(createOrder(customers.get(10), LocalDateTime.now().minusDays(35), "COMPLETED", paymentMethods[0],
                new ItemSpec(products.get(3), 1))); // Power Bank

        // Mia White (Audio)
        orders.add(createOrder(customers.get(11), LocalDateTime.now().minusMonths(3), "COMPLETED", paymentMethods[0],
                new ItemSpec(products.get(6), 1))); // Earbuds
        orders.add(createOrder(customers.get(11), LocalDateTime.now().minusDays(14), "COMPLETED", paymentMethods[2],
                new ItemSpec(products.get(5), 1))); // Smartwatch

        // Benjamin Hall (Electronics High Spender)
        orders.add(createOrder(customers.get(12), LocalDateTime.now().minusMonths(3), "COMPLETED", paymentMethods[0],
                new ItemSpec(products.get(1), 1), // Laptop ($1099.99)
                new ItemSpec(products.get(10), 1))); // Curved Monitor ($499.00)

        // Charlotte Young (Appliances)
        orders.add(createOrder(customers.get(13), LocalDateTime.now().minusMonths(2), "COMPLETED", paymentMethods[1],
                new ItemSpec(products.get(15), 1), // Espresso Machine
                new ItemSpec(products.get(16), 1))); // Air Fryer

        // James King (Accessories)
        orders.add(createOrder(customers.get(14), LocalDateTime.now().minusMonths(2), "COMPLETED", paymentMethods[0],
                new ItemSpec(products.get(8), 1), // Keyboard
                new ItemSpec(products.get(11), 1))); // Webcam

        // Amelia Scott (Wearables)
        orders.add(createOrder(customers.get(15), LocalDateTime.now().minusMonths(2), "COMPLETED", paymentMethods[0],
                new ItemSpec(products.get(5), 1))); // Smartwatch
        orders.add(createOrder(customers.get(15), LocalDateTime.now().minusDays(10), "COMPLETED", paymentMethods[0],
                new ItemSpec(products.get(6), 1))); // Earbuds

        // Henry Green (Smart Home)
        orders.add(createOrder(customers.get(16), LocalDateTime.now().minusMonths(1), "COMPLETED", paymentMethods[1],
                new ItemSpec(products.get(13), 2))); // Bulbs
        orders.add(createOrder(customers.get(16), LocalDateTime.now().minusDays(8), "COMPLETED", paymentMethods[0],
                new ItemSpec(products.get(14), 1))); // Doorbell

        // Harper Adams (Home)
        orders.add(createOrder(customers.get(17), LocalDateTime.now().minusMonths(1), "COMPLETED", paymentMethods[0],
                new ItemSpec(products.get(17), 1))); // Air Purifier

        // Daniel Baker (Electronics)
        orders.add(createOrder(customers.get(18), LocalDateTime.now().minusDays(22), "COMPLETED", paymentMethods[2],
                new ItemSpec(products.get(2), 1), // Tablet
                new ItemSpec(products.get(3), 1))); // Power Bank

        // Evelyn Nelson (Shoes & Athletic Focus)
        orders.add(createOrder(customers.get(19), LocalDateTime.now().minusDays(15), "COMPLETED", paymentMethods[0],
                new ItemSpec(products.get(18), 1))); // Apex Sprint Pro Running Shoes ($129.99)
        orders.add(createOrder(customers.get(19), LocalDateTime.now().minusDays(3), "COMPLETED", paymentMethods[0],
                new ItemSpec(products.get(20), 1))); // CloudStride Everyday Walking Shoes ($79.99)

        // Jack Miller (Outdoor & Sports Shoes)
        orders.add(createOrder(customers.get(20), LocalDateTime.now().minusDays(7), "COMPLETED", paymentMethods[1],
                new ItemSpec(products.get(19), 1), // TrailShield Hiking Shoes ($149.99)
                new ItemSpec(products.get(21), 1))); // ProCourt Sports Trainers ($109.99)

        // Ella Garcia (Occasional)
        orders.add(createOrder(customers.get(23), LocalDateTime.now().minusMonths(7), "COMPLETED", paymentMethods[0],
                new ItemSpec(products.get(16), 1))); // Air Fryer
        orders.add(createOrder(customers.get(23), LocalDateTime.now().minusMonths(4), "COMPLETED", paymentMethods[0],
                new ItemSpec(products.get(13), 1))); // Bulbs

        // David Lopez (High Spender)
        orders.add(createOrder(customers.get(24), LocalDateTime.now().minusMonths(9), "COMPLETED", paymentMethods[0],
                new ItemSpec(products.get(0), 1), // TV
                new ItemSpec(products.get(7), 1))); // Soundbar
        orders.add(createOrder(customers.get(24), LocalDateTime.now().minusMonths(6), "COMPLETED", paymentMethods[0],
                new ItemSpec(products.get(1), 1))); // Laptop
        orders.add(createOrder(customers.get(24), LocalDateTime.now().minusMonths(3), "COMPLETED", paymentMethods[1],
                new ItemSpec(products.get(10), 1))); // Monitor

        // Additional seasonal orders distributed across months for realistic trend charts
        orders.add(createOrder(customers.get(4), LocalDateTime.now().minusMonths(5), "COMPLETED", paymentMethods[0],
                new ItemSpec(products.get(9), 1), new ItemSpec(products.get(8), 1)));
        orders.add(createOrder(customers.get(7), LocalDateTime.now().minusMonths(4), "COMPLETED", paymentMethods[1],
                new ItemSpec(products.get(4), 1), new ItemSpec(products.get(6), 1)));
        orders.add(createOrder(customers.get(10), LocalDateTime.now().minusMonths(2), "COMPLETED", paymentMethods[0],
                new ItemSpec(products.get(12), 1), new ItemSpec(products.get(13), 1)));
        orders.add(createOrder(customers.get(14), LocalDateTime.now().minusMonths(1), "COMPLETED", paymentMethods[0],
                new ItemSpec(products.get(10), 1), new ItemSpec(products.get(9), 1)));
        orders.add(createOrder(customers.get(2), LocalDateTime.now().minusDays(5), "COMPLETED", paymentMethods[0],
                new ItemSpec(products.get(4), 1), new ItemSpec(products.get(7), 1)));
        orders.add(createOrder(customers.get(5), LocalDateTime.now().minusDays(3), "COMPLETED", paymentMethods[1],
                new ItemSpec(products.get(15), 1), new ItemSpec(products.get(16), 1)));

        orderRepository.saveAll(orders);
    }

    private static class ItemSpec {
        Product product;
        int qty;
        ItemSpec(Product product, int qty) {
            this.product = product;
            this.qty = qty;
        }
    }

    private Order createOrder(Customer customer, LocalDateTime date, String status, String paymentMethod, ItemSpec... itemSpecs) {
        Order order = new Order(customer, date, BigDecimal.ZERO, status, paymentMethod);
        order.setShippingAddress(customer.getCity());

        BigDecimal total = BigDecimal.ZERO;
        for (ItemSpec spec : itemSpecs) {
            BigDecimal lineTotal = spec.product.getPrice().multiply(BigDecimal.valueOf(spec.qty));
            OrderItem item = new OrderItem(order, spec.product, spec.qty, spec.product.getPrice(), lineTotal);
            order.addItem(item);
            total = total.add(lineTotal);
        }
        order.setTotalAmount(total);
        return order;
    }

    private void seedShoesProducts() {
        List<Product> shoeProducts = new ArrayList<>();
        shoeProducts.add(new Product("Apex Sprint Pro Running Shoes", "Shoes", new BigDecimal("129.99"), 45, "Breathable engineered mesh running shoes with responsive foam cushioning and high durability.", "https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=400"));
        shoeProducts.add(new Product("TrailShield Waterproof Hiking Shoes", "Shoes", new BigDecimal("149.99"), 30, "All-terrain waterproof outdoor walking and trail shoes with rugged vibram grip.", "https://images.unsplash.com/photo-1551107696-a4b085a6d9a6?w=400"));
        shoeProducts.add(new Product("CloudStride Everyday Walking Shoes", "Shoes", new BigDecimal("79.99"), 60, "Ultra-comfortable lightweight walking sneakers with memory foam insole.", "https://images.unsplash.com/photo-1525966222134-fcfa99b8ae77?w=400"));
        shoeProducts.add(new Product("ProCourt Indoor Sports Trainers", "Shoes", new BigDecimal("109.99"), 40, "High-traction non-marking court shoes for gym agility, basketball, and sports training.", "https://images.unsplash.com/photo-1608231387042-66d1773070a5?w=400"));
        shoeProducts.add(new Product("Heritage Classic Leather Derby Shoes", "Shoes", new BigDecimal("159.00"), 25, "Handcrafted full-grain durable leather classic dress shoes with Goodyear welt.", "https://images.unsplash.com/photo-1533867617858-e7b97e060509?w=400"));
        shoeProducts.add(new Product("AirGlide Performance Marathon Shoes", "Shoes", new BigDecimal("189.99"), 35, "Carbon-fiber plate marathon racing shoes with maximum energy return and supreme comfort.", "https://images.unsplash.com/photo-1595950653106-6c9ebd614d3a?w=400"));
        productRepository.saveAll(shoeProducts);
        log.info("Successfully seeded 6 Shoes products into catalog.");
    }
}
