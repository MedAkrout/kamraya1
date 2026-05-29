package com.kamraya.backend.order;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderItemDTO {
    private Long articleId;
    private String name;
    private Double price;
    private String color;
    private String size;
    private Integer quantity;
}