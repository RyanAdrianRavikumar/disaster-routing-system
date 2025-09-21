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
    private ShelterRouteService service;

    @GetMapping("/nodes")
    public ResponseEntity<List<Node>> getNodes() {
        return ResponseEntity.ok(service.getNodes());
    }

    @GetMapping("/edges")
    public ResponseEntity<List<Edge>> getEdges() {
        return ResponseEntity.ok(service.getEdges());
    }

    @GetMapping("/shelters")
    public ResponseEntity<List<Shelter>> getShelters() {
        return ResponseEntity.ok(service.getShelters());
    }

    @PostMapping("/shelters/{shelterId}")
    public ResponseEntity<String> createShelter(
            @PathVariable String shelterId,
            @RequestParam String name,
            @RequestParam int capacity,
            @RequestParam double latitude,
            @RequestParam double longitude) {
        return ResponseEntity.ok(service.createShelter(shelterId, name, capacity, latitude, longitude));
    }

    @PostMapping("/shelters/{shelterId}/checkin/{rfidTag}")
    public ResponseEntity<String> checkInUser(
            @PathVariable String shelterId,
            @PathVariable String rfidTag) {
        return ResponseEntity.ok(service.checkInUser(shelterId, rfidTag));
    }

    @PostMapping("/shelters/{shelterId}/checkout")
    public ResponseEntity<String> checkOutUser(
            @PathVariable String shelterId) {
        return ResponseEntity.ok(service.checkOutUser(shelterId));
    }

    @GetMapping("/nearest-shelter-path")
    public ResponseEntity<Map<String, Object>> getNearestShelterPath(
            @RequestParam double userLat,
            @RequestParam double userLng) {
        try {
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
                return ResponseEntity.ok(Map.of("error", "No path found between nodes " + startNode + " and " + endNode));
            }
            return ResponseEntity.ok(Map.of(
                    "path", response.getPath(),
                    "distance", response.getDistance(),
                    "shelterId", endNode,
                    "shelterName", shelterName
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            System.err.println("Unexpected error in pathfinding: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(Map.of("error", "Internal server error: " + e.getMessage()));
        }
    }

    @PostMapping("/init-data")
    public ResponseEntity<String> initSampleData() {
        service.initSampleData();
        return ResponseEntity.ok("Sample data initialized");
    }

    @PostMapping("/clear-data")
    public ResponseEntity<String> clearData() {
        service.clearData();
        return ResponseEntity.ok("Data cleared");
    }
}