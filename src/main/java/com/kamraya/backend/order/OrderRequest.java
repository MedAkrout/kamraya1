package com.kamraya.backend.order;

import lombok.Data;
import java.util.List;

@Data
public class OrderRequest {
    private Long userId;
    private String address;
    private Double total;
    private List<OrderItemRequest> items;

    @Data
    public static class OrderItemRequest {
        private Long articleId;
        private String name;
        private Double price;
        private String color;
        private String size;
        private Integer quantity;
    }
}