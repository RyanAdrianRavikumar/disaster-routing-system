package com.tursa.route.service;

import com.google.firebase.database.*;
import com.tursa.route.model.Edge;
import com.tursa.route.model.Node;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

@Service
public class RouteService {

    private final FirebaseDatabase database;

    public RouteService(FirebaseDatabase database) {
        this.database = database;
    }

    // Fetch edges from Firebase
    public List<Edge> getEdges() throws InterruptedException {
        DatabaseReference ref = database.getReference("edges");
        List<Edge> edges = new ArrayList<>();

        CountDownLatch latch = new CountDownLatch(1);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot child : snapshot.getChildren()) {
                    Edge edge = child.getValue(Edge.class);
                    if (edge != null) edges.add(edge);
                }
                latch.countDown();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                latch.countDown();
            }
        });

        latch.await();
        return edges;
    }

    // Fetch nodes from Firebase
    public List<Node> getNodes() throws InterruptedException {
        DatabaseReference ref = database.getReference("nodes");
        List<Node> nodes = new ArrayList<>();

        CountDownLatch latch = new CountDownLatch(1);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot child : snapshot.getChildren()) {
                    Node node = child.getValue(Node.class);
                    if (node != null) nodes.add(node);
                }
                latch.countDown();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                latch.countDown();
            }
        });

        latch.await();
        return nodes;
    }

    // Create a response object that includes both path and distance
    public static class RouteResponse {
        private List<String> path;
        private double distance;

        public RouteResponse(List<String> path, double distance) {
            this.path = path;
            this.distance = distance;
        }

        // Getters and setters
        public List<String> getPath() { return path; }
        public void setPath(List<String> path) { this.path = path; }
        public double getDistance() { return distance; }
        public void setDistance(double distance) { this.distance = distance; }
    }

    // Dijkstra algorithm that returns both path and distance
    public RouteResponse shortestPathWithDistance(String start, String end, List<Edge> edges) {
        // Validate that start and end nodes exist
        Set<String> nodeIds;
        try {
            nodeIds = getNodes().stream().map(Node::getId).collect(Collectors.toSet());
        } catch (InterruptedException e) {
            throw new RuntimeException("Failed to fetch nodes", e);
        }

        if (!nodeIds.contains(start)) {
            throw new IllegalArgumentException("Start node '" + start + "' does not exist");
        }

        if (!nodeIds.contains(end)) {
            throw new IllegalArgumentException("End node '" + end + "' does not exist");
        }

        // Build graph, skipping blocked edges
        Map<String, List<Edge>> graph = new HashMap<>();
        for (Edge e : edges) {
            if (!e.isBlocked()) {
                graph.computeIfAbsent(e.getFrom(), k -> new ArrayList<>()).add(e);
                // Ensure all nodes are in the graph, even if they have no outgoing edges
                graph.putIfAbsent(e.getTo(), new ArrayList<>());
            }
        }

        // Ensure start node is in graph even if it has no outgoing edges
        graph.putIfAbsent(start, new ArrayList<>());

        // Initialize distances
        Map<String, Double> dist = new HashMap<>();
        Map<String, String> prev = new HashMap<>();
        PriorityQueue<Map.Entry<String, Double>> pq = new PriorityQueue<>(Comparator.comparingDouble(Map.Entry::getValue));

        // Set all distances to infinity
        for (String node : graph.keySet()) {
            dist.put(node, Double.MAX_VALUE);
        }

        dist.put(start, 0.0);
        pq.add(Map.entry(start, 0.0));

        while (!pq.isEmpty()) {
            var current = pq.poll();
            String u = current.getKey();
            double d = current.getValue();

            // If we've found a better path to u since adding it to the queue, skip it
            if (d > dist.get(u)) {
                continue;
            }

            // Explore neighbors
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

        // Build the path
        List<String> path = new ArrayList<>();
        String u = end;

        // Check if a path exists
        if (!prev.containsKey(end) && !end.equals(start)) {
            return new RouteResponse(new ArrayList<>(), -1.0);
        }

        while (u != null) {
            path.add(0, u);
            u = prev.get(u);
        }

        // Return both path and distance
        return new RouteResponse(path, dist.get(end));
    }
}