package com.kamraya.backend.cart;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CartItemDTO {
    private Long articleId;
    private String name;
    private Double price;
    private String category;
    private String color;
    private String size;
    private Integer quantity;
    private String status; 
    private Integer maxQuantity;

}