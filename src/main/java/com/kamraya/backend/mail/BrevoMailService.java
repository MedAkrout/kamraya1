package com.kamraya.backend.mail;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class BrevoMailService {

    private final RestTemplate restTemplate;

    @Value("${brevo.api.key}")
    private String apiKey;

    @Value("${brevo.sender.email}")
    private String senderEmail;

    private static final String BREVO_URL = "https://api.brevo.com/v3/smtp/email";

    public BrevoMailService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void send(String toEmail, String subject, String textContent) {
        send(new String[]{toEmail}, subject, textContent, null);
    }

    public void send(String[] toEmails, String subject, String textContent, String replyTo) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("api-key", apiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));

            Map<String, Object> body = new HashMap<>();

            Map<String, String> sender = new HashMap<>();
            sender.put("email", senderEmail);
            body.put("sender", sender);

            List<Map<String, String>> toList = new ArrayList<>();
            for (String email : toEmails) {
                Map<String, String> to = new HashMap<>();
                to.put("email", email);
                toList.add(to);
            }
            body.put("to", toList);

            body.put("subject", subject);
            body.put("textContent", textContent);

            if (replyTo != null && !replyTo.isBlank()) {
                Map<String, String> replyToMap = new HashMap<>();
                replyToMap.put("email", replyTo);
                body.put("replyTo", replyToMap);
            }

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            restTemplate.postForEntity(BREVO_URL, request, String.class);

            log.info("=== MAIL ENVOYÉ via Brevo à {}", String.join(", ", toEmails));
        } catch (Exception e) {
            log.error("=== MAIL ERROR (Brevo): {} — {}", e.getClass().getName(), e.getMessage());
        }
    }
}