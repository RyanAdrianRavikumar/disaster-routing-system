package com.tursa.user.service;

import com.tursa.user.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class UserService {

    @Autowired
    private FirebaseRealtimeService firebaseService;

    // Fetch all users from Firebase
    public List<User> getAllUsers() {
        return firebaseService.getAllUsers();
    }

    // Create a new user in Firebase
    public User createUser(User user) {
        if (user.getRfid() == null || user.getName() == null) {
            throw new IllegalArgumentException("RFID and name are required");
        }

        // Check for duplicate RFID in Firebase
        List<User> allUsers = getAllUsers();
        for (User existing : allUsers) {
            if (existing.getRfid().equals(user.getRfid())) {
                throw new IllegalArgumentException("User with RFID " + user.getRfid() + " already exists");
            }
        }

        firebaseService.saveUser(user);
        return user;
    }

    // Find a user by RFID
    public User findByRfid(String rfid) {
        List<User> allUsers = getAllUsers();
        for (User user : allUsers) {
            if (rfid.equals(user.getRfid())) {
                return user;
            }
        }
        throw new RuntimeException("User not found with RFID: " + rfid);
    }

    // Update user's location
    public User updateLocation(String rfid, Double latitude, Double longitude) {
        User user = findByRfid(rfid);
        user.setCurrentLatitude(latitude);
        user.setCurrentLongitude(longitude);
        firebaseService.updateUserLocation(rfid, latitude, longitude);
        return user;
    }

    // Get users needing rescue, sorted by priority
    public List<User> getUsersNeedingRescue() {
        List<User> allUsers = getAllUsers();
        List<User> needingRescue = new ArrayList<>();
        for (User user : allUsers) {
            if (user.getStatus() == User.UserStatus.NEEDS_RESCUE) {
                needingRescue.add(user);
            }
        }
        needingRescue.sort((u1, u2) -> u2.getRescuePriority().compareTo(u1.getRescuePriority()));
        return needingRescue;
    }

    // Update user status
    public User updateUserStatus(String rfid, User.UserStatus status) {
        User user = findByRfid(rfid);
        user.setStatus(status);

        if (status == User.UserStatus.NEEDS_RESCUE) {
            int priority = calculateRescuePriority(user);
            user.setRescuePriority(priority);
            firebaseService.updateRescuePriority(rfid, priority);
        }

        firebaseService.updateUserStatus(rfid, status.name());
        return user;
    }

    // Update family info
    public User updateFamilyInfo(String rfid, Integer familyCount, Integer childrenCount, Integer elderlyCount) {
        User user = findByRfid(rfid);
        user.setFamilyCount(familyCount);
        user.setChildrenCount(childrenCount);
        user.setElderlyCount(elderlyCount);

        if (user.getStatus() == User.UserStatus.NEEDS_RESCUE) {
            int priority = calculateRescuePriority(user);
            user.setRescuePriority(priority);
            firebaseService.updateRescuePriority(rfid, priority);
        }

        firebaseService.saveUser(user); // Save updated info
        return user;
    }

    // Calculate rescue priority
    private int calculateRescuePriority(User user) {
        int priority = 1;
        if (user.getChildrenCount() != null) priority += user.getChildrenCount() * 3;
        if (user.getElderlyCount() != null) priority += user.getElderlyCount() * 2;
        if (user.getFamilyCount() != null) priority += Math.max(0, user.getFamilyCount() - 2);
        return Math.min(priority, 10);
    }

    public List<User> getUsersInArea(Double centerLat, Double centerLon, Double radiusKm) {
        List<User> allUsers = getAllUsers();
        List<User> usersInArea = new ArrayList<>();

        for (User user : allUsers) {
            Double userLat = user.getCurrentLatitude();
            Double userLon = user.getCurrentLongitude();
            if (userLat != null && userLon != null) {
                double distance = calculateDistance(centerLat, centerLon, userLat, userLon);
                if (distance <= radiusKm) {
                    usersInArea.add(user);
                }
            }
        }

        return usersInArea;
    }

    // Haversine formula to calculate distance in km between two points
    private double calculateDistance(Double lat1, Double lon1, Double lat2, Double lon2) {
        final int R = 6371; // Earth radius in km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

}
