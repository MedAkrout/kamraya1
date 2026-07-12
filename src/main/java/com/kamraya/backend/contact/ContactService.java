package com.kamraya.backend.contact;

import com.kamraya.backend.mail.BrevoMailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ContactService {

    private final BrevoMailService brevoMailService;

    public void sendContactEmail(ContactRequest request) {
        String text =
            "Nom : " + request.getName() + "\n" +
            "Email : " + request.getEmail() + "\n" +
            "Téléphone : " + request.getPhone() + "\n\n" +
            "Message :\n" + request.getMessage();

        brevoMailService.send(
            new String[]{"nouranemesfar@gmail.com"},
            "Nouveau message de " + request.getName() + " — Kamraya",
            text,
            request.getEmail()
        );
    }
}