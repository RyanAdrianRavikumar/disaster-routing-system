package com.tursa.shelter.controller;

import com.tursa.shelter.entity.Shelter;
import com.tursa.shelter.service.ShelterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/shelters")
public class ShelterController {

    @Autowired
    private ShelterService shelterService;

    @GetMapping("/all")
    public ResponseEntity<List<Shelter>> getAllShelters() {
        List<Shelter> shelters = shelterService.getAllShelters();
        return ResponseEntity.ok(shelters);
    }

    @PostMapping("/create/{shelterId}")
    public ResponseEntity<String> createShelter(@PathVariable String shelterId,
                                                @RequestParam String name,
                                                @RequestParam int capacity) {
        return ResponseEntity.ok(shelterService.createShelter(shelterId, name, capacity));
    }

    @PostMapping("/{shelterId}/checkin/{rfidTag}")
    public ResponseEntity<String> checkInUser(@PathVariable String shelterId,
                                              @PathVariable String rfidTag) {
        return ResponseEntity.ok(shelterService.checkInUser(shelterId, rfidTag));
    }

    @PostMapping("/{shelterId}/checkout")
    public ResponseEntity<String> checkOutUser(@PathVariable String shelterId) {
        return ResponseEntity.ok(shelterService.checkOutUser(shelterId));
    }

    @GetMapping("/{shelterId}/remainingCapacity")
    public ResponseEntity<Integer> getRemainingCapacity(@PathVariable String shelterId) {
        return ResponseEntity.ok(shelterService.getRemainingCapacity(shelterId));
    }

    @GetMapping("/{shelterId}/population")
    public ResponseEntity<Integer> getPopulation(@PathVariable String shelterId) {
        return ResponseEntity.ok(shelterService.getCurrentPopulation(shelterId));
    }
}