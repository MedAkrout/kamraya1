package com.kamraya.backend.user;

import lombok.Data;

@Data
public class AddressRequest {
    private Long userId;
    private String address;
}