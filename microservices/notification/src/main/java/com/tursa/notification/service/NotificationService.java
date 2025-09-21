package com.tursa.notification.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private final JavaMailSender mailSender;

    public NotificationService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // Send a notification (via email + console log)
    public String sendNotification(String message) {
        System.out.println("Sending notification: " + message);

        // Send email
        sendEmail("recipient@example.com", "Disaster Notification", message);

        return "Notification sent (email + log): " + message;
    }

    // Email-specific method
    public String sendEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setTo(to);
            mailMessage.setSubject(subject);
            mailMessage.setText(text);

            mailSender.send(mailMessage);

            return "Email sent to " + to + " with subject: " + subject;
        } catch (Exception e) {
            return "Failed to send email: " + e.getMessage();
        }
    }
}
