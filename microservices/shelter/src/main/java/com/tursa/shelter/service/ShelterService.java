package com.tursa.shelter.service;

import com.tursa.shelter.entity.Shelter;
import com.tursa.shelter.repository.ShelterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ShelterService {

    @Autowired
    private ShelterRepository shelterRepository;

    public String checkInUser(String shelterId, String rfidTag) throws Exception {
        Shelter shelter = shelterRepository.getShelter(shelterId).get();
        if (shelter.getQueue().isFull()) {
            return "Shelter is full!";
        }
        shelter.getQueue().enqueue(rfidTag);
        shelterRepository.saveShelter(shelter);
        return "User " + rfidTag + " checked into shelter " + shelterId;
    }

    public String checkOutUser(String shelterId) throws Exception {
        Shelter shelter = shelterRepository.getShelter(shelterId).get();
        String removed = shelter.getQueue().dequeue();
        shelterRepository.saveShelter(shelter);
        return removed == null ? "No users in queue" : "User " + removed + " rescued from shelter " + shelterId;
    }

    public int getRemainingCapacity(String shelterId) throws Exception {
        Shelter shelter = shelterRepository.getShelter(shelterId).get();
        return shelter.getQueue().getRemainingCapacity();
    }

    public int getCurrentPopulation(String shelterId) throws Exception {
        Shelter shelter = shelterRepository.getShelter(shelterId).get();
        return shelter.getQueue().size();
    }
}
