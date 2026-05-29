package com.kamraya.backend.contact;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ContactService {

    private final JavaMailSender mailSender;

    public void sendContactEmail(ContactRequest request) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo("nouranemesfar@gmail.com");
        message.setFrom("nouranemesfar@gmail.com");
        message.setReplyTo(request.getEmail());
        message.setSubject("Nouveau message de " + request.getName() + " — Kamraya");
        message.setText(
            "Nom : " + request.getName() + "\n" +
            "Email : " + request.getEmail() + "\n" +
            "Téléphone : " + request.getPhone() + "\n\n" +
            "Message :\n" + request.getMessage()
        );
        mailSender.send(message);
    }
}