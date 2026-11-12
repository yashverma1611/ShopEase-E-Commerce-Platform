package com.shopease.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderRequestDTO {
    @NotNull(message = "Shipping Address ID is mandatory")
    private Long addressId;

    private String paymentMethod; // COD, ONLINE
}
