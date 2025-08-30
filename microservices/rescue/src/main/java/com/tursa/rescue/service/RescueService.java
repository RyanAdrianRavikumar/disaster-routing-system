package com.tursa.rescue.service;

import com.tursa.rescue.dto.UserDTO;
import com.tursa.rescue.entity.RescueQueue;
import com.tursa.rescue.util.MergeSort;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class RescueService {
    private RescueQueue rescueQueue = new RescueQueue(50);

    public String enqueueUsers(List<UserDTO> users) {
        UserDTO[] arr = users.toArray(new UserDTO[0]);
        MergeSort.sort(arr, 0, arr.length - 1);

        int count = 0;
        for (UserDTO user : arr) {
            try {
                rescueQueue.enqueue(user);
                count++;
            } catch (Exception e) {
                break;
            }
        }
        return count + " users enqueued successfully";
    }

    public UserDTO rescueNextUser() throws Exception {
        return rescueQueue.dequeue();
    }

    public UserDTO peekNextUser() throws Exception {
        return rescueQueue.peek();
    }

    public int queueSize() {
        return rescueQueue.size();
    }

    public boolean isQueueEmpty() {
        return rescueQueue.isEmpty();
    }

    public boolean isQueueFull() {
        return rescueQueue.isFull();
    }
}
