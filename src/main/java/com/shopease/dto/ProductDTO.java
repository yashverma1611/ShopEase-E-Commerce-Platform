package com.shopease.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDTO {
    private Long id;

    @NotBlank(message = "Product name is mandatory")
    private String name;

    private String description;

    @NotNull(message = "Product price is mandatory")
    @DecimalMin(value = "0.01", message = "Price must be strictly positive")
    private BigDecimal price;

    private BigDecimal originalPrice;
    private Integer discountPercent;

    @NotNull(message = "Stock quantity is mandatory")
    @Min(value = 0, message = "Stock cannot be negative")
    private Integer stockQuantity;

    private String imageUrl;
    private Double rating;
    private Long categoryId;
    private String categoryName;
}
