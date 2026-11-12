package com.shopease.service;

import com.shopease.dto.OrderRequestDTO;
import com.shopease.dto.OrderResponseDTO;

import java.util.List;

public interface OrderService {
    OrderResponseDTO placeOrder(String username, OrderRequestDTO request);
    List<OrderResponseDTO> getUserOrders(String username);
    OrderResponseDTO getOrderById(Long id, String username);
    List<OrderResponseDTO> getAllOrders();
    OrderResponseDTO updateOrderStatus(Long orderId, String status);
    void addToCart(String username, Long productId, int quantity);
    void removeFromCart(String username, Long productId);
    com.shopease.entity.Cart getCart(String username);
}
