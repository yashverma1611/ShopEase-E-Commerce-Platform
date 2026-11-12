package com.shopease.controller;

import com.shopease.dto.OrderRequestDTO;
import com.shopease.dto.OrderResponseDTO;
import com.shopease.entity.Cart;
import com.shopease.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/orders/place")
    public ResponseEntity<OrderResponseDTO> placeOrder(
            Authentication authentication,
            @Valid @RequestBody OrderRequestDTO request
    ) {
        String username = authentication.getName();
        return new ResponseEntity<>(orderService.placeOrder(username, request), HttpStatus.CREATED);
    }

    @GetMapping("/orders/my-orders")
    public ResponseEntity<List<OrderResponseDTO>> getMyOrders(Authentication authentication) {
        String username = authentication.getName();
        return ResponseEntity.ok(orderService.getUserOrders(username));
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<OrderResponseDTO> getOrderById(
            @PathVariable Long id,
            Authentication authentication
    ) {
        String username = authentication.getName();
        return ResponseEntity.ok(orderService.getOrderById(id, username));
    }

    // Cart Endpoints
    @GetMapping("/cart")
    public ResponseEntity<Cart> getCart(Authentication authentication) {
        return ResponseEntity.ok(orderService.getCart(authentication.getName()));
    }

    @PostMapping("/cart/add")
    public ResponseEntity<Map<String, String>> addToCart(
            Authentication authentication,
            @RequestBody Map<String, Object> payload
    ) {
        Long productId = Long.valueOf(payload.get("productId").toString());
        int quantity = Integer.parseInt(payload.getOrDefault("quantity", 1).toString());
        orderService.addToCart(authentication.getName(), productId, quantity);
        return ResponseEntity.ok(Map.of("message", "Product added to cart successfully"));
    }

    @DeleteMapping("/cart/remove/{productId}")
    public ResponseEntity<Map<String, String>> removeFromCart(
            Authentication authentication,
            @PathVariable Long productId
    ) {
        orderService.removeFromCart(authentication.getName(), productId);
        return ResponseEntity.ok(Map.of("message", "Product removed from cart"));
    }
}
