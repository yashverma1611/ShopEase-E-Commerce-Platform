package com.shopease.service;

import com.shopease.dto.OrderRequestDTO;
import com.shopease.dto.OrderResponseDTO;
import com.shopease.entity.*;
import com.shopease.exception.BadRequestException;
import com.shopease.exception.InsufficientStockException;
import com.shopease.exception.ResourceNotFoundException;
import com.shopease.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final ProductRepository productRepository;
    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;

    @Override
    @Transactional
    public OrderResponseDTO placeOrder(String username, OrderRequestDTO request) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", username));

        Address address = addressRepository.findById(request.getAddressId())
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", request.getAddressId()));

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new BadRequestException("User has no active cart"));

        if (cart.getItems().isEmpty()) {
            throw new BadRequestException("Cart is empty. Add products before checkout.");
        }

        // Validate stock for all items
        for (CartItem item : cart.getItems()) {
            Product prod = item.getProduct();
            if (prod.getStockQuantity() < item.getQuantity()) {
                throw new InsufficientStockException(prod.getName(), prod.getStockQuantity(), item.getQuantity());
            }
        }

        // Deduct inventory and build Order
        BigDecimal totalAmount = BigDecimal.ZERO;
        Order order = Order.builder()
                .user(user)
                .shippingAddress(address)
                .status(OrderStatus.PLACED)
                .totalAmount(BigDecimal.ZERO)
                .build();

        for (CartItem item : cart.getItems()) {
            Product prod = item.getProduct();
            prod.setStockQuantity(prod.getStockQuantity() - item.getQuantity());
            productRepository.save(prod);

            BigDecimal itemTotal = prod.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(prod)
                    .quantity(item.getQuantity())
                    .price(prod.getPrice())
                    .build();
            order.getOrderItems().add(orderItem);
        }

        order.setTotalAmount(totalAmount);

        // Create Payment
        Payment payment = Payment.builder()
                .order(order)
                .method(request.getPaymentMethod() != null ? request.getPaymentMethod() : "COD")
                .transactionId("TXN-" + UUID.randomUUID().toString().substring(0, 10).toUpperCase())
                .status("SUCCESS")
                .build();
        order.setPayment(payment);

        Order savedOrder = orderRepository.save(order);

        // Clear Cart
        cart.getItems().clear();
        cartRepository.save(cart);

        return mapToDTO(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getUserOrders(String username) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", username));

        return orderRepository.findByUserOrderByOrderDateDesc(user).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponseDTO getOrderById(Long id, String username) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));

        if (!order.getUser().getEmail().equals(username)) {
            throw new BadRequestException("Unauthorized access to order");
        }

        return mapToDTO(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OrderResponseDTO updateOrderStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        try {
            order.setStatus(OrderStatus.valueOf(status.toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid status: " + status + ". Allowed: PLACED, PACKED, SHIPPED, DELIVERED, CANCELLED");
        }

        Order updated = orderRepository.save(order);
        return mapToDTO(updated);
    }

    @Override
    @Transactional
    public void addToCart(String username, Long productId, int quantity) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", username));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        if (product.getStockQuantity() < quantity) {
            throw new InsufficientStockException(product.getName(), product.getStockQuantity(), quantity);
        }

        Cart cart = cartRepository.findByUser(user)
                .orElseGet(() -> cartRepository.save(Cart.builder().user(user).build()));

        // Check if product already in cart
        CartItem existingItem = cart.getItems().stream()
                .filter(i -> i.getProduct().getId().equals(productId))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
        } else {
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(quantity)
                    .build();
            cart.getItems().add(newItem);
        }

        cartRepository.save(cart);
    }

    @Override
    @Transactional
    public void removeFromCart(String username, Long productId) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", username));

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new BadRequestException("Cart not found"));

        cart.getItems().removeIf(item -> item.getProduct().getId().equals(productId));
        cartRepository.save(cart);
    }

    @Override
    @Transactional(readOnly = true)
    public Cart getCart(String username) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", username));

        return cartRepository.findByUser(user)
                .orElseGet(() -> cartRepository.save(Cart.builder().user(user).build()));
    }

    private OrderResponseDTO mapToDTO(Order order) {
        List<OrderResponseDTO.OrderItemDTO> itemDTOs = order.getOrderItems().stream()
                .map(i -> OrderResponseDTO.OrderItemDTO.builder()
                        .productId(i.getProduct().getId())
                        .productName(i.getProduct().getName())
                        .quantity(i.getQuantity())
                        .unitPrice(i.getPrice())
                        .build())
                .collect(Collectors.toList());

        String addressStr = order.getShippingAddress() != null
                ? String.format("%s, %s, %s - %s",
                order.getShippingAddress().getStreet(),
                order.getShippingAddress().getCity(),
                order.getShippingAddress().getState(),
                order.getShippingAddress().getPostalCode())
                : "Address unavailable";

        return OrderResponseDTO.builder()
                .id(order.getId())
                .orderTrackingNumber(order.getOrderTrackingNumber())
                .status(order.getStatus().name())
                .totalAmount(order.getTotalAmount())
                .orderDate(order.getOrderDate())
                .customerEmail(order.getUser().getEmail())
                .shippingAddress(addressStr)
                .items(itemDTOs)
                .build();
    }
}
