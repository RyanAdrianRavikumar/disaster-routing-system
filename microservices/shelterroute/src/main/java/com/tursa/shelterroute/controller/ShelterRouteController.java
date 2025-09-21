package com.tursa.shelterroute.controller;

import com.tursa.shelterroute.entity.Edge;
import com.tursa.shelterroute.entity.Node;
import com.tursa.shelterroute.entity.Shelter;
import com.tursa.shelterroute.service.ShelterRouteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ShelterRouteController {
    @Autowired
    private ShelterRouteService service; // Added dependency for service

    @GetMapping("/nodes")
    public ResponseEntity<List<Node>> getNodes() { // Added endpoint to get all nodes
        return ResponseEntity.ok(service.getNodes());
    }

    @GetMapping("/edges")
    public ResponseEntity<List<Edge>> getEdges() { // Added endpoint to get all edges
        return ResponseEntity.ok(service.getEdges());
    }

    @GetMapping("/shelters")
    public ResponseEntity<List<Shelter>> getShelters() { // Added endpoint to get all shelters
        return ResponseEntity.ok(service.getShelters());
    }

    @PostMapping("/shelters/{shelterId}")
    public ResponseEntity<String> createShelter(
            @PathVariable String shelterId,
            @RequestParam String name,
            @RequestParam int capacity,
            @RequestParam Double latitude,
            @RequestParam Double longitude) { // Added endpoint to create a shelter
        return ResponseEntity.ok(service.createShelter(shelterId, name, capacity, latitude, longitude));
    }

    @PutMapping("/shelters/{shelterId}")
    public ResponseEntity<String> updateShelter(
            @PathVariable String shelterId,
            @RequestParam String name,
            @RequestParam int capacity,
            @RequestParam double latitude,
            @RequestParam double longitude) {
        return ResponseEntity.ok(service.updateShelter(shelterId, name, capacity, latitude, longitude));
    }

    @DeleteMapping("/shelters/{shelterId}")
    public ResponseEntity<String> deleteShelter(@PathVariable String shelterId) {
        return ResponseEntity.ok(service.deleteShelter(shelterId));
    }

    @PostMapping("/shelters/{shelterId}/checkin/{rfidTag}")
    public ResponseEntity<String> checkInUser(
            @PathVariable String shelterId,
            @PathVariable String rfidTag) { // Added endpoint to check in a user
        return ResponseEntity.ok(service.checkInUser(shelterId, rfidTag));
    }

    @PostMapping("/shelters/{shelterId}/checkout")
    public ResponseEntity<String> checkOutUser(
            @PathVariable String shelterId) { // Added endpoint to check out a user
        return ResponseEntity.ok(service.checkOutUser(shelterId));
    }

    @GetMapping("/nearest-shelter-path")
    public ResponseEntity<Map<String, Object>> getNearestShelterPath(
            @RequestParam double userLat,
            @RequestParam double userLng) { // Added endpoint for nearest shelter path
        String startNode = service.findNearestNode(userLat, userLng);
        if (startNode == null) {
            return ResponseEntity.ok(Map.of("error", "No nodes available"));
        }
        String endNode = service.findNearestAvailableShelter(userLat, userLng);
        if (endNode == null) {
            return ResponseEntity.ok(Map.of("error", "No available shelters"));
        }
        Shelter shelter = service.getShelters().stream()
                .filter(s -> s.getShelterId().equals(endNode))
                .findFirst()
                .orElse(null);
        String shelterName = shelter != null ? shelter.getName() : "Unknown";
        ShelterRouteService.RouteResponse response = service.shortestPathWithDistance(startNode, endNode);
        if (response.getPath().isEmpty()) {
            return ResponseEntity.ok(Map.of("error", "No path found"));
        }
        return ResponseEntity.ok(Map.of(
                "path", response.getPath(),
                "distance", response.getDistance(),
                "shelterId", endNode,
                "shelterName", shelterName
        ));
    }

    @PostMapping("/init-data")
    public ResponseEntity<String> initSampleData() { // Added endpoint to initialize sample data
        service.initSampleData();
        return ResponseEntity.ok("Sample data initialized");
    }

    @PostMapping("/clear-data")
    public ResponseEntity<String> clearData() { // Added endpoint to clear all data
        service.clearData();
        return ResponseEntity.ok("Data cleared");
    }
}