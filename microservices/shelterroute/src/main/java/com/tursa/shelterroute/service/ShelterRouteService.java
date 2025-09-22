package com.tursa.shelterroute.service;

import com.google.firebase.database.*;
import com.tursa.shelterroute.entity.Edge;
import com.tursa.shelterroute.entity.Node;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class ShelterRouteService {

    @Autowired
    private FirebaseDatabase database;

    public List<Node> getNodes() {
        DatabaseReference ref = database.getReference("nodes");
        List<Node> nodes = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot child : snapshot.getChildren()) {
                    Node node = child.getValue(Node.class);
                    if (node != null) {
                        nodes.add(node);
                    }
                }
                System.out.println("Fetched nodes: " + nodes.size());
                latch.countDown();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                throw new RuntimeException("Failed to fetch nodes: " + error.getMessage());
            }
        });
        try {
            latch.await();
            return nodes;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while fetching nodes", e);
        }
    }

    public List<Edge> getEdges() {
        DatabaseReference ref = database.getReference("edges");
        List<Edge> edges = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot child : snapshot.getChildren()) {
                    Edge edge = child.getValue(Edge.class);
                    if (edge != null) {
                        edges.add(edge);
                    }
                }
                System.out.println("Fetched edges: " + edges.size());
                latch.countDown();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                throw new RuntimeException("Failed to fetch edges: " + error.getMessage());
            }
        });
        try {
            latch.await();
            return edges;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while fetching edges", e);
        }
    }

    public String createOrUpdateShelter(String shelterId, String name, int capacity, double latitude, double longitude) {
        try {
            DatabaseReference ref = database.getReference("nodes/" + shelterId);
            Node node = new Node(shelterId, name, latitude, longitude, capacity);
            System.out.println("Saving node: " + node);
            ref.setValue(node, (databaseError, databaseReference) -> {
                if (databaseError != null) {
                    throw new RuntimeException("Failed to create/update shelter: " + databaseError.getMessage());
                }
            });
            return "Shelter created or updated successfully";
        } catch (DatabaseException e) {
            throw new RuntimeException("Error creating/updating shelter: " + e.getMessage(), e);
        }
    }

    public String deleteShelter(String shelterId) {
        try {
            DatabaseReference ref = database.getReference("nodes/" + shelterId);
            ref.removeValue((databaseError, databaseReference) -> {
                if (databaseError != null) {
                    throw new RuntimeException("Failed to delete shelter: " + databaseError.getMessage());
                }
            });
            return "Shelter deleted successfully";
        } catch (DatabaseException e) {
            throw new RuntimeException("Error deleting shelter: " + e.getMessage(), e);
        }
    }

    public String checkInUser(String shelterId, String rfidTag) {
        Node node = getNodeByShelterId(shelterId);
        if (node != null && node.enqueue(rfidTag)) {
            try {
                DatabaseReference ref = database.getReference("nodes/" + shelterId);
                ref.setValue(node, (databaseError, databaseReference) -> {
                    if (databaseError != null) {
                        throw new RuntimeException("Failed to check in user: " + databaseError.getMessage());
                    }
                });
                return "User checked in successfully";
            } catch (DatabaseException e) {
                throw new RuntimeException("Error checking in user: " + e.getMessage(), e);
            }
        }
        return "Shelter is full or not found";
    }

    public String checkOutUser(String shelterId) {
        Node node = getNodeByShelterId(shelterId);
        if (node != null) {
            String dequeued = node.dequeue();
            if (dequeued != null) {
                try {
                    DatabaseReference ref = database.getReference("nodes/" + shelterId);
                    ref.setValue(node, (databaseError, databaseReference) -> {
                        if (databaseError != null) {
                            throw new RuntimeException("Failed to check out user: " + databaseError.getMessage());
                        }
                    });
                    return "User checked out successfully";
                } catch (DatabaseException e) {
                    throw new RuntimeException("Error checking out user: " + e.getMessage(), e);
                }
            }
            return "No users in queue";
        }
        return "Shelter not found";
    }

    private Node getNodeByShelterId(String shelterId) {
        DatabaseReference ref = database.getReference("nodes/" + shelterId);
        AtomicReference<Node> nodeRef = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                nodeRef.set(snapshot.getValue(Node.class));
                latch.countDown();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                throw new RuntimeException("Failed to fetch node: " + error.getMessage());
            }
        });
        try {
            latch.await();
            return nodeRef.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while fetching node", e);
        }
    }

    public String findNearestNode(double userLat, double userLng) {
        List<Node> nodes = getNodes();
        if (nodes.isEmpty()) {
            System.out.println("No nodes available in findNearestNode");
            return null;
        }
        String nearestId = null;
        double minDist = Double.MAX_VALUE;
        for (Node node : nodes) {
            double dist = haversineDistance(userLat, userLng, node.getLatitude(), node.getLongitude());
            if (dist < minDist) {
                minDist = dist;
                nearestId = node.getShelterId();
            }
        }
        System.out.println("Nearest node ID: " + nearestId);
        return nearestId;
    }

    public String findNearestShelter(double userLat, double userLng) {
        List<Node> nodes = getNodes();
        if (nodes.isEmpty()) {
            System.out.println("No nodes available in findNearestShelter");
            return null;
        }
        String nearestId = null;
        double minDist = Double.MAX_VALUE;
        for (Node node : nodes) {
            if (node.getCapacity() > 0) { // Only consider shelters
                double dist = haversineDistance(userLat, userLng, node.getLatitude(), node.getLongitude());
                if (dist < minDist) {
                    minDist = dist;
                    nearestId = node.getShelterId();
                }
            }
        }
        System.out.println("Nearest shelter ID: " + nearestId);
        return nearestId;
    }

    private double haversineDistance(double lat1, double lng1, double lat2, double lng2) {
        double earthRadius = 6371; // km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadius * c;
    }

    public RouteResponse shortestPathWithDistance(String start, String end) {
        List<Edge> allEdges = getEdges();
        if (allEdges.isEmpty()) {
            System.out.println("No edges available in shortestPathWithDistance");
            return new RouteResponse(List.of(), Double.MAX_VALUE);
        }
        Map<String, List<AbstractMap.SimpleEntry<String, Double>>> adj = new HashMap<>();
        for (Edge edge : allEdges) {
            if (!edge.isBlocked()) {
                adj.computeIfAbsent(edge.getFrom(), k -> new ArrayList<>()).add(new AbstractMap.SimpleEntry<>(edge.getTo(), edge.getWeight()));
                adj.computeIfAbsent(edge.getTo(), k -> new ArrayList<>()).add(new AbstractMap.SimpleEntry<>(edge.getFrom(), edge.getWeight())); // Undirected
            }
        }

        Map<String, Double> dist = new HashMap<>();
        Map<String, String> prev = new HashMap<>();
        PriorityQueue<AbstractMap.SimpleEntry<Double, String>> pq = new PriorityQueue<>(Comparator.comparingDouble(AbstractMap.SimpleEntry::getKey));

        dist.put(start, 0.0);
        pq.add(new AbstractMap.SimpleEntry<>(0.0, start));

        while (!pq.isEmpty()) {
            AbstractMap.SimpleEntry<Double, String> cur = pq.poll();
            double d = cur.getKey();
            String u = cur.getValue();

            if (d > dist.getOrDefault(u, Double.MAX_VALUE)) continue;

            for (AbstractMap.SimpleEntry<String, Double> neighbor : adj.getOrDefault(u, List.of())) {
                String v = neighbor.getKey();
                double w = neighbor.getValue();
                double newDist = d + w;
                if (newDist < dist.getOrDefault(v, Double.MAX_VALUE)) {
                    dist.put(v, newDist);
                    prev.put(v, u);
                    pq.add(new AbstractMap.SimpleEntry<>(newDist, v));
                }
            }
        }

        if (!dist.containsKey(end)) {
            System.out.println("No path found from " + start + " to " + end);
            return new RouteResponse(List.of(), Double.MAX_VALUE);
        }

        List<String> path = new ArrayList<>();
        String current = end;
        while (current != null) {
            path.add(current);
            current = prev.get(current);
        }
        Collections.reverse(path);
        return new RouteResponse(path, dist.get(end));
    }

    public RouteResponse shortestPathFromUser(double userLat, double userLng, String nearestNodeId, String end) {
        List<Node> nodes = getNodes();
        List<Edge> allEdges = getEdges();
        if (nodes.isEmpty() || allEdges.isEmpty()) {
            System.out.println("No nodes or edges available in shortestPathFromUser");
            return new RouteResponse(List.of(), Double.MAX_VALUE);
        }

        // Create adjacency list
        Map<String, List<AbstractMap.SimpleEntry<String, Double>>> adj = new HashMap<>();
        for (Edge edge : allEdges) {
            if (!edge.isBlocked()) {
                adj.computeIfAbsent(edge.getFrom(), k -> new ArrayList<>()).add(new AbstractMap.SimpleEntry<>(edge.getTo(), edge.getWeight()));
                adj.computeIfAbsent(edge.getTo(), k -> new ArrayList<>()).add(new AbstractMap.SimpleEntry<>(edge.getFrom(), edge.getWeight()));
            }
        }

        // Add temporary user node
        String userNodeId = "USER";
        Node nearestNode = nodes.stream()
                .filter(n -> n.getShelterId().equals(nearestNodeId))
                .findFirst()
                .orElse(null);
        if (nearestNode == null) {
            System.out.println("Nearest node not found: " + nearestNodeId);
            return new RouteResponse(List.of(), Double.MAX_VALUE);
        }

        // Add edge from user to nearest node
        double userToNearestDist = haversineDistance(userLat, userLng, nearestNode.getLatitude(), nearestNode.getLongitude());
        adj.computeIfAbsent(userNodeId, k -> new ArrayList<>()).add(new AbstractMap.SimpleEntry<>(nearestNodeId, userToNearestDist));
        adj.computeIfAbsent(nearestNodeId, k -> new ArrayList<>()).add(new AbstractMap.SimpleEntry<>(userNodeId, userToNearestDist));

        // Dijkstra's algorithm
        Map<String, Double> dist = new HashMap<>();
        Map<String, String> prev = new HashMap<>();
        PriorityQueue<AbstractMap.SimpleEntry<Double, String>> pq = new PriorityQueue<>(Comparator.comparingDouble(AbstractMap.SimpleEntry::getKey));

        dist.put(userNodeId, 0.0);
        pq.add(new AbstractMap.SimpleEntry<>(0.0, userNodeId));

        while (!pq.isEmpty()) {
            AbstractMap.SimpleEntry<Double, String> cur = pq.poll();
            double d = cur.getKey();
            String u = cur.getValue();

            if (d > dist.getOrDefault(u, Double.MAX_VALUE)) continue;

            for (AbstractMap.SimpleEntry<String, Double> neighbor : adj.getOrDefault(u, List.of())) {
                String v = neighbor.getKey();
                double w = neighbor.getValue();
                double newDist = d + w;
                if (newDist < dist.getOrDefault(v, Double.MAX_VALUE)) {
                    dist.put(v, newDist);
                    prev.put(v, u);
                    pq.add(new AbstractMap.SimpleEntry<>(newDist, v));
                }
            }
        }

        if (!dist.containsKey(end)) {
            System.out.println("No path found from USER to " + end);
            return new RouteResponse(List.of(), Double.MAX_VALUE);
        }

        List<String> path = new ArrayList<>();
        String current = end;
        while (current != null) {
            path.add(current);
            current = prev.get(current);
        }
        Collections.reverse(path);
        return new RouteResponse(path, dist.get(end));
    }

    public void initSampleData() {
        try {
            DatabaseReference nodesRef = database.getReference("nodes");
            nodesRef.child("A").setValue(new Node("A", "Shelter A", 6.9271, 79.8612, 10), (databaseError, ref) -> {
                if (databaseError != null) {
                    System.err.println("Failed to save node A: " + databaseError.getMessage());
                } else {
                    System.out.println("Saved node A");
                }
            });
            nodesRef.child("B").setValue(new Node("B", "Point B", 6.9371, 79.8712, 0), (databaseError, ref) -> {
                if (databaseError != null) {
                    System.err.println("Failed to save node B: " + databaseError.getMessage());
                } else {
                    System.out.println("Saved node B");
                }
            });
            nodesRef.child("C").setValue(new Node("C", "Point C", 6.9471, 79.8812, 0), (databaseError, ref) -> {
                if (databaseError != null) {
                    System.err.println("Failed to save node C: " + databaseError.getMessage());
                } else {
                    System.out.println("Saved node C");
                }
            });
            nodesRef.child("D").setValue(new Node("D", "Shelter D", 6.9571, 79.8912, 15), (databaseError, ref) -> {
                if (databaseError != null) {
                    System.err.println("Failed to save node D: " + databaseError.getMessage());
                } else {
                    System.out.println("Saved node D");
                }
            });
            nodesRef.child("E").setValue(new Node("E", "Point E", 6.9671, 79.9012, 0), (databaseError, ref) -> {
                if (databaseError != null) {
                    System.err.println("Failed to save node E: " + databaseError.getMessage());
                } else {
                    System.out.println("Saved node E");
                }
            });

            DatabaseReference edgesRef = database.getReference("edges");
            edgesRef.child("AB").setValue(new Edge("A", "B", 5.0, false), (databaseError, ref) -> {
                if (databaseError != null) {
                    System.err.println("Failed to save edge AB: " + databaseError.getMessage());
                } else {
                    System.out.println("Saved edge AB");
                }
            });
            edgesRef.child("BC").setValue(new Edge("B", "C", 3.0, false), (databaseError, ref) -> {
                if (databaseError != null) {
                    System.err.println("Failed to save edge BC: " + databaseError.getMessage());
                } else {
                    System.out.println("Saved edge BC");
                }
            });
            edgesRef.child("CD").setValue(new Edge("C", "D", 4.0, false), (databaseError, ref) -> {
                if (databaseError != null) {
                    System.err.println("Failed to save edge CD: " + databaseError.getMessage());
                } else {
                    System.out.println("Saved edge CD");
                }
            });
            edgesRef.child("BE").setValue(new Edge("B", "E", 6.0, false), (databaseError, ref) -> {
                if (databaseError != null) {
                    System.err.println("Failed to save edge BE: " + databaseError.getMessage());
                } else {
                    System.out.println("Saved edge BE");
                }
            });
        } catch (DatabaseException e) {
            throw new RuntimeException("Error initializing sample data: " + e.getMessage(), e);
        }
    }

    public void clearData() {
        try {
            database.getReference("nodes").removeValue((databaseError, ref) -> {
                if (databaseError != null) {
                    throw new RuntimeException("Failed to clear nodes: " + databaseError.getMessage());
                }
            });
            database.getReference("edges").removeValue((databaseError, ref) -> {
                if (databaseError != null) {
                    throw new RuntimeException("Failed to clear edges: " + databaseError.getMessage());
                }
            });
        } catch (DatabaseException e) {
            throw new RuntimeException("Error clearing data: " + e.getMessage(), e);
        }
    }

    public static class RouteResponse {
        private final List<String> path;
        private final double distance;

        public RouteResponse(List<String> path, double distance) {
            this.path = path;
            this.distance = distance;
        }

        public List<String> getPath() {
            return path;
        }

        public double getDistance() {
            return distance;
        }
    }
}