package com.tursa.shelter.service;

import com.tursa.shelter.entity.Shelter;
import com.tursa.shelter.repository.ShelterRepository;
import com.tursa.shelter.queue.ShelterQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ShelterService {

    @Autowired
    private ShelterRepository shelterRepository;

    // In-memory cache of shelters
    private final ConcurrentHashMap<String, Shelter> shelters = new ConcurrentHashMap<>();

    // Load all existing shelters from Firebase at startup
    @PostConstruct
    public void init() {
        try {
            List<Shelter> firebaseShelters = shelterRepository.getAllSheltersFromFirebase().join();
            for (Shelter s : firebaseShelters) {
                if (s != null && s.getShelterId() != null) {
                    // Ensure queue is properly initialized
                    if (s.getQueue() == null) {
                        s.setQueue(new ShelterQueue(s.getCapacity()));
                    } else {
                        // Ensure capacity is set correctly
                        s.getQueue().setCapacity(s.getCapacity());
                    }
                    shelters.put(s.getShelterId(), s);
                }
            }
            System.out.println("Loaded " + shelters.size() + " shelters from Firebase");
        } catch (Exception e) {
            System.err.println("Error loading shelters from Firebase: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Return all shelters
    public List<Shelter> getAllShelters() {
        return List.copyOf(shelters.values());
    }

    // Create a new shelter (with latitude & longitude)
    public String createShelter(String shelterId, String name, int capacity, Double latitude, Double longitude) {
        if (shelters.containsKey(shelterId)) {
            return "Shelter already exists";
        }

        Shelter shelter = new Shelter(shelterId, name, capacity, latitude, longitude);
        shelters.put(shelterId, shelter);

        // Async save to Firebase
        shelterRepository.saveShelter(shelter).exceptionally(throwable -> {
            System.err.println("Failed to save shelter to Firebase: " + throwable.getMessage());
            return null;
        });

        return "Shelter " + shelterId + " created with capacity " + capacity +
                " at location (" + latitude + ", " + longitude + ")";
    }

    // Check-in a user
    public String checkInUser(String shelterId, String rfidTag) {
        Shelter shelter = shelters.get(shelterId);
        if (shelter == null) {
            return "Shelter not found";
        }

        if (shelter.getQueue().isFull()) {
            return "Shelter is full";
        }

        boolean success = shelter.getQueue().enqueue(rfidTag);
        if (!success) {
            return "Failed to check in user";
        }

        // Async save to Firebase
        shelterRepository.saveShelter(shelter).exceptionally(throwable -> {
            System.err.println("Failed to save shelter to Firebase: " + throwable.getMessage());
            return null;
        });

        return "User " + rfidTag + " checked into shelter " + shelterId;
    }

    // Check-out a user
    public String checkOutUser(String shelterId) {
        Shelter shelter = shelters.get(shelterId);
        if (shelter == null) {
            return "Shelter not found";
        }

        String removed = shelter.getQueue().dequeue();
        if (removed == null) {
            return "No users in queue";
        }

        // Async save to Firebase
        shelterRepository.saveShelter(shelter).exceptionally(throwable -> {
            System.err.println("Failed to save shelter to Firebase: " + throwable.getMessage());
            return null;
        });

        return "User " + removed + " rescued from shelter " + shelterId;
    }

    // Get remaining capacity
    public int getRemainingCapacity(String shelterId) {
        Shelter shelter = shelters.get(shelterId);
        if (shelter == null) {
            return 0;
        }
        return shelter.getQueue().getRemainingCapacity();
    }

    // Get current population
    public int getCurrentPopulation(String shelterId) {
        Shelter shelter = shelters.get(shelterId);
        if (shelter == null) {
            return 0;
        }
        return shelter.getQueue().size();
    }
}
