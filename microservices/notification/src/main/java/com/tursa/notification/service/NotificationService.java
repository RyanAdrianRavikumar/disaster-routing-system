package com.tursa.notification.service;

import com.tursa.notification.model.Notification;
import com.tursa.notification.repository.NotificationRepository;
import com.tursa.notification.sms.SmsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private SmsService smsService;

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    public Notification sendEvacuationAlert(String userRfid, String phoneNumber, String shelterName) {
        try {
            String message = "EMERGENCY: Please evacuate immediately to " + shelterName +
                    ". Follow the evacuation route provided in the app.";

            Notification notification = createNotification(userRfid, phoneNumber, message,
                    Notification.NotificationType.EVACUATION, 5); // High priority

            // Send SMS (simulated via SmsService)
            if (phoneNumber != null && !phoneNumber.isEmpty()) {
                smsService.sendSms(phoneNumber, message);
            }

            logger.info("Sent evacuation alert to user: {}", userRfid);
            return notification;

        } catch (Exception e) {
            logger.error("Error sending evacuation alert to user {}: {}", userRfid, e.getMessage());
            throw new RuntimeException("Failed to send evacuation alert", e);
        }
    }

    public Notification sendRouteUpdateAlert(String userRfid, String phoneNumber, String newRoute) {
        try {
            String message = "ROUTE UPDATE: Your evacuation route has been updated due to hazards. " +
                    "New route: " + newRoute + ". Please check the app for details.";

            Notification notification = createNotification(userRfid, phoneNumber, message,
                    Notification.NotificationType.ROUTE_UPDATE, 4); // Medium-high priority

            if (phoneNumber != null && !phoneNumber.isEmpty()) {
                smsService.sendSms(phoneNumber, message);
            }

            logger.info("Sent route update to user: {}", userRfid);
            return notification;

        } catch (Exception e) {
            logger.error("Error sending route update to user {}: {}", userRfid, e.getMessage());
            throw new RuntimeException("Failed to send route update", e);
        }
    }

    public Notification sendEmergencyAlert(String userRfid, String phoneNumber, String alertMessage) {
        try {
            Notification notification = createNotification(userRfid, phoneNumber, alertMessage,
                    Notification.NotificationType.EMERGENCY, 5); // Critical priority

            if (phoneNumber != null && !phoneNumber.isEmpty()) {
                smsService.sendSms(phoneNumber, alertMessage);
            }

            logger.info("Sent emergency alert to user: {}", userRfid);
            return notification;

        } catch (Exception e) {
            logger.error("Error sending emergency alert to user {}: {}", userRfid, e.getMessage());
            throw new RuntimeException("Failed to send emergency alert", e);
        }
    }

    public Notification sendRescueUpdate(String userRfid, String phoneNumber, String updateMessage) {
        try {
            Notification notification = createNotification(userRfid, phoneNumber, updateMessage,
                    Notification.NotificationType.RESCUE_UPDATE, 4); // High priority

            if (phoneNumber != null && !phoneNumber.isEmpty()) {
                smsService.sendSms(phoneNumber, updateMessage);
            }

            logger.info("Sent rescue update to user: {}", userRfid);
            return notification;

        } catch (Exception e) {
            logger.error("Error sending rescue update to user {}: {}", userRfid, e.getMessage());
            throw new RuntimeException("Failed to send rescue update", e);
        }
    }

    public Notification sendInfoAlert(String userRfid, String phoneNumber, String infoMessage) {
        try {
            Notification notification = createNotification(userRfid, phoneNumber, infoMessage,
                    Notification.NotificationType.INFO, 2); // Low priority

            // Don't send SMS for info messages to avoid spam; service retains notification
            logger.info("Saved info notification for user: {}", userRfid);
            return notification;

        } catch (Exception e) {
            logger.error("Error sending info alert to user {}: {}", userRfid, e.getMessage());
            throw new RuntimeException("Failed to send info alert", e);
        }
    }

    private Notification createNotification(String userRfid, String phoneNumber, String message,
                                            Notification.NotificationType type, Integer priority) {
        Notification notification = new Notification();
        notification.setUserRfid(userRfid);
        notification.setPhoneNumber(phoneNumber);
        notification.setMessage(message);
        notification.setType(type);
        notification.setPriority(priority);
        return notificationRepository.save(notification);
    }

    public List<Notification> getUserNotifications(String userRfid) {
        return notificationRepository.findByUserRfidOrderBySentAtDesc(userRfid);
    }

    public List<Notification> getUnreadNotifications() {
        return notificationRepository.findUnreadNotificationsOrderByPriority();
    }

    @Transactional
    public void markNotificationAsRead(Long notificationId) {
        try {
            Notification notification = notificationRepository.findById(notificationId)
                    .orElseThrow(() -> new EntityNotFoundException("Notification not found"));

            notification.setIsRead(true);
            notificationRepository.save(notification);

        } catch (Exception e) {
            logger.error("Error marking notification as read: {}", e.getMessage());
            throw new RuntimeException("Failed to mark notification as read", e);
        }
    }

    @Transactional
    public int markAllAsReadForUser(String userRfid) {
        try {
            return notificationRepository.markAllAsReadForUser(userRfid);

        } catch (Exception e) {
            logger.error("Error marking all notifications as read for user {}: {}", userRfid, e.getMessage());
            throw new RuntimeException("Failed to mark notifications as read", e);
        }
    }

    public long getUnreadCount(String userRfid) {
        return notificationRepository.countUnreadByUserRfid(userRfid);
    }

    public List<Notification> broadcastEmergencyAlert(List<String> userRfids, String message) {
        List<Notification> notifications = new ArrayList<>();

        for (String userRfid : userRfids) {
            try {
                // In a real system, you'd get phone number from user service
                String phoneNumber = "BROADCAST"; // Placeholder
                Notification notification = sendEmergencyAlert(userRfid, phoneNumber, message);
                notifications.add(notification);

            } catch (Exception e) {
                logger.error("Failed to send broadcast to user {}: {}", userRfid, e.getMessage());
            }
        }

        return notifications;
    }
}
