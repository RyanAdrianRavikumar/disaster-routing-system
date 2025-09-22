package com.tursa.shelterroute.entity;

import java.util.ArrayList;
import java.util.List;

public class Node {
    private String shelterId;
    private String name;
    private double latitude;
    private double longitude;
    private int capacity;
    private List<String> queue = new ArrayList<>();

    public Node() {}

    public Node(String shelterId, String name, double latitude, double longitude, int capacity) {
        this.shelterId = shelterId;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.capacity = capacity;
    }

    public String getShelterId() { return shelterId; }
    public void setShelterId(String shelterId) { this.shelterId = shelterId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public List<String> getQueue() { return queue; }
    public void setQueue(List<String> queue) { this.queue = queue; }

    public boolean isFull() {
        return queue.size() >= capacity;
    }

    public int getRemainingCapacity() {
        return capacity - queue.size();
    }

    public boolean enqueue(String rfidTag) {
        if (!isFull()) {
            queue.add(rfidTag);
            return true;
        }
        return false;
    }

    public String dequeue() {
        if (queue.isEmpty()) {
            return null;
        }
        return queue.remove(0);
    }
}