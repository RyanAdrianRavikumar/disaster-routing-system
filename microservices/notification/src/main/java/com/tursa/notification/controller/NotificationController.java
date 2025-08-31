package com.tursa.notification.controller;

import com.tursa.notification.dto.BroadcastRequest;
import com.tursa.notification.dto.NotificationRequest;
import com.tursa.notification.model.Notification;
import com.tursa.notification.service.NotificationService;
import com.tursa.notification.util.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);

    @PostMapping("/evacuation")
    public ResponseEntity<ApiResponse<Notification>> sendEvacuationAlert(@RequestBody NotificationRequest request) {
        try {
            if (request.getUserRfid() == null || request.getPhoneNumber() == null) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(false, "User RFID and phone number are required", null));
            }

            Notification notification = notificationService.sendEvacuationAlert(
                    request.getUserRfid(),
                    request.getPhoneNumber(),
                    request.getShelterName()
            );

            return ResponseEntity.ok(new ApiResponse<>(true, "Evacuation alert sent successfully", notification));

        } catch (Exception e) {
            logger.error("Error sending evacuation alert: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Internal server error", null));
        }
    }

    @PostMapping("/route-update")
    public ResponseEntity<ApiResponse<Notification>> sendRouteUpdate(@RequestBody NotificationRequest request) {
        try {
            if (request.getUserRfid() == null || request.getPhoneNumber() == null || request.getNewRoute() == null) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(false, "User RFID, phone number, and new route are required", null));
            }

            Notification notification = notificationService.sendRouteUpdateAlert(
                    request.getUserRfid(),
                    request.getPhoneNumber(),
                    request.getNewRoute()
            );

            return ResponseEntity.ok(new ApiResponse<>(true, "Route update sent successfully", notification));

        } catch (Exception e) {
            logger.error("Error sending route update: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Internal server error", null));
        }
    }

    @PostMapping("/emergency")
    public ResponseEntity<ApiResponse<Notification>> sendEmergencyAlert(@RequestBody NotificationRequest request) {
        try {
            if (request.getUserRfid() == null || request.getPhoneNumber() == null || request.getMessage() == null) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(false, "User RFID, phone number, and message are required", null));
            }

            Notification notification = notificationService.sendEmergencyAlert(
                    request.getUserRfid(),
                    request.getPhoneNumber(),
                    request.getMessage()
            );

            return ResponseEntity.ok(new ApiResponse<>(true, "Emergency alert sent successfully", notification));

        } catch (Exception e) {
            logger.error("Error sending emergency alert: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Internal server error", null));
        }
    }

    /*@PostMapping("/rescue-update")
    public ResponseEntity<ApiResponse<Notification>> sendRescueUpdate(@RequestBody NotificationRequest request) {
        try {
            Notification notification = notificationService.sendRescueUpdate(
                    request.getUserRfid(),
                    request.getPhoneNumber(),
                    request.getMessage()
            );

            return ResponseEntity.ok(new ApiResponse<>(true, "Rescue update sent successfully", notification));

        } catch (Exception e) {
            logger.error("Error sending rescue update: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Internal server error", null));
        }
    }

    @PostMapping("/info")
    public ResponseEntity<ApiResponse<Notification>> sendInfoAlert(@RequestBody NotificationRequest request) {
        try {
            Notification notification = notificationService.sendInfoAlert(
                    request.getUserRfid(),
                    request.getPhoneNumber(),
                    request.getMessage()
            );

            return ResponseEntity.ok(new ApiResponse<>(true, "Info alert sent successfully", notification));

        } catch (Exception e) {
            logger.error("Error sending info alert: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Internal server error", null));
        }
    }

    @GetMapping("/user/{userRfid}")
    public ResponseEntity<ApiResponse<List<Notification>>> getUserNotifications(@PathVariable String userRfid) {
        try {
            List<Notification> notifications = notificationService.getUserNotifications(userRfid);
            return ResponseEntity.ok(new ApiResponse<>(true, "Notifications retrieved successfully", notifications));

        } catch (Exception e) {
            logger.error("Error retrieving notifications for user {}: {}", userRfid, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Internal server error", null));
        }
    }

    @GetMapping("/unread")
    public ResponseEntity<ApiResponse<List<Notification>>> getUnreadNotifications() {
        try {
            List<Notification> notifications = notificationService.getUnreadNotifications();
            return ResponseEntity.ok(new ApiResponse<>(true, "Unread notifications retrieved", notifications));

        } catch (Exception e) {
            logger.error("Error retrieving unread notifications: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Internal server error", null));
        }
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<String>> markAsRead(@PathVariable Long id) {
        try {
            notificationService.markNotificationAsRead(id);
            return ResponseEntity.ok(new ApiResponse<>(true, "Notification marked as read", "Success"));

        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error marking notification as read: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Internal server error", null));
        }
    }

    @PutMapping("/user/{userRfid}/read-all")
    public ResponseEntity<ApiResponse<Integer>> markAllAsRead(@PathVariable String userRfid) {
        try {
            int count = notificationService.markAllAsReadForUser(userRfid);
            return ResponseEntity.ok(new ApiResponse<>(true, "All notifications marked as read", count));

        } catch (Exception e) {
            logger.error("Error marking all notifications as read: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Internal server error", null));
        }
    }

    @GetMapping("/user/{userRfid}/unread-count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(@PathVariable String userRfid) {
        try {
            long count = notificationService.getUnreadCount(userRfid);
            return ResponseEntity.ok(new ApiResponse<>(true, "Unread count retrieved", count));

        } catch (Exception e) {
            logger.error("Error retrieving unread count for user {}: {}", userRfid, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Internal server error", null));
        }
    }

    @PostMapping("/broadcast")
    public ResponseEntity<ApiResponse<List<Notification>>> broadcastEmergencyAlert(@RequestBody BroadcastRequest request) {
        try {
            if (request.getUserRfids() == null || request.getUserRfids().isEmpty() || request.getMessage() == null) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(false, "User RFIDs and message are required", null));
            }

            List<Notification> notifications = notificationService.broadcastEmergencyAlert(
                    request.getUserRfids(),
                    request.getMessage()
            );

            return ResponseEntity.ok(new ApiResponse<>(true, "Broadcast sent successfully", notifications));

        } catch (Exception e) {
            logger.error("Error broadcasting emergency alert: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Internal server error", null));
        }
    }*/
}
