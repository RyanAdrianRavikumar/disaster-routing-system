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

@Service
public class ShelterRouteService {
    private final FirebaseDatabase database;
    private final ConcurrentHashMap<String, Node> nodes = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Edge> edges = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Shelter> shelters = new ConcurrentHashMap<>();

    public ShelterRouteService(FirebaseDatabase database) {
        this.database = database;
    }

    @PostConstruct
    public void init() {
        loadNodes();
        loadEdges();
        loadShelters();
    }

    private void loadNodes() {
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

    private void loadEdges() {
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

    private void loadShelters() {
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

    public void addNode(Node node) {
        nodes.put(node.getId(), node);
        database.getReference("nodes").child(node.getId()).setValueAsync(node);
    }

    public void addEdge(Edge edge) {
        String edgeId = edge.getFrom() + "-" + edge.getTo();
        edges.put(edgeId, edge);
        database.getReference("edges").child(edgeId).setValueAsync(edge);
    }

    public String createShelter(String shelterId, String name, int capacity, double latitude, double longitude) {
        if (shelters.containsKey(shelterId)) {
            return "Shelter already exists";
        }
        Shelter shelter = new Shelter(shelterId, name, capacity, latitude, longitude);
        shelters.put(shelterId, shelter);
        database.getReference("shelters").child(shelterId).setValueAsync(shelter);
        addNode(new Node(shelterId, name, latitude, longitude)); // Sync as node
        return "Shelter " + shelterId + " created with capacity " + capacity;
    }

    public String updateShelter(String shelterId, String name, int capacity, double latitude, double longitude) {
        Shelter shelter = shelters.get(shelterId);
        if (shelter == null) {
            return "Shelter not found";
        }
        shelter.setName(name);
        shelter.setCapacity(capacity);
        shelter.setLatitude(latitude);
        shelter.setLongitude(longitude);
        shelters.put(shelterId, shelter);
        database.getReference("shelters").child(shelterId).setValueAsync(shelter);
        addNode(new Node(shelterId, name, latitude, longitude)); // Update node
        return "Shelter " + shelterId + " updated";
    }

    public String deleteShelter(String shelterId) {
        Shelter shelter = shelters.get(shelterId);
        if (shelter == null) {
            return "Shelter not found";
        }
        shelters.remove(shelterId);
        nodes.remove(shelterId); // Remove corresponding node
        database.getReference("shelters").child(shelterId).removeValueAsync();
        database.getReference("nodes").child(shelterId).removeValueAsync();
        return "Shelter " + shelterId + " deleted";
    }

    public String checkInUser(String shelterId, String rfidTag) {
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

    public String checkOutUser(String shelterId) {
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

    public List<Node> getNodes() {
        return new ArrayList<>(nodes.values());
    }

    public List<Edge> getEdges() {
        return new ArrayList<>(edges.values());
    }

    public List<Shelter> getShelters() {
        return new ArrayList<>(shelters.values());
    }

    public List<Shelter> getAvailableShelters() {
        List<Shelter> available = new ArrayList<>();
        for (Shelter shelter : shelters.values()) {
            if (shelter.getRemainingCapacity() > 0) {
                available.add(shelter);
            }
        }
        return available;
    }

    public String findNearestNode(double lat, double lng) {
        String nearest = null;
        double minDist = Double.MAX_VALUE;
        for (Node node : nodes.values()) {
            if (node.getLatitude() != 0 && node.getLongitude() != 0) { // Check for valid lat/lng
                double dist = haversineDistance(lat, lng, node.getLatitude(), node.getLongitude());
                if (dist < minDist) {
                    minDist = dist;
                    nearest = node.getId();
                }
            }
        }
        return nearest;
    }

    public String findNearestAvailableShelter(double userLat, double userLng) {
        String nearest = null;
        double minDist = Double.MAX_VALUE;
        for (Shelter shelter : shelters.values()) {
            if (shelter.getLatitude() != 0 && shelter.getLongitude() != 0 && shelter.getRemainingCapacity() > 0) {
                double dist = haversineDistance(userLat, userLng, shelter.getLatitude(), shelter.getLongitude());
                if (dist < minDist) {
                    minDist = dist;
                    nearest = shelter.getShelterId();
                }
            }
        }
        return nearest;
    }

    private double haversineDistance(double lat1, double lng1, double lat2, double lng2) {
        double R = 6371; // Earth radius in km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    public static class RouteResponse {
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

    public RouteResponse shortestPathWithDistance(String start, String end) {
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

    public void clearData() {
        nodes.clear();
        edges.clear();
        shelters.clear();
        database.getReference("nodes").removeValueAsync();
        database.getReference("edges").removeValueAsync();
        database.getReference("shelters").removeValueAsync();
    }

    public void initSampleData() {
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