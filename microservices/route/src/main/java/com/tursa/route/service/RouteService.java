package com.tursa.route.service;

import com.tursa.route.entity.Edge;
import com.tursa.route.entity.Node;
import com.tursa.route.entity.Route;
import com.tursa.route.entity.RouteHistory;
import com.tursa.route.repository.EdgeRepository;
import com.tursa.route.repository.NodeRepository;
import com.tursa.route.repository.RouteHistoryRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class RouteService {

    @Autowired
    private NodeRepository nodeRepository;

    @Autowired
    private EdgeRepository edgeRepository;

    @Autowired
    private RouteHistoryRepository routeHistoryRepository;

    @Autowired
    private FirebaseRealtimeService firebaseService;

    private static final Logger logger = LoggerFactory.getLogger(RouteService.class);

    @PostConstruct
    public void initializeRoadNetwork() {
        if (nodeRepository.count() == 0) {
            createSampleNetwork();
        }
    }

    private void createSampleNetwork() {
        try {
            // Create sample nodes for Sri Lankan locations
            List<Node> nodes = Arrays.asList(
                    new Node("N001", 6.9271, 79.8612, "Fort Railway Station", "TRANSPORT_HUB", true),
                    new Node("N002", 6.9319, 79.8478, "Galle Face Green", "SHELTER", true),
                    new Node("N003", 6.9244, 79.8553, "Independence Square", "SHELTER", true),
                    new Node("N004", 6.9350, 79.8500, "Main Hospital", "HOSPITAL", true)
            );
            nodeRepository.saveAll(nodes);

            // Create edges (roads between locations)
            List<Edge> edges = Arrays.asList(
                    new Edge("E001", "N001", "N002", 2.5, "MAIN_ROAD", true, true),
                    new Edge("E002", "N001", "N003", 1.8, "MAIN_ROAD", true, true),
                    new Edge("E003", "N002", "N003", 1.2, "SIDE_ROAD", true, true),
                    new Edge("E004", "N003", "N004", 1.5, "MAIN_ROAD", true, true),
                    new Edge("E005", "N002", "N004", 2.0, "MAIN_ROAD", true, true)
            );
            edgeRepository.saveAll(edges);

            logger.info("Sample network created with {} nodes and {} edges",
                    nodeRepository.count(), edgeRepository.count());

        } catch (Exception e) {
            logger.error("Error creating sample network: {}", e.getMessage());
        }
    }

    // Main method to find safest route
    public Route findSafestRoute(String startNodeId, String endNodeId) {
        try {
            // Check if we have a cached route first
            Optional<RouteHistory> cached = findCachedRoute(startNodeId, endNodeId);
            if (cached.isPresent()) {
                return convertToRoute(cached.get());
            }

            // Use Dijkstra's algorithm to find shortest safe path
            Route route = calculateShortestSafePath(startNodeId, endNodeId);

            // Save route to cache
            saveRouteToCache(startNodeId, endNodeId, route);

            // Update Firebase with real-time route status (like user location updates)
            firebaseService.updateRouteStatus(startNodeId + "_" + endNodeId, route.isSafe());
            firebaseService.updateRouteDistance(startNodeId + "_" + endNodeId, route.getTotalDistance());

            logger.info("Found route from {} to {} - Distance: {}, Safe: {}",
                    startNodeId, endNodeId, route.getTotalDistance(), route.isSafe());

            return route;

        } catch (Exception e) {
            logger.error("Error finding route: {}", e.getMessage());
            throw new RuntimeException("Failed to find route");
        }
    }

    // Dijkstra's Algorithm Implementation
    private Route calculateShortestSafePath(String startId, String endId) {
        // Get all nodes and edges
        List<Node> allNodes = nodeRepository.findAll();
        List<Edge> allEdges = edgeRepository.findAll();

        // Build adjacency list for the graph
        Map<String, List<EdgeInfo>> graph = buildGraph(allNodes, allEdges);

        // Dijkstra's algorithm
        Map<String, Double> distances = new HashMap<>();
        Map<String, String> previous = new HashMap<>();
        PriorityQueue<NodeDistance> queue = new PriorityQueue<>();

        // Initialize distances
        for (Node node : allNodes) {
            if (node.getNodeId().equals(startId)) {
                distances.put(node.getNodeId(), 0.0);
                queue.add(new NodeDistance(node.getNodeId(), 0.0));
            } else {
                distances.put(node.getNodeId(), Double.POSITIVE_INFINITY);
            }
        }

        // Process each node
        while (!queue.isEmpty()) {
            NodeDistance current = queue.poll();
            String currentNodeId = current.nodeId;

            // Skip if we already found a shorter path
            if (current.distance > distances.get(currentNodeId)) {
                continue;
            }

            // Check neighbors
            if (graph.containsKey(currentNodeId)) {
                for (EdgeInfo edge : graph.get(currentNodeId)) {
                    String neighborId = edge.toNodeId;
                    double newDistance = distances.get(currentNodeId) + edge.weight;

                    // Only use safe nodes for routing
                    if (isNodeSafe(neighborId) && newDistance < distances.get(neighborId)) {
                        distances.put(neighborId, newDistance);
                        previous.put(neighborId, currentNodeId);
                        queue.add(new NodeDistance(neighborId, newDistance));
                    }
                }
            }
        }

        // Build the path
        List<String> path = buildPath(previous, startId, endId);
        double totalDistance = distances.get(endId);
        boolean isSafe = checkPathSafety(path);

        List<Edge> routeEdges = new ArrayList<>();
        for (int i = 0; i < path.size() - 1; i++) {
            String from = path.get(i);
            String to = path.get(i + 1);
            edgeRepository.findByFromNodeIdAndToNodeId(from, to).ifPresent(routeEdges::add);
        }

        return new Route(path, routeEdges, totalDistance, isSafe);
    }

    // Helper method to build graph from nodes and edges
    private Map<String, List<EdgeInfo>> buildGraph(List<Node> nodes, List<Edge> edges) {
        Map<String, List<EdgeInfo>> graph = new HashMap<>();

        for (Edge edge : edges) {
            if (edge.getIsSafe()) {
                // Add edge from source to destination
                graph.computeIfAbsent(edge.getFromNodeId(), k -> new ArrayList<>())
                        .add(new EdgeInfo(edge.getToNodeId(), edge.getWeight()));

                // Add reverse edge if bidirectional
                if (edge.getIsBidirectional()) {
                    graph.computeIfAbsent(edge.getToNodeId(), k -> new ArrayList<>())
                            .add(new EdgeInfo(edge.getFromNodeId(), edge.getWeight()));
                }
            }
        }

        return graph;
    }

    // Helper method to build path from start to end
    private List<String> buildPath(Map<String, String> previous, String startId, String endId) {
        List<String> path = new ArrayList<>();
        String current = endId;

        while (current != null) {
            path.add(0, current); // Add to beginning of list
            current = previous.get(current);
        }

        // If path doesn't start with startId, no path exists
        if (path.isEmpty() || !path.get(0).equals(startId)) {
            return new ArrayList<>(); // Return empty path
        }

        return path;
    }

    // Check if a node is safe
    private boolean isNodeSafe(String nodeId) {
        Optional<Node> node = nodeRepository.findByNodeId(nodeId);
        return node.isPresent() && node.get().getIsSafe();
    }

    // Check if entire path is safe
    private boolean checkPathSafety(List<String> path) {
        for (String nodeId : path) {
            if (!isNodeSafe(nodeId)) {
                return false;
            }
        }
        return true;
    }

    // Mark node as unsafe (similar to user status updates)
    public void markNodeUnsafe(String nodeId) {
        try {
            Optional<Node> nodeOpt = nodeRepository.findByNodeId(nodeId);
            if (nodeOpt.isPresent()) {
                Node node = nodeOpt.get();
                node.setIsSafe(false);
                nodeRepository.save(node);

                // Clear any cached routes using this node
                clearCacheForNode(nodeId);

                // Update Firebase with real-time safety status (like user location updates)
                firebaseService.updateNodeSafety(nodeId, false);
                firebaseService.broadcastEmergencyAlert("Node " + node.getNodeName() + " marked as unsafe");

                logger.warn("Node {} marked as unsafe", nodeId);
            }
        } catch (Exception e) {
            logger.error("Error marking node unsafe: {}", e.getMessage());
            throw new RuntimeException("Failed to mark node unsafe");
        }
    }

    // Mark node as safe (similar to user status updates)
    public void markNodeSafe(String nodeId) {
        try {
            Optional<Node> nodeOpt = nodeRepository.findByNodeId(nodeId);
            if (nodeOpt.isPresent()) {
                Node node = nodeOpt.get();
                node.setIsSafe(true);
                nodeRepository.save(node);

                // Update Firebase with real-time safety status
                firebaseService.updateNodeSafety(nodeId, true);

                logger.info("Node {} marked as safe", nodeId);
            }
        } catch (Exception e) {
            logger.error("Error marking node safe: {}", e.getMessage());
            throw new RuntimeException("Failed to mark node safe");
        }
    }

    // Get all nodes (similar to getAllUsers)
    public List<Node> getAllNodes() {
        return nodeRepository.findAll();
    }

    // Get only safe nodes (similar to getUsersNeedingRescue filtering)
    public List<Node> getSafeNodes() {
        return nodeRepository.findByIsSafeTrue();
    }

    // Get nodes in area (similar to getUsersInArea)
    public List<Node> getNodesInArea(double centerLat, double centerLon, double radiusKm) {
        double latRange = radiusKm / 111.0; // Approximate km to degree conversion
        double lonRange = radiusKm / (111.0 * Math.cos(Math.toRadians(centerLat)));

        double minLat = centerLat - latRange;
        double maxLat = centerLat + latRange;
        double minLon = centerLon - lonRange;
        double maxLon = centerLon + lonRange;

        return nodeRepository.findNodesInArea(minLat, maxLat, minLon, maxLon);
    }

    // Create new node (similar to user registration)
    public Node createNode(String nodeId, Double latitude, Double longitude,
                           String nodeName, String nodeType) {
        try {
            if (nodeRepository.findByNodeId(nodeId).isPresent()) {
                throw new IllegalArgumentException("Node already exists");
            }

            Node node = new Node(nodeId, latitude, longitude, nodeName, nodeType, true);
            return nodeRepository.save(node);

        } catch (Exception e) {
            logger.error("Error creating node: {}", e.getMessage());
            throw new RuntimeException("Failed to create node");
        }
    }

    // Create new edge (road connection)
    public Edge createEdge(String fromNodeId, String toNodeId, Double weight,
                           String roadType, Boolean isBidirectional) {
        try {
            // Check if both nodes exist
            if (!nodeRepository.findByNodeId(fromNodeId).isPresent() ||
                    !nodeRepository.findByNodeId(toNodeId).isPresent()) {
                throw new IllegalArgumentException("Both nodes must exist");
            }

            String edgeId = "E_" + fromNodeId + "_" + toNodeId;
            Edge edge = new Edge(edgeId, fromNodeId, toNodeId, weight, roadType,
                    isBidirectional != null ? isBidirectional : true, true);

            return edgeRepository.save(edge);

        } catch (Exception e) {
            logger.error("Error creating edge: {}", e.getMessage());
            throw new RuntimeException("Failed to create edge");
        }
    }

    // Recalculate route when nodes become unsafe
    public Route recalculateRoute(String startNodeId, String endNodeId, List<String> unsafeNodeIds) {
        try {
            // Mark nodes as unsafe
            for (String nodeId : unsafeNodeIds) {
                markNodeUnsafe(nodeId);
            }

            // Clear cache and calculate new route
            clearCacheForNodes(Arrays.asList(startNodeId, endNodeId));
            Route newRoute = findSafestRoute(startNodeId, endNodeId);

            // Broadcast route update to Firebase (like user status updates)
            firebaseService.broadcastRouteUpdate(startNodeId, endNodeId, newRoute.isSafe());

            return newRoute;

        } catch (Exception e) {
            logger.error("Error recalculating route: {}", e.getMessage());
            throw new RuntimeException("Failed to recalculate route");
        }
    }

    // Cache management methods
    private Optional<RouteHistory> findCachedRoute(String startId, String endId) {
        return routeHistoryRepository.findValidRoute(startId, endId, LocalDateTime.now());
    }

    private void saveRouteToCache(String startId, String endId, Route route) {
        try {
            RouteHistory history = new RouteHistory();
            history.setStartNodeId(startId);
            history.setEndNodeId(endId);
            history.setRoutePath(String.join(",", route.getPath()));
            history.setTotalDistance(route.getTotalDistance());
            history.setIsSafe(route.isSafe());
            history.setCreatedAt(LocalDateTime.now());

            routeHistoryRepository.save(history);
        } catch (Exception e) {
            logger.warn("Failed to cache route: {}", e.getMessage());
        }
    }

    private Route convertToRoute(RouteHistory history) {
        List<String> path = Arrays.asList(history.getRoutePath().split(","));

        // Build list of edges for this path
        List<Edge> routeEdges = new ArrayList<>();
        for (int i = 0; i < path.size() - 1; i++) {
            String from = path.get(i);
            String to = path.get(i + 1);

            edgeRepository.findByFromNodeIdAndToNodeId(from, to).ifPresent(routeEdges::add);
        }

        return new Route(path, routeEdges, history.getTotalDistance(), history.getIsSafe());
    }

    private void clearCacheForNode(String nodeId) {
        routeHistoryRepository.deleteExpiredRoutes(LocalDateTime.now());
    }

    private void clearCacheForNodes(List<String> nodeIds) {
        routeHistoryRepository.deleteExpiredRoutes(LocalDateTime.now());
    }

    public int cleanupExpiredRoutes() {
        try {
            return routeHistoryRepository.deleteExpiredRoutes(LocalDateTime.now());
        } catch (Exception e) {
            logger.error("Error cleaning expired routes: {}", e.getMessage());
            return 0;
        }
    }

    // Method to get evacuation recommendations for users (integrates with user service)
    public List<Route> getEvacuationRoutes(String userLocation, List<String> safeZones) {
        try {
            List<Route> evacuationRoutes = new ArrayList<>();

            for (String safeZone : safeZones) {
                try {
                    Route route = findSafestRoute(userLocation, safeZone);
                    evacuationRoutes.add(route);
                } catch (Exception e) {
                    logger.warn("Could not calculate route to safe zone {}: {}", safeZone, e.getMessage());
                }
            }

            // Sort routes by distance (shortest first)
            evacuationRoutes.sort((r1, r2) -> Double.compare(r1.getTotalDistance(), r2.getTotalDistance()));

            // Update Firebase with evacuation options
            if (!evacuationRoutes.isEmpty()) {
                Route bestRoute = evacuationRoutes.get(0);
                firebaseService.sendRouteRecommendation(userLocation,
                        bestRoute.getPath().toString(), "Shortest safe evacuation route");
            }

            return evacuationRoutes;

        } catch (Exception e) {
            logger.error("Error getting evacuation routes: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    // Method to monitor and update real-time network status
    public void updateNetworkStatus() {
        try {
            List<Node> allNodes = getAllNodes();
            List<Node> safeNodes = getSafeNodes();

            int totalNodes = allNodes.size();
            int safeNodeCount = safeNodes.size();
            int affectedNodes = totalNodes - safeNodeCount;

            // Calculate route statistics
            int totalPossibleRoutes = totalNodes * (totalNodes - 1);
            int estimatedSafeRoutes = safeNodeCount * (safeNodeCount - 1);

            // Update Firebase with network statistics (similar to user location tracking)
            firebaseService.updateEvacuationStats(totalPossibleRoutes, estimatedSafeRoutes, affectedNodes);

            logger.info("Network status updated - Safe nodes: {}/{}, Affected: {}",
                    safeNodeCount, totalNodes, affectedNodes);

        } catch (Exception e) {
            logger.error("Error updating network status: {}", e.getMessage());
        }
    }

    // Helper classes for Dijkstra's algorithm
    private static class EdgeInfo {
        String toNodeId;
        double weight;

        EdgeInfo(String toNodeId, double weight) {
            this.toNodeId = toNodeId;
            this.weight = weight;
        }
    }

    private static class NodeDistance implements Comparable<NodeDistance> {
        String nodeId;
        double distance;

        NodeDistance(String nodeId, double distance) {
            this.nodeId = nodeId;
            this.distance = distance;
        }

        @Override
        public int compareTo(NodeDistance other) {
            return Double.compare(this.distance, other.distance);
        }
    }

    public Route findRouteToNearestShelter(double userLat, double userLon) {
        // 1️⃣ Find nearest node to the user
        Node nearestNode = nodeRepository.findNearestNode(userLat, userLon);
        if (nearestNode == null) {
            throw new RuntimeException("No nearby node found for user location");
        }

        logger.info("User at ({}, {}) mapped to nearest node {}", userLat, userLon, nearestNode.getNodeId());

        // 2️⃣ Find nearest shelter
        List<Node> shelters = nodeRepository.findByNodeType("SHELTER");
        if (shelters.isEmpty()) {
            throw new RuntimeException("No shelters available");
        }

        Node nearestShelter = shelters.stream()
                .min(Comparator.comparingDouble(s -> distance(userLat, userLon, s.getLatitude(), s.getLongitude())))
                .get();

        logger.info("Nearest shelter is {} at ({}, {})", nearestShelter.getNodeId(),
                nearestShelter.getLatitude(), nearestShelter.getLongitude());

        // 3️⃣ Calculate safest route from nearest node to nearest shelter
        return findSafestRoute(nearestNode.getNodeId(), nearestShelter.getNodeId());
    }

    // Haversine distance between two lat/lon points
    private double distance(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371; // km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return R * c;
    }

}