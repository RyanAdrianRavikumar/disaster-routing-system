package com.tursa.rescue.entity;

import com.tursa.rescue.dto.UserDTO;

import java.util.ArrayList;
import java.util.List;

public class RescueQueue {
    private UserDTO[] queue;
    private int front;
    private int rear;
    private int capacity;
    private int count;

    public RescueQueue(int size) {
        queue = new UserDTO[size];
        capacity = size;
        front = 0;
        rear = -1;
        count = 0;
    }

    public void enqueue(UserDTO user) throws Exception {
        if (isFull()) throw new Exception("Queue is full");
        rear = (rear + 1) % capacity;
        queue[rear] = user;
        count++;
    }

    public UserDTO dequeue() throws Exception {
        if (isEmpty()) throw new Exception("Queue is empty");
        UserDTO user = queue[front];
        front = (front + 1) % capacity;
        count--;
        return user;
    }

    public UserDTO peek() throws Exception {
        if (isEmpty()) throw new Exception("Queue is empty");
        return queue[front];
    }

    public boolean isEmpty() { return count == 0; }
    public boolean isFull() { return count == capacity; }
    public int size() { return count; }

    public List<UserDTO> getUsers() {
        List<UserDTO> users = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            int index = (front + i) % capacity;
            users.add(queue[index]);
        }
        return users;
    }
}
