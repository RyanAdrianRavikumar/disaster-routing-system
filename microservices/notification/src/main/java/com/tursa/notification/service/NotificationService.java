package com.tursa.notification.service;

import com.tursa.notification.dto.UserDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Service
public class NotificationService {

    private final JavaMailSender mailSender;
    private final RestTemplate restTemplate;

    // inject RestTemplate & mailSender
    public NotificationService(JavaMailSender mailSender, RestTemplate restTemplate) {
        this.mailSender = mailSender;
        this.restTemplate = restTemplate;
    }

    // Send notification to all users
    public String sendNotification(String message) {
        System.out.println("Sending notification: " + message);

        // Call User microservice
        ResponseEntity<UserDTO[]> response =
                restTemplate.getForEntity("http://localhost:8082/users/all", UserDTO[].class);

        if (response.getBody() == null) {
            return "❌ No users found or service unavailable";
        }

        List<UserDTO> users = Arrays.asList(response.getBody());

        int sentCount = 0;
        for (UserDTO user : users) {
            if (user.getEmail() != null && !user.getEmail().isEmpty()) {
                sendEmail(user.getEmail(), "Disaster Notification", message);
                sentCount++;
            }
        }

        return "✅ Notification sent to " + sentCount + " users.";
    }

    // Email-specific method
    public String sendEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setTo(to);
            mailMessage.setSubject(subject);
            mailMessage.setText(text);

            mailSender.send(mailMessage);

            System.out.println("📧 Email sent to " + to);
            return "Email sent to " + to + " with subject: " + subject;
        } catch (Exception e) {
            System.err.println("❌ Failed to send email to " + to + ": " + e.getMessage());
            return "Failed to send email: " + e.getMessage();
        }
    }
}
