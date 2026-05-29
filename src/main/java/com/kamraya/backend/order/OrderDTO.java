package com.kamraya.backend.order;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OrderDTO {
    private Long id;
    private String address;
    private Double total;
    private String status;
    private LocalDateTime createdAt;
    private String userEmail;
    private String userName;
    private String userPhone;
    private List<OrderItemDTO> items;
}