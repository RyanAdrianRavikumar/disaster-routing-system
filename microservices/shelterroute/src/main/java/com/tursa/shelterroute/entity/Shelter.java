package com.tursa.shelterroute.entity;

import java.util.LinkedList;
import java.util.Queue;

public class Shelter {
    private String shelterId; // Added field for shelter ID
    private String name; // Added field for shelter name
    private int capacity; // Added field for total capacity
    private Queue<String> queue; // Added field for user queue (RFID tags)
    private Double latitude; // Added field for latitude
    private Double longitude; // Added field for longitude

    public Shelter() { // Added default constructor for Firebase
        this.queue = new LinkedList<>();
    }

    public Shelter(String shelterId, String name, int capacity, Double latitude, Double longitude) { // Added constructor
        this.shelterId = shelterId;
        this.name = name;
        this.capacity = capacity;
        this.queue = new LinkedList<>();
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Getters
    public String getShelterId() { return shelterId; } // Added getter for shelterId
    public String getName() { return name; } // Added getter for name
    public int getCapacity() { return capacity; } // Added getter for capacity
    public Queue<String> getQueue() { return queue; } // Added getter for queue
    public Double getLatitude() { return latitude; } // Added getter for latitude
    public Double getLongitude() { return longitude; } // Added getter for longitude

    // Setters
    public void setShelterId(String shelterId) { this.shelterId = shelterId; } // Added setter for shelterId
    public void setName(String name) { this.name = name; } // Added setter for name
    public void setCapacity(int capacity) { this.capacity = capacity; } // Added setter for capacity
    public void setQueue(Queue<String> queue) { this.queue = queue; } // Added setter for queue
    public void setLatitude(Double latitude) { this.latitude = latitude; } // Added setter for latitude
    public void setLongitude(Double longitude) { this.longitude = longitude; } // Added setter for longitude

    public boolean isFull() { // Added method to check if shelter is full
        return queue.size() >= capacity;
    }

    public int getRemainingCapacity() { // Added method to get remaining capacity
        return capacity - queue.size();
    }

    public boolean enqueue(String rfidTag) { // Added method to add user to queue
        if (!isFull()) {
            return queue.offer(rfidTag);
        }
        return false;
    }

    public String dequeue() { // Added method to remove user from queue
        return queue.poll();
    }
}