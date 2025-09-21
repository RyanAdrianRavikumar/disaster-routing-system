package com.tursa.shelterroute.entity;

import java.util.ArrayList;
import java.util.List;

public class Shelter {
    private String shelterId; // Primitive string for ID
    private String name; // Primitive string for name
    private int capacity; // Primitive int for capacity
    private List<String> queue = new ArrayList<>(); // List for queue (advanced collection)
    private double latitude; // Primitive double for latitude
    private double longitude; // Primitive double for longitude

    public Shelter() {} // Default constructor for Firebase deserialization

    public Shelter(String shelterId, String name, int capacity, double latitude, double longitude) { // Constructor with primitives
        this.shelterId = shelterId;
        this.name = name;
        this.capacity = capacity;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Getters and setters using primitives
    public String getShelterId() { return shelterId; }
    public void setShelterId(String shelterId) { this.shelterId = shelterId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public List<String> getQueue() { return queue; }
    public void setQueue(List<String> queue) { this.queue = queue; }
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public boolean isFull() { // Check if shelter is full (primitive int comparison)
        return queue.size() >= capacity;
    }

    public int getRemainingCapacity() { // Calculate remaining capacity (primitive int)
        return capacity - queue.size();
    }

    public boolean enqueue(String rfidTag) { // Add to queue (returns primitive boolean)
        if (!isFull()) {
            queue.add(rfidTag);
            return true;
        }
        return false;
    }

    public String dequeue() { // Remove from queue (returns string or null)
        if (queue.isEmpty()) {
            return null;
        }
        return queue.remove(0);
    }
}