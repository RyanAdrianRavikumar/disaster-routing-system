package com.tursa.shelter.service;

import com.tursa.shelter.entity.Shelter;
import com.tursa.shelter.repository.ShelterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class ShelterService {

    @Autowired
    private ShelterRepository shelterRepository;

    public List<Shelter> getActiveShelters() {
        return shelterRepository.findActiveSheltersWithCapacity();
    }

    public Shelter createShelter(Shelter shelter) {
        if (shelter.getName() == null || shelter.getLatitude() == null ||
                shelter.getLongitude() == null || shelter.getMaxCapacity() == null) {
            throw new IllegalArgumentException("Name, coordinates, and capacity are required");
        }

        if (shelter.getMaxCapacity() <= 0) {
            throw new IllegalArgumentException("Capacity must be positive");
        }

        return shelterRepository.save(shelter);
    }

    public Shelter findNearestShelterWithCapacity(Double userLat, Double userLon) {
        return shelterRepository.findNearestAvailableShelter(userLat, userLon)
                .orElse(null);
    }

    public Shelter getShelterById(Long id) {
        return shelterRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Shelter not found with ID: " + id));
    }

    @Transactional
    public boolean addUserToShelter(Long shelterId, String userRfid) {
        Shelter shelter = getShelterById(shelterId);

        if (!shelter.hasCapacity()) {
            return false;
        }

        int updated = shelterRepository.incrementOccupancy(shelterId);
        return updated > 0;
    }

    @Transactional
    public boolean removeUserFromShelter(Long shelterId, String userRfid) {
        int updated = shelterRepository.decrementOccupancy(shelterId);
        return updated > 0;
    }

    public Shelter updateShelterCapacity(Long id, Integer newCapacity) {
        Shelter shelter = getShelterById(id);

        if (newCapacity < shelter.getCurrentOccupancy()) {
            throw new IllegalArgumentException("New capacity cannot be less than current occupancy");
        }

        shelter.setMaxCapacity(newCapacity);
        return shelterRepository.save(shelter);
    }

    public Shelter updateShelterStatus(Long id, Boolean isActive) {
        Shelter shelter = getShelterById(id);
        shelter.setIsActive(isActive);

        return shelterRepository.save(shelter);
    }

    public List<Shelter> getAllShelters() {
        return shelterRepository.findAll();
    }

    public Map<String, Object> getShelterStatistics() {
        List<Shelter> allShelters = getAllShelters();

        int totalShelters = allShelters.size();
        int activeShelters = (int) allShelters.stream().filter(Shelter::getIsActive).count();
        int totalCapacity = allShelters.stream()
                .filter(Shelter::getIsActive)
                .mapToInt(Shelter::getMaxCapacity)
                .sum();
        int totalOccupancy = allShelters.stream()
                .filter(Shelter::getIsActive)
                .mapToInt(Shelter::getCurrentOccupancy)
                .sum();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalShelters", totalShelters);
        stats.put("activeShelters", activeShelters);
        stats.put("totalCapacity", totalCapacity);
        stats.put("totalOccupancy", totalOccupancy);
        stats.put("availableCapacity", totalCapacity - totalOccupancy);
        stats.put("occupancyRate", totalCapacity > 0 ? (double) totalOccupancy / totalCapacity : 0.0);

        return stats;
    }
}