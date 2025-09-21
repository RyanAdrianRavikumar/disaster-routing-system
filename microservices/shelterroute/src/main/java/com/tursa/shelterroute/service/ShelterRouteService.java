package com.tursa.shelterroute.service;

import com.google.firebase.database.*;
import com.tursa.shelterroute.entity.Edge;
import com.tursa.shelterroute.entity.Node;
import com.tursa.shelterroute.entity.Shelter;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

@Service
public class ShelterRouteService {
    private final FirebaseDatabase database;
    private final ConcurrentHashMap<String, Node> nodes = new ConcurrentHashMap<>(); // Added in-memory node storage
    private final ConcurrentHashMap<String, Edge> edges = new ConcurrentHashMap<>(); // Added in-memory edge storage
    private final ConcurrentHashMap<String, Shelter> shelters = new ConcurrentHashMap<>(); // Added in-memory shelter storage

    public ShelterRouteService(FirebaseDatabase database) { // Added constructor with Firebase dependency
        this.database = database;
    }

    @PostConstruct
    public void init() { // Added initialization method
        loadNodes(); // Load nodes from Firebase
        loadEdges(); // Load edges from Firebase
        loadShelters(); // Load shelters from Firebase
    }

    private void loadNodes() { // Added method to load nodes from Firebase
        DatabaseReference ref = database.getReference("nodes");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot child : snapshot.getChildren()) {
                    Node node = child.getValue(Node.class);
                    if (node != null && node.getId() != null) {
                        nodes.put(node.getId(), node);
                    }
                }
                System.out.println("Loaded " + nodes.size() + " nodes from Firebase");
            }

            @Override
            public void onCancelled(DatabaseError error) {
                System.err.println("Failed to load nodes: " + error.getMessage());
            }
        });
    }

    private void loadEdges() { // Added method to load edges from Firebase
        DatabaseReference ref = database.getReference("edges");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot child : snapshot.getChildren()) {
                    Edge edge = child.getValue(Edge.class);
                    if (edge != null && edge.getFrom() != null && edge.getTo() != null) {
                        edges.put(child.getKey(), edge);
                    }
                }
                System.out.println("Loaded " + edges.size() + " edges from Firebase");
            }

            @Override
            public void onCancelled(DatabaseError error) {
                System.err.println("Failed to load edges: " + error.getMessage());
            }
        });
    }

    private void loadShelters() { // Added method to load shelters from Firebase
        DatabaseReference ref = database.getReference("shelters");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot child : snapshot.getChildren()) {
                    Shelter shelter = child.getValue(Shelter.class);
                    if (shelter != null && shelter.getShelterId() != null) {
                        shelters.put(shelter.getShelterId(), shelter);
                    }
                }
                System.out.println("Loaded " + shelters.size() + " shelters from Firebase");
            }

            @Override
            public void onCancelled(DatabaseError error) {
                System.err.println("Failed to load shelters: " + error.getMessage());
            }
        });
    }

    public void addNode(Node node) { // Added method to add a node
        nodes.put(node.getId(), node);
        database.getReference("nodes").child(node.getId()).setValueAsync(node);
    }

    public void addEdge(Edge edge) { // Added method to add an edge
        String edgeId = edge.getFrom() + "-" + edge.getTo();
        edges.put(edgeId, edge);
        database.getReference("edges").child(edgeId).setValueAsync(edge);
    }

    public String createShelter(String shelterId, String name, int capacity, Double latitude, Double longitude) { // Added method to create a shelter
        if (shelters.containsKey(shelterId)) {
            return "Shelter already exists";
        }
        Shelter shelter = new Shelter(shelterId, name, capacity, latitude, longitude);
        shelters.put(shelterId, shelter);
        database.getReference("shelters").child(shelterId).setValueAsync(shelter);
        addNode(new Node(shelterId, name, latitude, longitude)); // Add corresponding node
        return "Shelter " + shelterId + " created with capacity " + capacity;
    }

    public String checkInUser(String shelterId, String rfidTag) { // Added method to check in a user
        Shelter shelter = shelters.get(shelterId);
        if (shelter == null) {
            return "Shelter not found";
        }
        if (shelter.isFull()) {
            return "Shelter is full";
        }
        boolean success = shelter.enqueue(rfidTag);
        if (!success) {
            return "Failed to check in user";
        }
        database.getReference("shelters").child(shelterId).setValueAsync(shelter);
        return "User " + rfidTag + " checked into shelter " + shelterId;
    }

    public String checkOutUser(String shelterId) { // Added method to check out a user
        Shelter shelter = shelters.get(shelterId);
        if (shelter == null) {
            return "Shelter not found";
        }
        String removed = shelter.dequeue();
        if (removed == null) {
            return "No users in queue";
        }
        database.getReference("shelters").child(shelterId).setValueAsync(shelter);
        return "User " + removed + " checked out from shelter " + shelterId;
    }

    public List<Node> getNodes() { // Added method to get all nodes
        return new ArrayList<>(nodes.values());
    }

    public List<Edge> getEdges() { // Added method to get all edges
        return new ArrayList<>(edges.values());
    }

    public List<Shelter> getShelters() { // Added method to get all shelters
        return new ArrayList<>(shelters.values());
    }

    public List<Shelter> getAvailableShelters() { // Added method to get shelters with capacity
        return shelters.values().stream()
                .filter(shelter -> shelter.getRemainingCapacity() > 0)
                .collect(Collectors.toList());
    }

    public String findNearestNode(double lat, double lng) { // Added method to find nearest node
        String nearest = null;
        double minDist = Double.MAX_VALUE;
        for (Node node : nodes.values()) {
            if (node.getLatitude() != null && node.getLongitude() != null) {
                double dist = haversineDistance(lat, lng, node.getLatitude(), node.getLongitude());
                if (dist < minDist) {
                    minDist = dist;
                    nearest = node.getId();
                }
            }
        }
        return nearest;
    }

    public String findNearestAvailableShelter(double userLat, double userLng) { // Added method to find nearest shelter with capacity
        String nearest = null;
        double minDist = Double.MAX_VALUE;
        for (Shelter shelter : shelters.values()) {
            if (shelter.getLatitude() != null && shelter.getLongitude() != null && shelter.getRemainingCapacity() > 0) {
                double dist = haversineDistance(userLat, userLng, shelter.getLatitude(), shelter.getLongitude());
                if (dist < minDist) {
                    minDist = dist;
                    nearest = shelter.getShelterId();
                }
            }
        }
        return nearest;
    }

    private double haversineDistance(double lat1, double lng1, double lat2, double lng2) { // Added Haversine formula for distance
        double R = 6371; // Earth radius in km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    public static class RouteResponse { // Added class for shortest path response
        private List<String> path;
        private double distance;

        public RouteResponse(List<String> path, double distance) {
            this.path = path;
            this.distance = distance;
        }

        public List<String> getPath() { return path; }
        public void setPath(List<String> path) { this.path = path; }
        public double getDistance() { return distance; }
        public void setDistance(double distance) { this.distance = distance; }
    }

    public RouteResponse shortestPathWithDistance(String start, String end) { // Added Dijkstra’s algorithm for shortest path
        Set<String> nodeIds = nodes.keySet();
        if (!nodeIds.contains(start)) {
            throw new IllegalArgumentException("Start node '" + start + "' does not exist");
        }
        if (!nodeIds.contains(end)) {
            throw new IllegalArgumentException("End node '" + end + "' does not exist");
        }

        Map<String, List<Edge>> graph = new HashMap<>();
        for (Edge e : edges.values()) {
            if (!e.isBlocked()) {
                graph.computeIfAbsent(e.getFrom(), k -> new ArrayList<>()).add(e);
                graph.putIfAbsent(e.getTo(), new ArrayList<>());
            }
        }
        graph.putIfAbsent(start, new ArrayList<>());

        Map<String, Double> dist = new HashMap<>();
        Map<String, String> prev = new HashMap<>();
        PriorityQueue<Map.Entry<String, Double>> pq = new PriorityQueue<>(Comparator.comparingDouble(Map.Entry::getValue));

        for (String node : graph.keySet()) {
            dist.put(node, Double.MAX_VALUE);
        }
        dist.put(start, 0.0);
        pq.add(Map.entry(start, 0.0));

        while (!pq.isEmpty()) {
            var current = pq.poll();
            String u = current.getKey();
            double d = current.getValue();

            if (d > dist.get(u)) {
                continue;
            }

            for (Edge e : graph.get(u)) {
                String v = e.getTo();
                double alt = d + e.getWeight();
                if (alt < dist.getOrDefault(v, Double.MAX_VALUE)) {
                    dist.put(v, alt);
                    prev.put(v, u);
                    pq.add(Map.entry(v, alt));
                }
            }
        }

        List<String> path = new ArrayList<>();
        String u = end;
        if (!prev.containsKey(end) && !end.equals(start)) {
            return new RouteResponse(new ArrayList<>(), -1.0);
        }

        while (u != null) {
            path.add(0, u);
            u = prev.get(u);
        }

        return new RouteResponse(path, dist.get(end));
    }

    public void clearData() { // Added method to clear all data
        nodes.clear();
        edges.clear();
        shelters.clear();
        database.getReference("nodes").removeValueAsync();
        database.getReference("edges").removeValueAsync();
        database.getReference("shelters").removeValueAsync();
    }

    public void initSampleData() { // Added method to initialize sample data
        clearData();
        // Add nodes
        addNode(new Node("A", "Point A", 40.7128, -74.0060));
        addNode(new Node("B", "Point B", 40.7228, -74.0160));
        addNode(new Node("C", "Point C", 40.7328, -74.0260));
        addNode(new Node("D", "Point D", 40.7428, -74.0360));
        addNode(new Node("E", "Point E", 40.7528, -74.0460));
        // Add edges
        addEdge(new Edge("A", "B", 5.0, false));
        addEdge(new Edge("A", "C", 10.0, false));
        addEdge(new Edge("B", "C", 3.0, false));
        addEdge(new Edge("B", "D", 8.0, false));
        addEdge(new Edge("C", "D", 4.0, false));
        addEdge(new Edge("D", "E", 6.0, false));
        // Add shelters
        createShelter("A", "Shelter A", 10, 40.7128, -74.0060);
        createShelter("D", "Shelter D", 15, 40.7428, -74.0360);
    }
}