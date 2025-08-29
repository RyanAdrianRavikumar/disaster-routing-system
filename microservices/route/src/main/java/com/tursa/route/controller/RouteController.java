package com.tursa.route.controller;

import com.tursa.route.entity.Route;
import com.tursa.route.entity.Node;
import com.tursa.route.entity.Edge;
import com.tursa.route.service.RouteService;
import com.tursa.route.dto.NodeCreationRequest;
import com.tursa.route.dto.EdgeCreationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/routes")
public class RouteController {

    @Autowired
    private RouteService routeService;

    private static final Logger logger = LoggerFactory.getLogger(RouteController.class);

    // Calculate safest route between two points
    @GetMapping("/calculate")
    public ResponseEntity<Route> calculateRoute(@RequestParam String start, @RequestParam String end) {
        try {
            if (start == null || end == null) {
                return ResponseEntity.badRequest().build();
            }

            Route route = routeService.findSafestRoute(start, end);
            return ResponseEntity.ok(route);

        } catch (Exception e) {
            logger.error("Error calculating route: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Recalculate route avoiding unsafe areas
    @PostMapping("/recalculate")
    public ResponseEntity<Route> recalculateRoute(
            @RequestParam String start,
            @RequestParam String end,
            @RequestBody(required = false) List<String> unsafeNodes) {
        try {
            if (unsafeNodes == null) {
                unsafeNodes = new ArrayList<>();
            }

            Route route = routeService.recalculateRoute(start, end, unsafeNodes);
            return ResponseEntity.ok(route);

        } catch (Exception e) {
            logger.error("Error recalculating route: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Mark a location as unsafe (similar to updating user status)
    @PutMapping("/node/{nodeId}/unsafe")
    public ResponseEntity<Void> markNodeUnsafe(@PathVariable String nodeId) {
        try {
            routeService.markNodeUnsafe(nodeId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error marking node unsafe: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Mark a location as safe (similar to updating user status)
    @PutMapping("/node/{nodeId}/safe")
    public ResponseEntity<Void> markNodeSafe(@PathVariable String nodeId) {
        try {
            routeService.markNodeSafe(nodeId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error marking node safe: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Get all locations (similar to getAllUsers)
    @GetMapping("/nodes/all")
    public ResponseEntity<List<Node>> getAllNodes() {
        try {
            List<Node> nodes = routeService.getAllNodes();
            return ResponseEntity.ok(nodes);
        } catch (Exception e) {
            logger.error("Error retrieving nodes: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Get only safe locations (similar to getUsersNeedingRescue)
    @GetMapping("/nodes/safe")
    public ResponseEntity<List<Node>> getSafeNodes() {
        try {
            List<Node> nodes = routeService.getSafeNodes();
            return ResponseEntity.ok(nodes);
        } catch (Exception e) {
            logger.error("Error retrieving safe nodes: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Get locations in specific area (similar to getUsersInArea)
    @GetMapping("/nodes/area")
    public ResponseEntity<List<Node>> getNodesInArea(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "5.0") Double radiusKm) {
        try {
            List<Node> nodes = routeService.getNodesInArea(latitude, longitude, radiusKm);
            return ResponseEntity.ok(nodes);
        } catch (Exception e) {
            logger.error("Error retrieving nodes in area: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Create new location (similar to user registration)
    @PostMapping("/nodes")
    public ResponseEntity<Node> createNode(@RequestBody NodeCreationRequest request) {
        try {
            if (request.getNodeId() == null || request.getLatitude() == null ||
                    request.getLongitude() == null) {
                return ResponseEntity.badRequest().build();
            }

            Node node = routeService.createNode(
                    request.getNodeId(),
                    request.getLatitude(),
                    request.getLongitude(),
                    request.getNodeName(),
                    request.getNodeType()
            );
            return ResponseEntity.ok(node);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error creating node: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Create new road connection
    @PostMapping("/edges")
    public ResponseEntity<Edge> createEdge(@RequestBody EdgeCreationRequest request) {
        try {
            if (request.getFromNodeId() == null || request.getToNodeId() == null ||
                    request.getWeight() == null) {
                return ResponseEntity.badRequest().build();
            }

            Edge edge = routeService.createEdge(
                    request.getFromNodeId(),
                    request.getToNodeId(),
                    request.getWeight(),
                    request.getRoadType(),
                    request.getIsBidirectional()
            );
            return ResponseEntity.ok(edge);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error creating edge: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Clean up old cached routes
    @DeleteMapping("/cache/cleanup")
    public ResponseEntity<Integer> cleanupExpiredRoutes() {
        try {
            int deletedCount = routeService.cleanupExpiredRoutes();
            return ResponseEntity.ok(deletedCount);
        } catch (Exception e) {
            logger.error("Error cleaning expired routes: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Get evacuation routes for a user (integrates with user microservice)
    @GetMapping("/evacuation")
    public ResponseEntity<List<Route>> getEvacuationRoutes(
            @RequestParam String userLocation,
            @RequestBody List<String> safeZones) {
        try {
            List<Route> routes = routeService.getEvacuationRoutes(userLocation, safeZones);
            return ResponseEntity.ok(routes);
        } catch (Exception e) {
            logger.error("Error getting evacuation routes: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Update real-time network status (similar to user status monitoring)
    @PostMapping("/network/status")
    public ResponseEntity<Void> updateNetworkStatus() {
        try {
            routeService.updateNetworkStatus();
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error updating network status: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}