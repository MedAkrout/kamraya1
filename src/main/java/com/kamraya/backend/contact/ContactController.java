package com.kamraya.backend.contact;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contact")
@RequiredArgsConstructor
public class ContactController {

    private final ContactService contactService;

    @PostMapping
    public ResponseEntity<String> sendMessage(@RequestBody ContactRequest request) {
        contactService.sendContactEmail(request);
        return ResponseEntity.ok("Message envoyé !");
    }
}