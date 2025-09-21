package com.tursa.shelter.entity;

import com.tursa.shelter.queue.ShelterQueue;
import com.google.firebase.database.Exclude;

public class Shelter {
    private String shelterId;
    private String name;
    private int capacity;
    private ShelterQueue queue;
    private Double latitude;  // New field
    private Double longitude; // New field

    // No-args constructor for Firebase
    public Shelter() {
    }

    public Shelter(String shelterId, String name, int capacity, Double latitude, Double longitude) {
        this.shelterId = shelterId;
        this.name = name;
        this.capacity = capacity;
        this.latitude = latitude;
        this.longitude = longitude;
        this.queue = new ShelterQueue(capacity);
    }

    // Getters
    public String getShelterId() { return shelterId; }
    public String getName() { return name; }
    public int getCapacity() { return capacity; }

    public ShelterQueue getQueue() {
        if (queue == null) {
            queue = new ShelterQueue(capacity);
        }
        return queue;
    }


    // Setters
    public void setShelterId(String shelterId) { this.shelterId = shelterId; }
    public void setName(String name) { this.name = name; }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
        // Update queue capacity if it exists
        if (queue != null) {
            queue.setCapacity(capacity);
        }
    }

    public void setQueue(ShelterQueue queue) {
        this.queue = queue;
        // Ensure queue capacity is set correctly
        if (queue != null && capacity > 0) {
            queue.setCapacity(capacity);
        }
    }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
}