package com.shopease.config;

import com.shopease.entity.*;
import com.shopease.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final AddressRepository addressRepository;
    private final CartRepository cartRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (roleRepository.count() > 0) {
            log.info("Database already initialized.");
            return;
        }

        log.info("Bootstrapping ShopEase initial data...");

        // 1. Roles
        Role adminRole = roleRepository.save(Role.builder().name("ROLE_ADMIN").build());
        Role customerRole = roleRepository.save(Role.builder().name("ROLE_CUSTOMER").build());

        // 2. Users
        User admin = User.builder()
                .email("admin@shopease.com")
                .password(passwordEncoder.encode("admin123"))
                .fullName("System Administrator")
                .roles(Set.of(adminRole, customerRole))
                .build();
        userRepository.save(admin);

        User customer = User.builder()
                .email("customer@shopease.com")
                .password(passwordEncoder.encode("customer123"))
                .fullName("Demo Customer")
                .roles(Collections.singleton(customerRole))
                .build();
        userRepository.save(customer);

        // User Address
        Address address = Address.builder()
                .user(customer)
                .street("Flat 402, Royal Residency, Hazratganj")
                .city("Lucknow")
                .state("Uttar Pradesh")
                .postalCode("226001")
                .country("India")
                .build();
        addressRepository.save(address);

        // Cart initialization
        cartRepository.save(Cart.builder().user(customer).build());
        cartRepository.save(Cart.builder().user(admin).build());

        // 3. Categories
        Category catElectronics = categoryRepository.save(Category.builder().name("Electronics").description("Gadgets, Laptops and Hardware").build());
        Category catAudio = categoryRepository.save(Category.builder().name("Audio & Wearables").description("Headphones, Earbuds and Smartwatches").build());
        Category catAccessories = categoryRepository.save(Category.builder().name("Accessories").description("Keyboards, Cables and Chargers").build());

        // 4. Products
        List<Product> products = List.of(
                Product.builder()
                        .name("MacBook Pro 16 M3 Max")
                        .description("Apple M3 Max chip with 16-core CPU, 40-core GPU, 36GB Unified Memory, 1TB SSD Storage.")
                        .price(new BigDecimal("314999.00"))
                        .originalPrice(new BigDecimal("349999.00"))
                        .discountPercent(10)
                        .stockQuantity(15)
                        .rating(4.9)
                        .imageUrl("https://images.unsplash.com/photo-1517336714731-489689fd1ca8?auto=format&fit=crop&w=600&q=80")
                        .category(catElectronics)
                        .active(true)
                        .build(),
                Product.builder()
                        .name("Dell XPS 15 OLED")
                        .description("13th Gen Intel Core i9-13900H, 32GB DDR5, 1TB PCIe NVMe SSD, 15.6 inch 3.5K OLED Touch.")
                        .price(new BigDecimal("214999.00"))
                        .stockQuantity(8)
                        .rating(4.7)
                        .imageUrl("https://images.unsplash.com/photo-1593642632823-8f785ba67e45?auto=format&fit=crop&w=600&q=80")
                        .category(catElectronics)
                        .active(true)
                        .build(),
                Product.builder()
                        .name("Sony WH-1000XM5 Headphones")
                        .description("Industry Leading Noise Canceling Wireless Headphones with Auto NC Optimizer, 30-hour battery.")
                        .price(new BigDecimal("29990.00"))
                        .stockQuantity(25)
                        .rating(4.8)
                        .imageUrl("https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=600&q=80")
                        .category(catAudio)
                        .active(true)
                        .build(),
                Product.builder()
                        .name("Apple AirPods Pro (2nd Gen)")
                        .description("Active Noise Cancellation, Adaptive Audio, Transparency mode, USB-C MagSafe charging case.")
                        .price(new BigDecimal("24900.00"))
                        .stockQuantity(40)
                        .rating(4.9)
                        .imageUrl("https://images.unsplash.com/photo-1600294037681-c80b4cb5b434?auto=format&fit=crop&w=600&q=80")
                        .category(catAudio)
                        .active(true)
                        .build(),
                Product.builder()
                        .name("Logitech MX Master 3S Wireless Mouse")
                        .description("Ergonomic Wireless Mouse with Quiet Clicks, 8K DPI Any-Surface Tracking, MagSpeed Scrolling.")
                        .price(new BigDecimal("9495.00"))
                        .stockQuantity(50)
                        .rating(4.8)
                        .imageUrl("https://images.unsplash.com/photo-1527864550417-7fd91fc51a46?auto=format&fit=crop&w=600&q=80")
                        .category(catAccessories)
                        .active(true)
                        .build(),
                Product.builder()
                        .name("Keychron K2 Pro Mechanical Keyboard")
                        .description("Wireless Custom Mechanical Keyboard with QMK/VIA support, Hot-swappable RGB Backlight.")
                        .price(new BigDecimal("11999.00"))
                        .stockQuantity(12)
                        .rating(4.6)
                        .imageUrl("https://images.unsplash.com/photo-1587829741301-dc798b83add3?auto=format&fit=crop&w=600&q=80")
                        .category(catAccessories)
                        .active(true)
                        .build(),
                Product.builder()
                        .name("Anker 737 GaNPrime 140W Charger")
                        .description("Fast 3-port wall charger powered by GaN Technology with ActiveShield 2.0 temperature sensor.")
                        .price(new BigDecimal("7999.00"))
                        .stockQuantity(3) // Low stock test case
                        .rating(4.7)
                        .imageUrl("https://images.unsplash.com/photo-1583863788434-e58a36330cf0?auto=format&fit=crop&w=600&q=80")
                        .category(catAccessories)
                        .active(true)
                        .build()
        );

        productRepository.saveAll(products);
        log.info("ShopEase data initialization complete! Seeded {} products.", products.size());
    }
}
