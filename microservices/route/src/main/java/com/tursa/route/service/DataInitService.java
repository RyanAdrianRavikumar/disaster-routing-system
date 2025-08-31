package com.tursa.route.service;

import com.google.firebase.database.*;
import com.tursa.route.model.Edge;
import com.tursa.route.model.Node;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

@Service
public class DataInitService {

    private final FirebaseDatabase database;

    public DataInitService(FirebaseDatabase database) {
        this.database = database;
    }

    @PostConstruct
    public void initData() {
        // Check if database is empty using the traditional approach
        DatabaseReference ref = database.getReference();
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    addSampleData();
                    System.out.println("Sample data initialized in Firebase");
                } else {
                    // Check if we need to update existing data structure
                    updateExistingData(snapshot);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                System.err.println("Failed to check database status: " + error.getMessage());
            }
        });
    }

    public void addSampleData() {
        // Add sample nodes
        Map<String, Node> nodes = new HashMap<>();
        nodes.put("A", new Node("A", "Point A"));
        nodes.put("B", new Node("B", "Point B"));
        nodes.put("C", new Node("C", "Point C"));
        nodes.put("D", new Node("D", "Point D"));
        nodes.put("E", new Node("E", "Point E"));

        DatabaseReference nodesRef = database.getReference("nodes");
        nodesRef.setValueAsync(nodes);

        // Add sample edges with unique IDs
        Map<String, Edge> edges = new HashMap<>();
        edges.put("edge1", new Edge("A", "B", 5.0, false));
        edges.put("edge2", new Edge("A", "C", 10.0, false));
        edges.put("edge3", new Edge("B", "D", 3.0, false));
        edges.put("edge4", new Edge("C", "D", 6.0, false));
        edges.put("edge5", new Edge("D", "E", 4.0, false));
        edges.put("edge6", new Edge("B", "E", 15.0, false));

        DatabaseReference edgesRef = database.getReference("edges");
        edgesRef.setValueAsync(edges);
    }

    private void updateExistingData(DataSnapshot snapshot) {
        // Check if edges have the blocked field
        if (snapshot.hasChild("edges")) {
            DataSnapshot edgesSnapshot = snapshot.child("edges");
            for (DataSnapshot edgeSnapshot : edgesSnapshot.getChildren()) {
                if (!edgeSnapshot.hasChild("blocked")) {
                    // Add blocked field to existing edges
                    DatabaseReference edgeRef = database.getReference("edges").child(edgeSnapshot.getKey()).child("blocked");
                    edgeRef.setValueAsync(false);
                }
            }
        }
    }

    public void clearData() {
        DatabaseReference ref = database.getReference();
        ref.removeValueAsync();
        System.out.println("All data cleared from Firebase");
    }
}