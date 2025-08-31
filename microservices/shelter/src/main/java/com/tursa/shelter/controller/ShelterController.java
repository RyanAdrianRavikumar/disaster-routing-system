package com.tursa.shelter.controller;

import com.tursa.shelter.service.ShelterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/shelters")
public class ShelterController {

    @Autowired
    private ShelterService shelterService;

    @PostMapping("/{shelterId}/checkin/{rfidTag}")
    public ResponseEntity<String> checkInUser(@PathVariable String shelterId, @PathVariable String rfidTag) {
        try {
            return ResponseEntity.ok(shelterService.checkInUser(shelterId, rfidTag));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/{shelterId}/checkout")
    public ResponseEntity<String> checkOutUser(@PathVariable String shelterId) {
        try {
            return ResponseEntity.ok(shelterService.checkOutUser(shelterId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/{shelterId}/remainingCapacity")
    public ResponseEntity<Integer> getRemainingCapacity(@PathVariable String shelterId) {
        try {
            return ResponseEntity.ok(shelterService.getRemainingCapacity(shelterId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(0);
        }
    }

    @GetMapping("/{shelterId}/population")
    public ResponseEntity<Integer> getPopulation(@PathVariable String shelterId) {
        try {
            return ResponseEntity.ok(shelterService.getCurrentPopulation(shelterId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(0);
        }
    }
}
