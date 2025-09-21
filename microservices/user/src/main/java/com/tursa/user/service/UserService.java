package com.tursa.user.service;

import com.tursa.user.entity.User;
import com.tursa.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FirebaseRealtimeService firebaseService;

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User createUser(User user) {
        try {
            // Validation
            if (user.getRfid() == null || user.getName() == null) {
                throw new IllegalArgumentException("RFID and name are required");
            }

            // Check for duplicate RFID in MySQL
            if (userRepository.findByRfid(user.getRfid()).isPresent()) {
                throw new IllegalArgumentException("User with RFID " + user.getRfid() + " already exists");
            }

            // Save to MySQL
            User savedUser = userRepository.save(user);
            logger.info("Created new user in MySQL: {}", savedUser.getRfid());

            // Save to Firebase
            try {
                firebaseService.saveUser(savedUser);
                logger.info("Created new user in Firebase: {}", savedUser.getRfid());
            } catch (Exception e) {
                logger.error("Failed to save user {} to Firebase: {}", savedUser.getRfid(), e.getMessage());
            }

            return savedUser;

        } catch (Exception e) {
            logger.error("Error creating user: {}", e.getMessage());
            throw new RuntimeException("Failed to create user", e);
        }
    }

    public User findByRfid(String rfid) {
        return userRepository.findByRfid(rfid).orElseThrow(() -> new EntityNotFoundException("User not found with RFID: " + rfid));
    }

    public User updateLocation(String rfid, Double latitude, Double longitude) {
        try {
            User user = findByRfid(rfid);
            user.setCurrentLatitude(latitude);
            user.setCurrentLongitude(longitude);

            User updatedUser = userRepository.save(user);

            // Update Firebase real-time location
            firebaseService.updateUserLocation(rfid, latitude, longitude);

            logger.info("Updated location for user: {} to ({}, {})", rfid, latitude, longitude);
            return updatedUser;

        } catch (Exception e) {
            logger.error("Error updating location for user {}: {}", rfid, e.getMessage());
            throw new RuntimeException("Failed to update user location", e);
        }
    }

    public List<User> getUsersNeedingRescue() {
        return userRepository.findByStatusOrderByPriorityDesc(User.UserStatus.NEEDS_RESCUE);
    }

    public User updateUserStatus(String rfid, User.UserStatus status) {
        try {
            User user = findByRfid(rfid);
            user.setStatus(status);

            // Calculate rescue priority based on status and family
            if (status == User.UserStatus.NEEDS_RESCUE) {
                int priority = calculateRescuePriority(user);
                user.setRescuePriority(priority);
            }

            User updatedUser = userRepository.save(user);
            logger.info("Updated status for user {} to {}", rfid, status);
            return updatedUser;

        } catch (Exception e) {
            logger.error("Error updating status for user {}: {}", rfid, e.getMessage());
            throw new RuntimeException("Failed to update user status", e);
        }
    }

    public List<User> getUsersInArea(double centerLat, double centerLon, double radiusKm) {
        double latRange = radiusKm / 111.0; // Approximate km per degree of latitude
        double lonRange = radiusKm / (111.0 * Math.cos(Math.toRadians(centerLat)));

        double minLat = centerLat - latRange;
        double maxLat = centerLat + latRange;
        double minLon = centerLon - lonRange;
        double maxLon = centerLon + lonRange;

        return userRepository.findUsersInArea(minLat, maxLat, minLon, maxLon);
    }

    public User updateFamilyInfo(String rfid, Integer familyCount, Integer childrenCount, Integer elderlyCount) {
        try {
            User user = findByRfid(rfid);
            user.setFamilyCount(familyCount);
            user.setChildrenCount(childrenCount);
            user.setElderlyCount(elderlyCount);

            // Recalculate rescue priority if user needs rescue
            if (user.getStatus() == User.UserStatus.NEEDS_RESCUE) {
                int priority = calculateRescuePriority(user);
                user.setRescuePriority(priority);
            }

            return userRepository.save(user);

        } catch (Exception e) {
            logger.error("Error updating family info for user {}: {}", rfid, e.getMessage());
            throw new RuntimeException("Failed to update family information", e);
        }
    }

    // Method to determine rescue priority
    private int calculateRescuePriority(User user) {
        int priority = 1; // Base priority

        // Higher priority for vulnerable populations
        if (user.getChildrenCount() != null) {
            priority += user.getChildrenCount() * 3;
        }
        if (user.getElderlyCount() != null) {
            priority += user.getElderlyCount() * 2;
        }
        if (user.getFamilyCount() != null) {
            priority += Math.max(0, user.getFamilyCount() - 2); // Extra points for larger families
        }

        return Math.min(priority, 10); // Cap at 10
    }
}
