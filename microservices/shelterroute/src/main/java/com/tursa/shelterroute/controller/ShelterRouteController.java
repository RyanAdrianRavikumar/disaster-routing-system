package com.tursa.shelterroute.controller;

import com.tursa.shelterroute.entity.Node;
import com.tursa.shelterroute.service.ShelterRouteService;
import com.tursa.shelterroute.service.ShelterRouteService.RouteResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
public class ShelterRouteController {

    @Autowired
    private ShelterRouteService shelterRouteService;

    @GetMapping("/nodes")
    public List<Node> getNodes() {
        return shelterRouteService.getNodes();
    }

    @GetMapping("/shelters")
    public List<Node> getShelters() {
        return shelterRouteService.getNodes().stream()
                .filter(node -> node.getCapacity() > 0)
                .toList();
    }

    @PostMapping("/shelters")
    public String createShelter(
            @RequestParam String shelterId,
            @RequestParam String name,
            @RequestParam int capacity,
            @RequestParam double latitude,
            @RequestParam double longitude) {
        return shelterRouteService.createOrUpdateShelter(shelterId, name, capacity, latitude, longitude);
    }

    @DeleteMapping("/shelters/{shelterId}")
    public String deleteShelter(@PathVariable String shelterId) {
        return shelterRouteService.deleteShelter(shelterId);
    }

    @PostMapping("/check-in")
    public String checkInUser(@RequestParam String shelterId, @RequestParam String rfidTag) {
        return shelterRouteService.checkInUser(shelterId, rfidTag);
    }

    @PostMapping("/check-out")
    public String checkOutUser(@RequestParam String shelterId) {
        return shelterRouteService.checkOutUser(shelterId);
    }

    @GetMapping("/find-nearest")
    public String findNearestNode(@RequestParam double lat, @RequestParam double lng) {
        return shelterRouteService.findNearestNode(lat, lng);
    }

    @GetMapping("/find-nearest-shelter")
    public String findNearestShelter(@RequestParam double lat, @RequestParam double lng) {
        return shelterRouteService.findNearestShelter(lat, lng);
    }

    @GetMapping("/shortest-path")
    public RouteResponse getShortestPath(@RequestParam String start, @RequestParam String end) {
        return shelterRouteService.shortestPathWithDistance(start, end);
    }

    @PostMapping("/shortest-path-from-user")
    public RouteResponse getShortestPathFromUser(
            @RequestBody UserPathRequest request) {
        return shelterRouteService.shortestPathFromUser(
                request.getUserLat(),
                request.getUserLng(),
                request.getNearestNodeId(),
                request.getEnd());
    }

    @PostMapping("/init-data")
    public void initSampleData() {
        shelterRouteService.initSampleData();
    }

    @DeleteMapping("/clear-data")
    public void clearData() {
        shelterRouteService.clearData();
    }

    public static class UserPathRequest {
        private double userLat;
        private double userLng;
        private String nearestNodeId;
        private String end;

        public double getUserLat() {
            return userLat;
        }

        public void setUserLat(double userLat) {
            this.userLat = userLat;
        }

        public double getUserLng() {
            return userLng;
        }

        public void setUserLng(double userLng) {
            this.userLng = userLng;
        }

        public String getNearestNodeId() {
            return nearestNodeId;
        }

        public void setNearestNodeId(String nearestNodeId) {
            this.nearestNodeId = nearestNodeId;
        }

        public String getEnd() {
            return end;
        }

        public void setEnd(String end) {
            this.end = end;
        }
    }
}