package com.tursa.shelter.entity;

import com.tursa.shelter.queue.ShelterQueue;

public class Shelter {
    private String shelterId;
    private String name;
    private int capacity;
    private ShelterQueue queue;

    public Shelter(String shelterId, String name, int capacity) {
        this.shelterId = shelterId;
        this.name = name;
        this.capacity = capacity;
        this.queue = new ShelterQueue(capacity);
    }

    public String getShelterId() {
        return shelterId;
    }

    public String getName() {
        return name;
    }

    public int getCapacity() {
        return capacity;
    }

    public ShelterQueue getQueue() {
        return queue;
    }

    public int getRemainingCapacity() {
        return capacity - queue.size();
    }
}
