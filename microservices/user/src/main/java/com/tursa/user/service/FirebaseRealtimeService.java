package com.tursa.user.service;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseRealtimeService {
    private final DatabaseReference database;

    public FirebaseRealtimeService() {
        this.database = FirebaseDatabase.getInstance().getReference("users");
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
            database.child(rfid).child("priority").setValueAsync(priority);
        } catch (Exception e) {
            throw new RuntimeException("Failed to update Firebase rescue priority", e);
        }
    }
}
