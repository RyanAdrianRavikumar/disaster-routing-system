package com.tursa.user.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.firebase.database.*;
import com.tursa.user.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

@Service
public class FirebaseRealtimeService {
    private final DatabaseReference database;

    public FirebaseRealtimeService() {
        this.database = FirebaseDatabase.getInstance().getReference("users");
    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        try {
            String url = "https://disasterhub-fbe9c-default-rtdb.asia-southeast1.firebasedatabase.app/users.json";
            RestTemplate restTemplate = new RestTemplate();

            // Create ObjectMapper and register JavaTimeModule
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());

            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response != null) {
                for (Map.Entry<String, Object> entry : response.entrySet()) {
                    User user = mapper.convertValue(entry.getValue(), User.class);
                    users.add(user);
                }
            }
        } catch (Exception e) {
            e.printStackTrace(); // see full cause
            throw new RuntimeException("Failed to fetch users from Firebase", e);
        }
        return users;
    }

    // Save full user to Firebase
    public void saveUser(User user) {
        try {
            DatabaseReference userRef = database.child(user.getRfid());
            userRef.child("name").setValueAsync(user.getName());
            userRef.child("email").setValueAsync(user.getEmail());
            userRef.child("phoneNumber").setValueAsync(user.getPhoneNumber());
            userRef.child("rfid").setValueAsync(user.getRfid());
            userRef.child("status").setValueAsync(user.getStatus() != null ? user.getStatus().name() : null);
            userRef.child("rescuePriority").setValueAsync(user.getRescuePriority());
            userRef.child("familyCount").setValueAsync(user.getFamilyCount());
            userRef.child("childrenCount").setValueAsync(user.getChildrenCount());
            userRef.child("elderlyCount").setValueAsync(user.getElderlyCount());
            userRef.child("currentLatitude").setValueAsync(user.getCurrentLatitude());
            userRef.child("currentLongitude").setValueAsync(user.getCurrentLongitude());
            userRef.child("createdAt").setValueAsync(user.getCreatedAt() != null ? user.getCreatedAt().toString() : null);
            userRef.child("updatedAt").setValueAsync(user.getUpdatedAt() != null ? user.getUpdatedAt().toString() : null);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save user to Firebase", e);
        }
    }

    public void updateUserLocation(String rfid, Double latitude, Double longitude) {
        try {
            DatabaseReference userRef = database.child(rfid).child("location");
            userRef.child("latitude").setValueAsync(latitude);
            userRef.child("longitude").setValueAsync(longitude);
        } catch (Exception e) {
            throw new RuntimeException("Failed to update Firebase location", e);
        }
    }

    public void updateUserStatus(String rfid, String status) {
        try {
            database.child(rfid).child("status").setValueAsync(status);
        } catch (Exception e) {
            throw new RuntimeException("Failed to update Firebase status", e);
        }
    }

    public void updateRescuePriority(String rfid, int priority) {
        try {
            database.child(rfid).child("rescuePriority").setValueAsync(priority);
        } catch (Exception e) {
            throw new RuntimeException("Failed to update Firebase rescue priority", e);
        }
    }
}
