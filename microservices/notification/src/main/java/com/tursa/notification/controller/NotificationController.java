package com.tursa.notification.controller;

import com.tursa.notification.service.NotificationService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/disaster")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("/simulate")
    public String simulateDisaster(@RequestParam String disasterType, @RequestParam String description) {
        String fullMessage = formatDisasterMessage(disasterType, description);
        return notificationService.sendNotification(fullMessage);
    }

    @PostMapping("/message")
    public String sendCustomMessage(@RequestParam String message) {
        return notificationService.sendNotification(message);
    }

    private String formatDisasterMessage(String disasterType, String description) {
        return "🚨 Disaster Alert: " + disasterType + " 🚨\n" + description;
    }
}
