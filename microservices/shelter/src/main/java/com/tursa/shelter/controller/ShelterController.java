package com.tursa.shelter.controller;

import com.tursa.shelter.model.Shelter;
import com.tursa.shelter.service.ShelterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/shelters")
@CrossOrigin(origins = "*")
public class ShelterController {

    @Autowired
    private ShelterService shelterService;

    @PostMapping
    public ResponseEntity<?> createShelter(@Valid @RequestBody Shelter shelter) {
        try {
            Shelter createdShelter = shelterService.createShelter(shelter);
            return ResponseEntity.ok(createdShelter);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal server error");
        }
    }

    @GetMapping("/active")
    public ResponseEntity<List<Shelter>> getActiveShelters() {
        try {
            List<Shelter> shelters = shelterService.getActiveShelters();
            return ResponseEntity.ok(shelters);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<Shelter>> getAllShelters() {
        try {
            List<Shelter> shelters = shelterService.getAllShelters();
            return ResponseEntity.ok(shelters);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getShelterById(@PathVariable Long id) {
        try {
            Shelter shelter = shelterService.getShelterById(id);
            return ResponseEntity.ok(shelter);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal server error");
        }
    }

    @GetMapping("/nearest")
    public ResponseEntity<?> getNearestShelter(
            @RequestParam Double latitude,
            @RequestParam Double longitude) {
        try {
            if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
                return ResponseEntity.badRequest().body("Invalid coordinates");
            }

            Shelter shelter = shelterService.findNearestShelterWithCapacity(latitude, longitude);
            if (shelter != null) {
                return ResponseEntity.ok(shelter);
            } else {
                return ResponseEntity.ok("No available shelter found");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal server error");
        }
    }

    @PostMapping("/{shelterId}/enter")
    public ResponseEntity<?> enterShelter(
            @PathVariable Long shelterId,
            @RequestParam String userRfid) {
        try {
            boolean success = shelterService.addUserToShelter(shelterId, userRfid);
            if (success) {
                return ResponseEntity.ok("User added to shelter successfully");
            } else {
                return ResponseEntity.badRequest()
                        .body("Cannot add user to shelter - may be at capacity");
            }
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal server error");
        }
    }

    @PostMapping("/{shelterId}/exit")
    public ResponseEntity<?> exitShelter(
            @PathVariable Long shelterId,
            @RequestParam String userRfid) {
        try {
            boolean success = shelterService.removeUserFromShelter(shelterId, userRfid);
            if (success) {
                return ResponseEntity.ok("User removed from shelter successfully");
            } else {
                return ResponseEntity.badRequest()
                        .body("Cannot remove user from shelter");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal server error");
        }
    }

    @PutMapping("/{id}/capacity")
    public ResponseEntity<?> updateCapacity(
            @PathVariable Long id,
            @RequestParam Integer newCapacity) {
        try {
            if (newCapacity <= 0) {
                return ResponseEntity.badRequest()
                        .body("Capacity must be positive");
            }

            Shelter shelter = shelterService.updateShelterCapacity(id, newCapacity);
            return ResponseEntity.ok(shelter);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal server error");
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable Long id,
            @RequestParam Boolean isActive) {
        try {
            Shelter shelter = shelterService.updateShelterStatus(id, isActive);
            return ResponseEntity.ok(shelter);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal server error");
        }
    }

    @GetMapping("/statistics")
    public ResponseEntity<?> getShelterStatistics() {
        try {
            Map<String, Object> stats = shelterService.getShelterStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal server error");
        }
    }
}