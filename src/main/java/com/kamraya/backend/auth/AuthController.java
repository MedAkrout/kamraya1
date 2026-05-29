package com.kamraya.backend.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // Étape 1 — envoie le code, ne crée pas le compte
    @PostMapping("/pre-register")
    public ResponseEntity<?> preRegister(@RequestBody RegisterRequest request) {
        try {
            authService.preRegister(request);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Étape 2 — vérifie le code et crée le compte
    @PostMapping("/verify")
    public ResponseEntity<?> verify(@RequestBody VerifyRequest request) {
        try {
            AuthResponse response = authService.verifyAndRegister(request.getEmail(), request.getCode());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Renvoyer le code
    @PostMapping("/resend-code")
    public ResponseEntity<?> resendCode(@RequestBody ResendRequest request) {
        try {
            authService.resendCode(request.getEmail());
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}