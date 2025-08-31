package com.tursa.shelter.queue;

public class ShelterQueue {
    private String[] queue;
    private int front;
    private int rear;
    private int size;
    private int capacity;

    public ShelterQueue(int capacity) {
        this.capacity = capacity;
        this.queue = new String[capacity]; // store RFID tags
        this.front = 0;
        this.rear = -1;
        this.size = 0;
    }

    public boolean enqueue(String rfidTag) {
        if (isFull()) return false;
        rear = (rear + 1) % capacity;
        queue[rear] = rfidTag;
        size++;
        return true;
    }

    public String dequeue() {
        if (isEmpty()) return null;
        String user = queue[front];
        front = (front + 1) % capacity;
        size--;
        return user;
    }

    public String peek() {
        if (isEmpty()) return null;
        return queue[front];
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public boolean isFull() {
        return size == capacity;
    }

    public int size() {
        return size;
    }

    public int getRemainingCapacity() {
        return capacity - size;
    }
}
