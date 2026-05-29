package com.kamraya.backend.auth;

import lombok.Data;

@Data
public class VerifyRequest {
    private String email;
    private String code;
}