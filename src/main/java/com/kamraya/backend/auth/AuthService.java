package com.kamraya.backend.auth;

import com.kamraya.backend.mail.BrevoMailService;
import com.kamraya.backend.user.User;
import com.kamraya.backend.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final VerificationCodeRepository verificationCodeRepository;
    private final BrevoMailService brevoMailService;

    @Transactional
    public void preRegister(RegisterRequest request) {
        System.out.println("=== preRegister appelé pour: " + request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("EMAIL_EXISTE");
        }

        if (request.getPhone() == null || !request.getPhone().matches("^[2579]\\d{7}$")) {
            throw new RuntimeException("TELEPHONE_INVALIDE");
        }

        verificationCodeRepository.deleteByEmail(request.getEmail());

        String code = String.format("%06d", new Random().nextInt(999999));
        System.out.println("=== Code généré: " + code);

        VerificationCode verif = VerificationCode.builder()
            .email(request.getEmail())
            .code(code)
            .fullName(request.getFullName())
            .password(passwordEncoder.encode(request.getPassword()))
            .phone(request.getPhone())
            .build();
        verificationCodeRepository.save(verif);
        System.out.println("=== Code sauvegardé en base");

        sendVerificationEmail(request.getEmail(), request.getFullName(), code);
        System.out.println("=== sendVerificationEmail terminé");
    }

    @Transactional
    public AuthResponse verifyAndRegister(String email, String code) {
        VerificationCode verif = verificationCodeRepository
            .findTopByEmailOrderByIdDesc(email)
            .orElseThrow(() -> new RuntimeException("CODE_INVALIDE"));

        if (verif.getExpiresAt().isBefore(LocalDateTime.now())) {
            verificationCodeRepository.deleteByEmail(email);
            throw new RuntimeException("CODE_EXPIRE");
        }

        if (!verif.getCode().equals(code)) {
            throw new RuntimeException("CODE_INCORRECT");
        }

        User user = User.builder()
            .fullName(verif.getFullName())
            .email(verif.getEmail())
            .password(verif.getPassword())
            .phone(verif.getPhone())
            .role(User.Role.USER)
            .build();

        userRepository.save(user);
        verificationCodeRepository.deleteByEmail(email);

        String token = jwtService.generateToken(user);

        return AuthResponse.builder()
            .id(user.getId())
            .token(token)
            .email(user.getEmail())
            .fullName(user.getFullName())
            .role(user.getRole().name())
            .phone(user.getPhone())
            .address(user.getAddress())
            .build();
    }

    @Transactional
    public void resendCode(String email) {
        VerificationCode existing = verificationCodeRepository
            .findTopByEmailOrderByIdDesc(email)
            .orElseThrow(() -> new RuntimeException("EMAIL_INTROUVABLE"));

        String code = String.format("%06d", new Random().nextInt(999999));
        existing.setCode(code);
        existing.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        verificationCodeRepository.save(existing);

        sendVerificationEmail(email, existing.getFullName(), code);
    }

    private void sendVerificationEmail(String email, String fullName, String code) {
        System.out.println("=== sendVerificationEmail appelé pour: " + email);

        String text =
            "Bonjour " + fullName + ",\n\n" +
            "Merci de rejoindre Kamraya !\n\n" +
            "Votre code de vérification est :\n\n" +
            "        " + code + "\n\n" +
            "Ce code est valable pendant 10 minutes.\n\n" +
            "Si vous n'avez pas demandé ce code, ignorez ce message.\n\n" +
            "L'équipe Kamraya";

        brevoMailService.send(email, "Votre code de vérification — Kamraya", text);
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getEmail(),
                request.getPassword()
            )
        );

        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        String token = jwtService.generateToken(user);

        return AuthResponse.builder()
            .id(user.getId())
            .token(token)
            .email(user.getEmail())
            .fullName(user.getFullName())
            .role(user.getRole().name())
            .phone(user.getPhone())
            .address(user.getAddress())
            .build();
    }
}