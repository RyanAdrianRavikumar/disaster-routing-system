package com.tursa.shelter.queue;

import java.util.ArrayList;
import java.util.List;
import com.google.firebase.database.Exclude;

public class ShelterQueue {
    private String[] queue;
    private int front;
    private int rear;
    private int size;
    private int capacity;

    // No-args constructor for Firebase
    public ShelterQueue() {
    }

    public ShelterQueue(int capacity) {
        this.capacity = capacity;
        this.queue = new String[capacity];
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
        queue[front] = null; // Clear the reference
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

    // Firebase serialization methods - convert array to list
    public List<String> getQueueList() {
        List<String> list = new ArrayList<>();
        if (queue != null && size > 0) {
            // Add elements in queue order (from front to rear)
            for (int i = 0; i < size; i++) {
                int index = (front + i) % capacity;
                if (queue[index] != null) {
                    list.add(queue[index]);
                }
            }
        }
        return list;
    }

    public void setQueueList(List<String> queueList) {
        if (queueList == null) {
            queueList = new ArrayList<>();
        }

        // Reconstruct array from list
        if (capacity <= 0) {
            capacity = Math.max(queueList.size(), 10); // Default capacity
        }

        this.queue = new String[capacity];
        this.front = 0;
        this.rear = -1;
        this.size = 0;

        // Add all elements from list back to queue
        for (String item : queueList) {
            if (item != null && !isFull()) {
                enqueue(item);
            }
        }
    }

    // Exclude array from Firebase serialization
    @Exclude
    public String[] getQueue() { return queue; }
    @Exclude
    public void setQueue(String[] queue) { this.queue = queue; }

    // Other getters/setters for Firebase
    public int getFront() { return front; }
    public void setFront(int front) { this.front = front; }

    public int getRear() { return rear; }
    public void setRear(int rear) { this.rear = rear; }

    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) {
        this.capacity = capacity;
        if (this.queue == null || this.queue.length != capacity) {
            // Preserve existing data when resizing
            String[] oldQueue = this.queue;
            int oldSize = this.size;
            int oldFront = this.front;

            this.queue = new String[capacity];
            this.front = 0;
            this.rear = -1;
            this.size = 0;

            // Re-add existing elements if any
            if (oldQueue != null && oldSize > 0) {
                for (int i = 0; i < oldSize && i < capacity; i++) {
                    int index = (oldFront + i) % oldQueue.length;
                    if (oldQueue[index] != null) {
                        enqueue(oldQueue[index]);
                    }
                }
            }
        }
    }
}
