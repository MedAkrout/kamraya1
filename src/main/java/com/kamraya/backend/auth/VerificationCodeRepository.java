package com.kamraya.backend.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Long> {
    Optional<VerificationCode> findTopByEmailOrderByIdDesc(String email);
    void deleteByEmail(String email);
}