package com.tursa.rescue.service;

import com.tursa.rescue.dto.UserDTO;
import com.tursa.rescue.entity.RescueQueue;
import com.tursa.rescue.util.MergeSort;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RescueService {

    private final RescueQueue rescueQueue = new RescueQueue(50);
    private final RestTemplate restTemplate = new RestTemplate(); // can be @Autowired

    private static final String USER_SERVICE_URL = "http://localhost:8082/users/all"; // adjust as needed

    // Fetch users from User microservice, convert to DTOs, sort, and enqueue
    public String enqueueUsersFromUserService() {
        List<UserDTO> users = fetchUsersFromUserService();

        // Convert to array for merge sort
        UserDTO[] arr = users.toArray(new UserDTO[0]);
        MergeSort.sort(arr, 0, arr.length - 1); // adjust sort to descending if needed

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

    // Fetch users via REST call
    private List<UserDTO> fetchUsersFromUserService() {
        UserDTO[] usersArray = restTemplate.getForObject(USER_SERVICE_URL, UserDTO[].class);
        if (usersArray == null) {
            return List.of();
        }
        return Arrays.stream(usersArray).collect(Collectors.toList());
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

    public List<UserDTO> getAllQueuedUsers() {
        return rescueQueue.getUsers();
    }
}
